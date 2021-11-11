package uk.ac.ed.inf;

import uk.ac.ed.inf.models.db.*;

import java.sql.*;
import java.util.ArrayList;

public class DbManager {
    // Port to connect to database on
    String port;

    // Database connection
    Connection connection;

    /**
     * Gets the databse url from the given database port using the JDBC derby protocol
     * @return formatted JDBC url
     */
    private String getJdbcUrl() {
        return String.format("jdbc:derby://localhost:%s/derbyDB", this.port);
    }

    /**
     * Creates DbManager class and sets connection port
     * @param port database connection port
     */
    public DbManager(String port) {
        this.port = port;
    }

    /**
     * Connects to the database and creates necessary tables
     */
    public boolean connect() {
        try {
            // Connect to database and check metadata for table info
            this.connection = DriverManager.getConnection(this.getJdbcUrl());
            DatabaseMetaData metaData = connection.getMetaData();

            // Drop tables if they exist, then create them (wipes data)
            createTable(metaData, "deliveries");
            createTable(metaData, "flightpath");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a table if it doesn't exist. Drop the table if it does exist
     * @param metaData metadata on tables in the database
     * @param tableName table to create (and drop if it exists)
     * @throws SQLException if exception on dropping / creating table
     */
    private void createTable(DatabaseMetaData metaData, String tableName) throws SQLException {
        // Check if table exists
        ResultSet resultSet = metaData.getTables(null, null, tableName.toUpperCase(), null);
        if (resultSet.next()) { // table exists, drop it
            Statement statement = connection.createStatement();
            // Don't need to use prepared statement as no user input is ever passed to this function
            statement.execute("drop table " + tableName);
        }
        // Create the table from its schema
        Statement statement = connection.createStatement();
        statement.execute(getTableSchema(tableName));
    }

    /**
     * Get the SQL for creating a certain table
     * @param tableName table to create
     * @return the SQL for creating the specified table
     */
    private String getTableSchema(String tableName) {
        return switch (tableName) {
            case "deliveries" -> Delivery.getSchema();
            case "flightpath" -> Flightpath.getSchema();
            default -> null;
        };
    }

    /**
     * Gets all the orders in the database with a delivery date of `deliveryDate`
     * @param deliveryDate target date to filter by
     * @return all the orders for the given date
     */
    public ArrayList<Order> getOrders(Date deliveryDate) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            final String sql = "select * from orders where deliveryDate = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, deliveryDate);

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                orders.add(new Order(results));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Get all the flightpath rows in the database
     * @return all the flightpaths
     */
    public ArrayList<Flightpath> getFlightpaths() {
        ArrayList<Flightpath> flightpaths = new ArrayList<>();
        try {
            final String sql = "select * from flightpath";
            Statement statement = connection.createStatement();

            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                flightpaths.add(new Flightpath(results));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flightpaths;
    }

    /**
     * Gets line items for a given order
     * @param order to fetch details for
     * @return list of all the items to deliver for an order
     */
    public ArrayList<OrderDetail> getOrderDetails(Order order) {
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        try {
            final String sql = "select * from orderDetails where orderNo = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, order.id);

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                orderDetails.add(new OrderDetail(results));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderDetails;
    }

    /**
     * Creates a delivery row in the database
     * @param delivery row to insert
     */
    public void insertDelivery(Delivery delivery) {
        try {
            final String sql = "insert into deliveries (orderNo, deliveredTo, costInPence) values (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, delivery.orderId);
            statement.setString(2, delivery.deliveredTo);
            statement.setInt(3, delivery.costInPence);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a flightpath in the database
     * @param flightpath row to insert
     */
    public void insertFlightpath(Flightpath flightpath) {
        try {
            final String sql = "insert into flightpath (orderNo, fromLongitude, fromLatitude, angle, toLongitude, toLatitude) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, flightpath.orderId);
            statement.setDouble(2, flightpath.from.longitude);
            statement.setDouble(3, flightpath.from.latitude);
            statement.setInt(4, flightpath.angle);
            statement.setDouble(5, flightpath.to.longitude);
            statement.setDouble(6, flightpath.to.latitude);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
