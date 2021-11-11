package uk.ac.ed.inf;

public class Constant {
    // Drone start position
    public final static LongLat APPLETON_TOWER = new LongLat(-3.186874, 55.944494);

    // One move (degrees)
    public final static double MOVE_AMOUNT = 0.00015;

    // Max moves for drone in one day
    public final static int MAX_MOVES = 1500;

    // Bounds of the drone confinement area
    public final static double MIN_LONG = -3.192473;
    public final static double MAX_LONG = -3.184319;
    public final static double MIN_LAT = 55.942617;
    public final static double MAX_LAT = 55.946233;
}
