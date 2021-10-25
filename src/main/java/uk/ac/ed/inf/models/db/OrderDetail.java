package uk.ac.ed.inf.models.db;


import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDetail {
    public String orderId;
    public String item;

    public OrderDetail(ResultSet r) throws SQLException {
        this.orderId = r.getString("orderNo");
        this.item = r.getString("item");
    }
}