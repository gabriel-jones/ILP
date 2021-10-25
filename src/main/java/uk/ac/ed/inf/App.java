package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import uk.ac.ed.inf.models.api.Vendor;
import uk.ac.ed.inf.models.db.*;

import java.awt.geom.Line2D;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

public class App {
    static DbManager dbManager;
    static Menus menus;
    static Buildings buildings;
    static What3Words what3Words;
    static Date date;

    static LongLat dronePosition = Constant.APPLETON_TOWER;
    static int moves = 0;

    public static void main(String[] args) {
        setup(args);
        run();
    }

    static void setup(String[] args) {
        try {
            int day = Integer.parseInt(args[0]);
            int month = Integer.parseInt(args[1]);
            int year = Integer.parseInt(args[2]);
            date = Date.valueOf(LocalDate.of(year, month, day));

            String serverPort = args[3];
            menus = new Menus("localhost", serverPort);
            buildings = new Buildings("localhost", serverPort);
            what3Words = new What3Words("localhost", serverPort);

            for (Vendor vendor : menus.vendors) {
                vendor.setCoordinates(what3Words.getLocation(vendor.getLocation()));
            }

            String dbPort = args[4];
            dbManager = new DbManager(dbPort);
        } catch (ArrayIndexOutOfBoundsException e) {
            // TODO: invalid args
            System.err.println(e);
        } catch (NumberFormatException e) {
            // TODO: invalid date numbers
            System.err.println(e);
        }
    }

    static void run() {
        ArrayList<Order> orders = dbManager.getOrders(date);

        for (Order order : orders) {
            System.out.println(order.id);
            ArrayList<OrderDetail> items = dbManager.getOrderDetails(order);
            String[] itemNames = items.stream().map(i -> i.item).toArray(String[]::new);
            int orderCost = menus.getDeliveryCost(itemNames);
            System.out.println(orderCost);

            // FLY to restaurant
            Vendor targetVendor = menus.getVendor(items.get(0).item);
            if (targetVendor == null) {
                // TODO: invalid item name
            }
            flyTo(order.id, targetVendor.getCoordinates());

            // FLY to delivery point
            LongLat deliveryLocation = what3Words.getLocation(order.deliverTo);
            flyTo(order.id, deliveryLocation);

            // hover
            moves++;
            // TODO: insert flightpath for hover?

            // insert delivery
            dbManager.insertDelivery(new Delivery(order.id, "", orderCost));
        }

        System.out.println("\nMOVES:");
        System.out.println(moves);

        ArrayList<Flightpath> flightpaths = dbManager.getFlightpaths();
        FlightpathWriter.write(date, flightpaths);
    }

    static void flyTo(String orderId, LongLat location) {
        Line2D path = new Line2D.Double(dronePosition.longitude, dronePosition.latitude, location.longitude, location.latitude);
        if (buildings.intersects(path)) {
            // TODO: can't assume landmarks will always avoid no fly zone
            LongLat viableLandmark = null;
            for (Point landmark : buildings.landmarks) {
                LongLat landmarkLocation = new LongLat(landmark.longitude(), landmark.latitude());
                path = new Line2D.Double(dronePosition.longitude, dronePosition.latitude, landmarkLocation.longitude, landmarkLocation.latitude);
                if (!buildings.intersects(path)) {
                    viableLandmark = landmarkLocation;
                    break;
                }
            }

            if (viableLandmark != null) {
                flyTo(orderId, viableLandmark);
            } else {
                System.err.println("Cant find a landmark to fly to!!");
            }
        }

        LongLat startPosition = dronePosition;
        while (!dronePosition.closeTo(location)) {
            int angle = dronePosition.angleTo(location);
            dronePosition = dronePosition.nextPosition(angle);
            moves++;
            dbManager.insertFlightpath(new Flightpath(orderId, startPosition, angle, dronePosition));
        }
    }
}