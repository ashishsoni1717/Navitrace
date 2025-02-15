
package org.navitrace.geofence;

import java.text.ParseException;
import java.util.ArrayList;

import org.navitrace.config.Config;
import org.navitrace.config.Keys;
import org.navitrace.helper.DistanceCalculator;
import org.navitrace.model.Geofence;

public class GeofencePolyline extends GeofenceGeometry {

    private ArrayList<Coordinate> coordinates;

    public GeofencePolyline() {
    }

    public GeofencePolyline(String wkt) throws ParseException {
        fromWkt(wkt);
    }

    @Override
    public boolean containsPoint(Config config, Geofence geofence, double latitude, double longitude) {
        double distance = geofence.getDouble("polylineDistance");
        if (distance == 0) {
            distance = config.getDouble(Keys.GEOFENCE_POLYLINE_DISTANCE);
        }
        for (int i = 1; i < coordinates.size(); i++) {
            if (DistanceCalculator.distanceToLine(
                    latitude, longitude, coordinates.get(i - 1).getLat(), coordinates.get(i - 1).getLon(),
                    coordinates.get(i).getLat(), coordinates.get(i).getLon()) <= distance) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double calculateArea() {
        return 0;
    }

    @Override
    public String toWkt() {
        StringBuilder buf = new StringBuilder();
        buf.append("LINESTRING (");
        for (Coordinate coordinate : coordinates) {
            buf.append(coordinate.getLat());
            buf.append(" ");
            buf.append(coordinate.getLon());
            buf.append(", ");
        }
        return buf.substring(0, buf.length() - 2) + ")";
    }

    @Override
    public void fromWkt(String wkt) throws ParseException {
        if (coordinates == null) {
            coordinates = new ArrayList<>();
        } else {
            coordinates.clear();
        }

        if (!wkt.startsWith("LINESTRING")) {
            throw new ParseException("Mismatch geometry type", 0);
        }
        String content = wkt.substring(wkt.indexOf("(") + 1, wkt.indexOf(")"));
        if (content.isEmpty()) {
            throw new ParseException("No content", 0);
        }
        String[] commaTokens = content.split(",");
        if (commaTokens.length < 2) {
            throw new ParseException("Not valid content", 0);
        }

        for (String commaToken : commaTokens) {
            String[] tokens = commaToken.trim().split("\\s");
            if (tokens.length != 2) {
                throw new ParseException("Here must be two coordinates: " + commaToken, 0);
            }
            Coordinate coordinate = new Coordinate();
            try {
                coordinate.setLat(Double.parseDouble(tokens[0]));
            } catch (NumberFormatException e) {
                throw new ParseException(tokens[0] + " is not a double", 0);
            }
            try {
                coordinate.setLon(Double.parseDouble(tokens[1]));
            } catch (NumberFormatException e) {
                throw new ParseException(tokens[1] + " is not a double", 0);
            }
            coordinates.add(coordinate);
        }

    }

}
