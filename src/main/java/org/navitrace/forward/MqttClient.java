
package org.navitrace.forward;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;

public class MqttClient {

    private final Mqtt5AsyncClient client;

    MqttClient(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Mqtt5SimpleAuth simpleAuth = this.getSimpleAuth(uri);

        String host = uri.getHost();
        int port = uri.getPort();
        Mqtt5ClientBuilder builder = Mqtt5Client.builder().identifier("traccar-" + UUID.randomUUID())
                .serverHost(host).serverPort(port).simpleAuth(simpleAuth).automaticReconnectWithDefaultConfig();

        client = builder.buildAsync();
        client.connectWith().send().whenComplete((message, e) -> {
            throw new RuntimeException(e);
        });
    }

    private Mqtt5SimpleAuth getSimpleAuth(URI uri) {
        String userInfo = uri.getUserInfo();
        Mqtt5SimpleAuth simpleAuth = null;
        if (userInfo != null) {
            int delimiter = userInfo.indexOf(':');
            if (delimiter == -1) {
                throw new IllegalArgumentException("Wrong MQTT credentials. Should be in format \"username:password\"");
            } else {
                simpleAuth = Mqtt5SimpleAuth.builder().username(userInfo.substring(0, delimiter++))
                        .password(userInfo.substring(delimiter).getBytes()).build();
            }
        }
        return simpleAuth;
    }

    public void publish(
            String pubTopic, String payload, BiConsumer<? super Mqtt5PublishResult, ? super Throwable> whenComplete) {
        client.publishWith().topic(pubTopic).qos(MqttQos.AT_LEAST_ONCE).payload(payload.getBytes()).send()
                .whenComplete(whenComplete);
    }

}
