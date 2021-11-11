package uk.ac.ed.inf.models.db;

import uk.ac.ed.inf.LongLat;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Flightpath {
    // Order number
    public String orderId;

    // Start location
    public LongLat from;

    // Flight angle
    public int angle;

    // End location
    public LongLat to;

    /**
     * Create Flightpath from raw values (for insert)
     * @param orderId Order number
     * @param from Starting location
     * @param angle Flight angle
     * @param to Ending location
     */
    public Flightpath(String orderId, LongLat from, int angle, LongLat to) {
        this.orderId = orderId;
        this.from = from;
        this.angle = angle;
        this.to = to;
    }

    /**
     * Create Flightpath from SQL query result
     * @param r query result
     * @throws SQLException if any of the columns don't exist in the result
     */
    public Flightpath(ResultSet r) throws SQLException {
        this.orderId = r.getString("orderNo");
        this.from = new LongLat(r.getDouble("fromLongitude"), r.getDouble("fromLatitude"));
        this.angle = r.getInt("angle");
        this.to = new LongLat(r.getDouble("toLongitude"), r.getDouble("toLatitude"));
    }

    /**
     * Generates schema for the Flightpath table
     * @return SQL statement that creates flightpath table
     */
    public static String getSchema() {
        return "create table flightpath(orderNo char(8), fromLongitude double, fromLatitude double, angle integer, toLongitude double, toLatitude double)";
    }
}