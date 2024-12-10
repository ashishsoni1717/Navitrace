
package org.navitrace.geocoder;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.ws.rs.client.Client;

public class MapboxGeocoder extends JsonGeocoder {

    private static String formatUrl(String key) {
        return "https://api.mapbox.com/geocoding/v5/mapbox.places/%2$f,%1$f.json?access_token=" + key;
    }

    public MapboxGeocoder(Client client, String key, int cacheSize, AddressFormat addressFormat) {
        super(client, formatUrl(key), cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonArray features = json.getJsonArray("features");

        if (!features.isEmpty()) {
            Address address = new Address();

            JsonObject mostSpecificFeature = (JsonObject) features.get(0);

            if (mostSpecificFeature.containsKey("place_name")) {
                address.setFormattedAddress(mostSpecificFeature.getString("place_name"));
            }

            if (mostSpecificFeature.containsKey("address")) {
                address.setHouse(mostSpecificFeature.getString("address"));
            }

            for (JsonObject feature : features.getValuesAs(JsonObject.class)) {

                String value = feature.getString("text");

                typesLoop:
                for (JsonString type : feature.getJsonArray("place_type").getValuesAs(JsonString.class)) {

                    switch (type.getString()) {
                        case "address":
                            address.setStreet(value);
                            break typesLoop;
                        case "neighborhood":
                            address.setSuburb(value);
                            break typesLoop;
                        case "postcode":
                            address.setPostcode(value);
                            break typesLoop;
                        case "locality":
                            address.setSettlement(value);
                            break typesLoop;
                        case "district":
                        case "place":
                            address.setDistrict(value);
                            break typesLoop;
                        case "region":
                            address.setState(value);
                            break typesLoop;
                        case "country":
                            address.setCountry(value);
                            break typesLoop;
                        default:
                            break;
                    }
                }
            }

            return address;
        }
        return null;
    }

}
