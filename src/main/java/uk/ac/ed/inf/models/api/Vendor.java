package uk.ac.ed.inf.models.api;

import uk.ac.ed.inf.LongLat;

import java.util.ArrayList;

public class Vendor {
    // Name of vendor
    private String name;

    // What3Words location of vendor
    private String location;

    // LongLat generated from What3Words location
    private LongLat coordinates;

    // Items served by this vendor
    private ArrayList<MenuItem> menu;

    // Getter for name
    public String getName() { return name; }

    // Getter for location
    public String getLocation() { return location; }

    // Getter for coordinates
    public LongLat getCoordinates() { return coordinates; }

    // Getter for menu
    public ArrayList<MenuItem> getMenu() { return menu; }

    // Setter for coordinates
    public void setCoordinates(LongLat coordinates) {
        this.coordinates = coordinates;
    }
}
