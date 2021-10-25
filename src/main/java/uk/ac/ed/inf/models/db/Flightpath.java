package uk.ac.ed.inf.models.db;


import uk.ac.ed.inf.LongLat;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Flightpath {
    public String orderId;
    public LongLat from;
    public int angle;
    public LongLat to;

    public Flightpath(String orderId, LongLat from, int angle, LongLat to) {
        this.orderId = orderId;
        this.from = from;
        this.angle = angle;
        this.to = to;
    }

    public Flightpath(ResultSet r) throws SQLException {
        this.orderId = r.getString("orderNo");
        this.from = new LongLat(r.getDouble("fromLongitude"), r.getDouble("fromLatitude"));
        this.angle = r.getInt("angle");
        this.to = new LongLat(r.getDouble("toLongitude"), r.getDouble("toLatitude"));
    }

    public static String getSchema() {
        return "create table flightpath(orderNo char(8), fromLongitude double, fromLatitude double, angle integer, toLongitude double, toLatitude double)";
    }
}