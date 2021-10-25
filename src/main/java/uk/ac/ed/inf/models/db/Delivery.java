package uk.ac.ed.inf.models.db;

import uk.ac.ed.inf.LongLat;

import java.sql.ResultSet;
import java.sql.SQLException;


public class Delivery {
    public String orderId;
    public String deliveredTo;
    public int costInPence;

    public Delivery(String orderId, String deliveredTo, int costInPence) {
        this.orderId = orderId;
        this.deliveredTo = deliveredTo;
        this.costInPence = costInPence;
    }

    public Delivery(ResultSet r) throws SQLException {
        this.orderId = r.getString("orderNo");
        this.deliveredTo = r.getString("deliveredTo");
        this.costInPence = r.getInt("costInPence");
    }

    public static String getSchema() {
        return "create table deliveries(orderNo char(8), deliveredTo varchar(19), costInPence int)";
    }
}