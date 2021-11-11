package uk.ac.ed.inf;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uk.ac.ed.inf.models.api.MenuItem;
import uk.ac.ed.inf.models.api.Vendor;

public class Menus {
    // The list of vendors on the server
    ArrayList<Vendor> vendors;

    /**
     * Creates a new Menus object and fetches the menu data from the server
     * @param client the HTTP client to use for fetching resources from the server
     */
    public Menus(ServerClient client) {
        // Fetch the menu from the server
        HttpResponse<String> json = client.httpGet("/menus/menus.json");

        if (json != null) {
            // Cast response JSON string to list of Vendor objects
            Type listType = new TypeToken<ArrayList<Vendor>>() {}.getType();
            this.vendors = new Gson().fromJson(json.body(), listType);
        }
    }

    /**
     * Searches the menu for the price of a specified item name
     * @param itemName the name of the menu item to search for
     * @return the price (pence) of the item, or 0 if no item is found with that name
     */
    int getPrice(String itemName) {
        for (Vendor vendor : vendors) {
            for (MenuItem item : vendor.getMenu()) {
                if (item.getItem().equals(itemName)) {
                    return item.getPence();
                }
            }
        }
        return 0;
    }

    /**
     * Finds a vendor from a given item name
     * @param itemName the menu item name to search for
     * @return the Vendor that serves this item
     */
    public Vendor getVendor(String itemName) {
        for (Vendor vendor : vendors) {
            for (MenuItem item : vendor.getMenu()) {
                if (item.getItem().equals(itemName)) {
                    return vendor;
                }
            }
        }
        return null;
    }

    /**
     * Calculates the total delivery cost of all item names specified
     * @param items variable number of string item names
     * @return the price of delivery of all the items
     */
    public int getDeliveryCost(String... items) {
        int cost = 50; // pence (delivery fee)
        for (String item : items) {
            cost += getPrice(item);
        }
        return cost;
    }
}
