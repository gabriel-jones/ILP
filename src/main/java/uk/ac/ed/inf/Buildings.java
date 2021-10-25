package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Buildings extends ServerClient {
    ArrayList<Point> landmarks = new ArrayList<>();
    ArrayList<Polygon> noFlyZones = new ArrayList<>();

    /**
     * Instantiates the landmarks and no-fly zones from the server
     * @param host server hostname
     * @param port server port
     */
    public Buildings(String host, String port) {
        super(host, port);

        // Fetch the buildings from the server
        HttpResponse<String> landmarksGeoJson = this.httpGet("/buildings/landmarks.geojson");
        HttpResponse<String> noFlyZonesGeoJson = this.httpGet("/buildings/no-fly-zones.geojson");

        // Cast response JSON strings to list of GeoJSON objects
        try {
            FeatureCollection landmarksFeatureCollection = FeatureCollection.fromJson(landmarksGeoJson.body());
            for (Feature feature : landmarksFeatureCollection.features()) {
                landmarks.add((Point) feature.geometry());
            }
            FeatureCollection noFlyZonesFeatureCollection = FeatureCollection.fromJson(noFlyZonesGeoJson.body());
            for (Feature feature : noFlyZonesFeatureCollection.features()) {
                noFlyZones.add((Polygon) feature.geometry());
            }
        } catch (ClassCastException e) {
            // TODO: handle invalid GeoJSON from server
            System.err.println(e);
        }
    }

    /**
     * Checks if a given line intersects any of the buildings in the
     * no-fly zone
     * @param line the given path to check
     * @return true iff the line goes through any of the buildings
     */
    public boolean intersects(Line2D line) {
        for (Polygon building : noFlyZones) {
            if (intersects(line, building)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a line intersects a polygon
     * @param line
     * @param polygon
     * @return true iff the line goes through one of the polygon's edges
     */
    private boolean intersects(Line2D line, Polygon polygon) {
        List<Point> points = polygon.coordinates().get(0);

        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            Line2D edge = new Line2D.Double(p1.longitude(), p1.latitude(), p2.longitude(), p2.latitude());
            if (line.intersectsLine(edge)) {
                return true;
            }
        }
        return false;
    }
}
