package uk.ac.ed.inf;

public class LongLat {
    // The longitude and latitude variables stored
    public double longitude;
    public double latitude;

    /**
     * Creates a new LongLat object with the specified coordinates
     * @param longitude the longitude of the coordinate
     * @param latitude the latitude of the coordinate
     */
    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Checks if this coordinate is within the drone confinement area
     * @return true if the latitude and longitude are within the bounds of the drone confinement area
     */
    public boolean isConfined() {
        return longitude > Constant.MIN_LONG && longitude < Constant.MAX_LONG && latitude > Constant.MIN_LAT && latitude < Constant.MAX_LAT;
    }

    /**
     * Calculates the distance between this coordinate and a provided location
     * @param location coordinate to calculate distance to
     * @return the distance between the two points
     */
    public double distanceTo(LongLat location) {
        return Math.sqrt(Math.pow(latitude - location.latitude, 2) + Math.pow(longitude - location.longitude, 2));
    }

    /**
     * Returns true if the distance between this coordinate and the provided location is less
     * than MOVE_AMOUNT degrees
     * @param location coordinate to check if close to
     * @return true if the provided location is close to this position
     */
    public boolean closeTo(LongLat location) {
        return distanceTo(location) < Constant.MOVE_AMOUNT;
    }

    /**
     * Calculates the new position after moving MOVE_AMOUNT degrees in the direction of the specified angle
     * @param angle Must be a multiple of 10, 0 for east, 90 for north, 180 for west, 270 for south or other
     *              angle in between. If angle is -999, returns the same position
     * @return the next calculated position
     */
    public LongLat nextPosition(int angle) {
        // On throwaway value of -999, return self (for hover)
        if (angle == -999) {
            return this;
        }
        if (angle % 10 != 0) {
            throw new IllegalArgumentException("Angle must be a multiple of 10");
        }

        double theta = Math.toRadians(angle);
        double deltaLong = Math.cos(theta) * Constant.MOVE_AMOUNT;
        double deltaLat = Math.sin(theta) * Constant.MOVE_AMOUNT;
        return new LongLat(longitude + deltaLong, latitude + deltaLat);
    }

    /**
     * Calculate angle from this to another coordinate
     * @param location target
     * @return the angle (multiple of 10) from this location to the target location
     */
    public int angleTo(LongLat location) {
        double deltaLong = location.longitude - this.longitude;
        double deltaLat = location.latitude - this.latitude;
        double theta = Math.atan2(deltaLat, deltaLong);

        double angle = Math.toDegrees(theta);
        if (angle < 0) {
            // Angle must be positive
            angle = 360 - Math.abs(angle);
        }
        // Round to nearest 10 degrees
        return (int)(Math.round(angle / 10) * 10);
    }
}
