package uk.ac.ed.inf.models.db;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Order {
    // Order number
    public String id;

    // Date for delivery of this order
    public Date deliveryDate;

    // Student ID to deliver to
    public String customer;

    // What3Words location to deliver to
    public String deliverTo;

    /**
     * Creates an Order from a SQL query result
     * @param r query result
     * @throws SQLException if expected column names aren't in the result
     */
    public Order(ResultSet r) throws SQLException {
        this.id = r.getString("orderNo");
        this.deliveryDate = r.getDate("deliveryDate");
        this.customer = r.getString("customer");
        this.deliverTo = r.getString("deliverTo");
    }
}
