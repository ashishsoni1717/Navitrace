
package org.navitrace.forward;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.navitrace.config.Config;
import org.navitrace.config.Keys;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class PositionForwarderJson implements PositionForwarder {

    private final String url;
    private final String header;

    private final Client client;
    private final ObjectMapper objectMapper;

    public PositionForwarderJson(Config config, Client client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.url = config.getString(Keys.FORWARD_URL);
        this.header = config.getString(Keys.FORWARD_HEADER);
    }

    @Override
    public void forward(PositionData positionData, ResultHandler resultHandler) {
        var requestBuilder = client.target(url).request();

        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
        if (header != null && !header.isEmpty()) {
            for (String line: header.split("\\r?\\n")) {
                String[] values = line.split(":", 2);
                String headerName = values[0].trim();
                String headerValue = values[1].trim();
                if (headerName.equals(HttpHeaders.CONTENT_TYPE)) {
                    mediaType = MediaType.valueOf(headerValue);
                } else {
                    requestBuilder.header(headerName, headerValue);
                }
            }
        }

        try {
            var entity = Entity.entity(objectMapper.writeValueAsString(positionData), mediaType);
            requestBuilder.async().post(entity, new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                        resultHandler.onResult(true, null);
                    } else {
                        int code = response.getStatusInfo().getStatusCode();
                        resultHandler.onResult(false, new RuntimeException("HTTP code " + code));
                    }
                }

                @Override
                public void failed(Throwable throwable) {
                    resultHandler.onResult(false, throwable);
                }
            });
        } catch (JsonProcessingException e) {
            resultHandler.onResult(false, e);
        }
    }

}
