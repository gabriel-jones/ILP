package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import uk.ac.ed.inf.models.api.Vendor;
import uk.ac.ed.inf.models.db.*;

import java.awt.geom.Line2D;
import java.sql.Date;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class App {
    // Database manager class for handling database connection
    static DbManager dbManager;

    // Menus class handles the Vendors and MenuItems
    static Menus menus;

    // Buildings class handles landmarks and no-fly zones
    static Buildings buildings;

    // What3Words class looks up LongLat objects for 3 word area descriptors
    static What3Words what3Words;

    // Date to run the program for
    static Date date;

    // Position of drone (starts at appleton tower)
    static LongLat dronePosition = Constant.APPLETON_TOWER;

    // Number of moves made by the drone
    static int moves = 0;

    // Entry point for the application
    public static void main(String[] args) {
        boolean setupSuccess = setup(args);
        // Only run drone control algorithm if setup is successful
        if (setupSuccess) {
            run();
        }
    }

    /**
     * Parses command line arguments, sets up HTTP client,
     * and connects to database. Also loads the LongLat coordinates for
     * each vendor
     * @param args command line arguments
     * @return true if setup was successful (all resources fetched, database connected, etc.)
     */
    static boolean setup(String[] args) {
        try {
            // Parse date from command line args
            int day = Integer.parseInt(args[0]);
            int month = Integer.parseInt(args[1]);
            int year = Integer.parseInt(args[2]);
            date = Date.valueOf(LocalDate.of(year, month, day));

            // Create HTTP client from hostname and port
            String serverPort = args[3];
            ServerClient serverClient = new ServerClient("localhost", serverPort);

            // Fetch initial data from server and create resource-handler classes
            menus = new Menus(serverClient);
            if (menus.vendors == null) {
                return false; // If menus can't be fetched, exit program
            }

            buildings = new Buildings(serverClient);
            if (buildings.landmarks == null || buildings.noFlyZones == null) {
                return false; // If buildings can't be fetched, exit program
            }
            what3Words = new What3Words(serverClient);

            // Load coordinates of each Vendor from their what3words address
            for (Vendor vendor : menus.vendors) {
                vendor.setCoordinates(what3Words.getLocation(vendor.getLocation()));
            }

            // Connect to database server
            String dbPort = args[4];
            dbManager = new DbManager(dbPort);
            return dbManager.connect();
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException | DateTimeException e) {
            e.printStackTrace();
            return false; // If invalid args given, exit program
        }
    }

    /**
     * Runs the algorithm for creating a flightpath for the drone based on orders
     */
    static void run() {
        // Get orders for the given date
        ArrayList<Order> orders = dbManager.getOrders(date);

        // Monetary values for all orders and delivered orders
        int totalMonetaryValue = 0;
        int deliveredMonetaryValue = 0;

        // Order items and order cost
        HashMap<String, ArrayList<OrderDetail>> orderItems = new HashMap<>();
        HashMap<String, Integer> orderCost = new HashMap<>();

        for (Order order : orders) {
            // Get order items
            ArrayList<OrderDetail> details = dbManager.getOrderDetails(order);
            orderItems.put(order.id, details);

            // Calculate order cost in pence
            String[] itemNames = details.stream().map(i -> i.item).toArray(String[]::new);
            int cost = menus.getDeliveryCost(itemNames);
            orderCost.put(order.id, cost);
            totalMonetaryValue += cost;
        }

        for (Order order : orders) {
            // Lookup delivery location for order
            LongLat deliveryLocation = what3Words.getLocation(order.deliverTo);

            // Get target vendor from order items
            Vendor targetVendor = menus.getVendor(orderItems.get(order.id).get(0).item);
            if (targetVendor == null) {
                System.err.println("Invalid item name");
                return;
            }

            // Check if the moves for this order would exceed MAX_MOVES
            int vendorMoves = (int)(dronePosition.distanceTo(targetVendor.getCoordinates()) / Constant.MOVE_AMOUNT);
            int deliveryMoves = (int)(targetVendor.getCoordinates().distanceTo(deliveryLocation) / Constant.MOVE_AMOUNT);
            int orderMoves = 2 + vendorMoves + deliveryMoves;
            int startMoves = (int)(deliveryLocation.distanceTo(Constant.APPLETON_TOWER) / Constant.MOVE_AMOUNT);
            if (moves + orderMoves + startMoves >= Constant.MAX_MOVES) {
                break;
            }

            // Fly to vendor location
            flyTo(order.id, targetVendor.getCoordinates());

            // Hover to pickup
            moves++;

            // Fly to delivery location
            flyTo(order.id, deliveryLocation);

            // Hover and deliver
            moves++;

            int cost = orderCost.get(order.id);
            dbManager.insertDelivery(new Delivery(order.id, "", cost));
            deliveredMonetaryValue += cost;
        }

        // TODO: remove
        System.out.println(moves);

        // Return to Appleton Tower
        flyTo(null, Constant.APPLETON_TOWER);

        // TODO: remove
        System.out.println(moves);

        // Write flight paths to drone GeoJSON output
        ArrayList<Flightpath> flightpaths = dbManager.getFlightpaths();
        FlightpathWriter.write(date, flightpaths);

        // Output percentage monetary value
        System.out.println("Percentage monetary value:");
        System.out.printf("%f\n", (float)(deliveredMonetaryValue) / (float)(totalMonetaryValue));
    }

    static void flyTo(String orderId, LongLat location) {
        Line2D path = new Line2D.Double(dronePosition.longitude, dronePosition.latitude, location.longitude, location.latitude);
        // If line between current position and target location intersects no-fly zone
        if (buildings.intersects(path)) {
            // Find first viable landmark (where path between current position and landmark doesn't intersect no-fly zone)
            LongLat viableLandmark = null;
            for (Point landmark : buildings.landmarks) {
                // LongLat of the landmark (from GeoJSON point)
                LongLat landmarkLocation = new LongLat(landmark.longitude(), landmark.latitude());

                // Line2D from the current drone position to the landmark
                path = new Line2D.Double(dronePosition.longitude, dronePosition.latitude, landmarkLocation.longitude, landmarkLocation.latitude);

                // If the estimated path (`path` Line2D) does not intersect
                // the no-fly zone polygons, it's a viable landmark to fly to
                if (!buildings.intersects(path)) {
                    viableLandmark = landmarkLocation;
                    break;
                }
            }

            // If a viable landmark is found, then fly to it before continuing to destination
            if (viableLandmark != null) {
                flyTo(orderId, viableLandmark);
            }
        }

        // Flying: start at `dronePosition`, move towards `location` until
        // close to location. For each movement, create a new Flightpath and
        // increase moves by 1.
        LongLat startPosition = dronePosition;
        while (!dronePosition.closeTo(location)) {
            // Calculate angle to fly
            int angle = dronePosition.angleTo(location);

            // Set position as 1 move towards angle
            dronePosition = dronePosition.nextPosition(angle);

            // Increase moves by 1
            moves++;

            // Create Flightpath for this move
            dbManager.insertFlightpath(new Flightpath(orderId, startPosition, angle, dronePosition));
        }
    }
}