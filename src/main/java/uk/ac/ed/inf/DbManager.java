package uk.ac.ed.inf;

import uk.ac.ed.inf.models.db.*;

import java.sql.*;
import java.util.ArrayList;

public class DbManager {
    String port;
    Connection connection;

    private String getJdbcUrl() {
        return String.format("jdbc:derby://localhost:%s/derbyDB", this.port);
    }

    public DbManager(String port) {
        this.port = port;

        try {
            // Connect to database and check metadata for table info
            this.connection = DriverManager.getConnection(this.getJdbcUrl());
            DatabaseMetaData metaData = connection.getMetaData();

            // Drop tables if they exist, then create them (wipes data)
            createTable(metaData, "deliveries");
            createTable(metaData, "flightpath");

        } catch (SQLException e) {
            // TODO: db error
            System.err.println(e);
        }
    }

    /**
     * Create a table if it doesn't exist. Drop the table if it does exist
     * @param metaData metadata on tables in the database
     * @param tableName table to create (and drop if it exists)
     * @throws SQLException
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
        switch (tableName) {
            case "deliveries":
                return Delivery.getSchema();
            case "flightpath":
                return Flightpath.getSchema();
            default: return null;
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<Order> getOrders(Date deliveryDate) {
        try {
            final String sql = "select * from orders where deliveryDate = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, deliveryDate);

            ResultSet results = statement.executeQuery();
            ArrayList<Order> orders = new ArrayList<>();
            while (results.next()) {
                orders.add(new Order(results));
            }
            return orders;
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<Flightpath> getFlightpaths() {
        try {
            final String sql = "select * from flightpath";
            Statement statement = connection.createStatement();

            ResultSet results = statement.executeQuery(sql);
            ArrayList<Flightpath> flightpaths = new ArrayList<>();
            while (results.next()) {
                flightpaths.add(new Flightpath(results));
            }
            return flightpaths;
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    public ArrayList<OrderDetail> getOrderDetails(Order order) {
        try {
            final String sql = "select * from orderDetails where orderNo = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, order.id);

            ResultSet results = statement.executeQuery();
            ArrayList<OrderDetail> orderDetails = new ArrayList<>();
            while (results.next()) {
                orderDetails.add(new OrderDetail(results));
            }
            return orderDetails;
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    public boolean insertDelivery(Delivery delivery) {
        try {
            final String sql = "insert into deliveries (orderNo, deliveredTo, costInPence) values (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, delivery.orderId);
            statement.setString(2, delivery.deliveredTo);
            statement.setInt(3, delivery.costInPence);
            return statement.execute();
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean insertFlightpath(Flightpath flightpath) {
        try {
            final String sql = "insert into flightpath (orderNo, fromLongitude, fromLatitude, angle, toLongitude, toLatitude) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, flightpath.orderId);
            statement.setDouble(2, flightpath.from.longitude);
            statement.setDouble(3, flightpath.from.latitude);
            statement.setInt(4, flightpath.angle);
            statement.setDouble(5, flightpath.to.longitude);
            statement.setDouble(6, flightpath.to.latitude);
            return statement.execute();
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }
}
