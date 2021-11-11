package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import uk.ac.ed.inf.models.db.Flightpath;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FlightpathWriter {
    public static void write(Date date, ArrayList<Flightpath> flightpaths) {
        // Generate list of points along flight path
        ArrayList<Point> points = new ArrayList<>();
        // For each flightpath, add points to the `points` array
        for (Flightpath flightpath : flightpaths) {
            points.add(Point.fromLngLat(flightpath.from.longitude, flightpath.from.latitude));
        }
        // Add last flightpath ending location to the `points` array
        if (!flightpaths.isEmpty()) {
            Flightpath last = flightpaths.get(flightpaths.size() - 1);
            points.add(Point.fromLngLat(last.to.longitude, last.to.latitude));
        }

        // Create LineString from list of points
        LineString lineString = LineString.fromLngLats(points);

        // Create FeatureCollection from LineString
        FeatureCollection featureCollection = FeatureCollection.fromFeature(Feature.fromGeometry(lineString));

        // Generate FeatureCollection JSON
        String featureCollectionJson = featureCollection.toJson();

        // Write to drone output file
        try {
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            FileWriter writer = new FileWriter("drone-" + formatter.format(date) + ".geojson");
            writer.write(featureCollectionJson);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
