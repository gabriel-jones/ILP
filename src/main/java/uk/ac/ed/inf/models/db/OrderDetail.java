package uk.ac.ed.inf.models.db;


import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDetail {
    // Order number
    public String orderId;

    // Delivery item name
    public String item;

    /**
     * Create OrderDetail object from SQL query result
     * @param r result of query
     * @throws SQLException if expected column names don't exist in result
     */
    public OrderDetail(ResultSet r) throws SQLException {
        this.orderId = r.getString("orderNo");
        this.item = r.getString("item");
    }
}