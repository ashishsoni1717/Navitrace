
package org.navitrace.geocoder;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;

public class GeocodeFarmGeocoder extends JsonGeocoder {

    private static String formatUrl(String key, String language) {
        String url = "https://www.geocode.farm/v3/json/reverse/";
        url += "?lat=%f&lon=%f&country=us&count=1";
        if (key != null) {
            url += "&key=" + key;
        }
        if (language != null) {
            url += "&lang=" + language;
        }
        return url;
    }
    public GeocodeFarmGeocoder(
            Client client, String key, String language, int cacheSize, AddressFormat addressFormat) {
        super(client, formatUrl(key, language), cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        Address address = new Address();

        JsonObject result = json
                .getJsonObject("geocoding_results")
                .getJsonArray("RESULTS")
                .getJsonObject(0);

        JsonObject resultAddress = result.getJsonObject("ADDRESS");

        if (result.containsKey("formatted_address")) {
            address.setFormattedAddress(result.getString("formatted_address"));
        }
        if (resultAddress.containsKey("street_number")) {
            address.setStreet(resultAddress.getString("street_number"));
        }
        if (resultAddress.containsKey("street_name")) {
            address.setStreet(resultAddress.getString("street_name"));
        }
        if (resultAddress.containsKey("locality")) {
            address.setSettlement(resultAddress.getString("locality"));
        }
        if (resultAddress.containsKey("admin_1")) {
            address.setState(resultAddress.getString("admin_1"));
        }
        if (resultAddress.containsKey("country")) {
            address.setCountry(resultAddress.getString("country"));
        }

        return address;
    }

}