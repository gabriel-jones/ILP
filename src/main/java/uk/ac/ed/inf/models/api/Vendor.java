package uk.ac.ed.inf.models.api;

import uk.ac.ed.inf.LongLat;

import java.util.ArrayList;

public class Vendor {
    String name;
    String location;
    LongLat coordinates;
    ArrayList<MenuItem> menu;

    public String getName() { return name; }
    public String getLocation() { return location; }
    public LongLat getCoordinates() { return coordinates; }
    public ArrayList<MenuItem> getMenu() { return menu; }

    public void setCoordinates(LongLat coords) {
        this.coordinates = coords;
    }
}
