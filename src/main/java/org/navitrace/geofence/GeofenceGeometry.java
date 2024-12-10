
package org.navitrace.geofence;

import org.navitrace.config.Config;
import org.navitrace.model.Geofence;

import java.text.ParseException;

public abstract class GeofenceGeometry {

    public abstract boolean containsPoint(Config config, Geofence geofence, double latitude, double longitude);

    public abstract double calculateArea();

    public abstract String toWkt();

    public abstract void fromWkt(String wkt) throws ParseException;

    public static class Coordinate {

        private double lat;
        private double lon;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }
    }

}
