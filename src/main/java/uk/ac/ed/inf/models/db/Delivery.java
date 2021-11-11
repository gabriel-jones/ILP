package uk.ac.ed.inf.models.db;

import java.sql.ResultSet;
import java.sql.SQLException;


public class Delivery {
    // Order number
    public String orderId;

    // What3Words location for delivery
    public String deliveredTo;

    // Total cost of order
    public int costInPence;

    /**
     * Create Delivery object from raw values (used for inserts)
     * @param orderId Order Number
     * @param deliveredTo What3Words location for delivery location
     * @param costInPence Order cost including delivery
     */
    public Delivery(String orderId, String deliveredTo, int costInPence) {
        this.orderId = orderId;
        this.deliveredTo = deliveredTo;
        this.costInPence = costInPence;
    }

    /**
     * Create Delivery class from SQL query results
     * @param r results from the query
     * @throws SQLException if any of the schema columns don't exist in the result
     */
    public Delivery(ResultSet r) throws SQLException {
        this.orderId = r.getString("orderNo");
        this.deliveredTo = r.getString("deliveredTo");
        this.costInPence = r.getInt("costInPence");
    }

    /**
     * Generates SQL for creating the Deliveries table
     * @return the SQL statement
     */
    public static String getSchema() {
        return "create table deliveries(orderNo char(8), deliveredTo varchar(19), costInPence int)";
    }
}