
package org.navitrace.protocol;

import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.CellTower;
import org.navitrace.model.Network;
import org.navitrace.model.Position;
import org.navitrace.model.WifiAccessPoint;

import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class S168ProtocolDecoder extends BaseProtocolDecoder {

    public S168ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        String[] values = sentence.split("#");

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, values[1]);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        Network network = new Network();

        String content = values[4];
        String[] fragments = content.split(";");
        for (String fragment : fragments) {
            if (fragment.isEmpty()) {
                continue;
            }

            int dataIndex = fragment.indexOf(':');
            String type = fragment.substring(0, dataIndex);
            values = fragment.substring(dataIndex + 1).split(",");
            int index = 0;

            switch (type) {
                case "GDATA":
                    position.setValid(values[index++].equals("A"));
                    position.set(Position.KEY_SATELLITES, Integer.parseInt(values[index++]));
                    DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    position.setTime(dateFormat.parse(values[index++]));
                    position.setLatitude(Double.parseDouble(values[index++]));
                    position.setLongitude(Double.parseDouble(values[index++]));
                    position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
                    position.setCourse(Integer.parseInt(values[index++]));
                    position.setAltitude(Integer.parseInt(values[index++]));
                    break;
                case "CELL":
                    int cellCount = Integer.parseInt(values[index++]);
                    int mcc = Integer.parseInt(values[index++], 16);
                    int mnc = Integer.parseInt(values[index++], 16);
                    for (int i = 0; i < cellCount; i++) {
                        network.addCellTower(CellTower.from(
                                mcc, mnc, Integer.parseInt(values[index++], 16), Integer.parseInt(values[index++], 16),
                                Integer.parseInt(values[index++], 16)));
                    }
                    break;
                case "WIFI":
                    int wifiCount = Integer.parseInt(values[index++]);
                    for (int i = 0; i < wifiCount; i++) {
                        network.addWifiAccessPoint(WifiAccessPoint.from(
                                values[index++].replace('-', ':'), Integer.parseInt(values[index++])));
                    }
                    break;
                case "STATUS":
                    position.set(Position.KEY_BATTERY_LEVEL, Integer.parseInt(values[index++]));
                    position.set(Position.KEY_RSSI, Integer.parseInt(values[index++]));
                    break;
                default:
                    break;
            }
        }

        if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
            position.setNetwork(network);
        }
        if (!position.hasAttribute(Position.KEY_SATELLITES)) {
            getLastLocation(position, null);
        }

        if (position.getNetwork() != null || !position.getAttributes().isEmpty()) {
            return position;
        } else {
            return null;
        }
    }

}
