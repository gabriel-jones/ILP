package uk.ac.ed.inf.models.db;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Order {
    public String id;
    public Date deliveryDate;
    public String customer;
    public String deliverTo;

    public Order(ResultSet r) throws SQLException {
        this.id = r.getString("orderNo");
        this.deliveryDate = r.getDate("deliveryDate");
        this.customer = r.getString("customer");
        this.deliverTo = r.getString("deliverTo");
    }
}
