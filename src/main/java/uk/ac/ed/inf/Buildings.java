package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Buildings {
    // List of points for landmarks used for drone control algorithm
    ArrayList<Point> landmarks;

    // List of polygons for areas that the drone can't fly in
    ArrayList<Polygon> noFlyZones;

    /**
     * Instantiates the landmarks and no-fly zones from the server
     * @param client the HTTP server client to use
     */
    public Buildings(ServerClient client) {
        // Fetch the buildings from the server
        HttpResponse<String> landmarksGeoJson = client.httpGet("/buildings/landmarks.geojson");
        HttpResponse<String> noFlyZonesGeoJson = client.httpGet("/buildings/no-fly-zones.geojson");

        if (landmarksGeoJson == null || noFlyZonesGeoJson == null) {
            return;
        }

        // Cast response JSON strings to list of GeoJSON objects
        try {
            FeatureCollection landmarksFeatureCollection = FeatureCollection.fromJson(landmarksGeoJson.body());
            if (landmarksFeatureCollection.features() == null) {
                return;
            }
            landmarks = new ArrayList<>();
            for (Feature feature : landmarksFeatureCollection.features()) {
                // Get Points from landmarks response
                landmarks.add((Point) feature.geometry());
            }

            FeatureCollection noFlyZonesFeatureCollection = FeatureCollection.fromJson(noFlyZonesGeoJson.body());
            if (noFlyZonesFeatureCollection.features() == null) {
                return;
            }
            noFlyZones = new ArrayList<>();
            for (Feature feature : noFlyZonesFeatureCollection.features()) {
                // Get Polygons from no-fly zones response
                noFlyZones.add((Polygon) feature.geometry());
            }
        } catch (ClassCastException | NullPointerException e) {
            e.printStackTrace();
            landmarks = null;
            noFlyZones = null;
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
            if (intersectsPolygon(line, building)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a line intersects a polygon
     * @param line line to check
     * @param polygon polygon to check
     * @return true iff the line goes through one of the polygon's edges
     */
    private boolean intersectsPolygon(Line2D line, Polygon polygon) {
        List<Point> points = polygon.coordinates().get(0);

        // Iterate through all the edges of the polygon
        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            // Generate Line2D object from the polygon edge
            Line2D edge = new Line2D.Double(p1.longitude(), p1.latitude(), p2.longitude(), p2.latitude());
            if (line.intersectsLine(edge)) {
                return true;
            }
        }
        return false;
    }
}
