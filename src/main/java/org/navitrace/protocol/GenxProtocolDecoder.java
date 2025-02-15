
package org.navitrace.protocol;

import io.netty.channel.Channel;
import org.navitrace.BaseProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.helper.UnitsConverter;
import org.navitrace.model.Position;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;

public class GenxProtocolDecoder extends BaseProtocolDecoder {

    private int[] reportColumns;

    public GenxProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected void init() {
        setReportColumns(getConfig().getString(getProtocolName() + ".reportColumns", "1,2,3,4"));
    }

    public void setReportColumns(String format) {
        String[] columns = format.split(",");
        reportColumns = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            reportColumns[i] = Integer.parseInt(columns[i]);
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String[] values = ((String) msg).split(",");

        Position position = new Position(getProtocolName());
        position.setValid(true);

        for (int i = 0; i < Math.min(values.length, reportColumns.length); i++) {
            switch (reportColumns[i]) {
                case 1, 28 -> {
                    DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, values[i]);
                    if (deviceSession != null) {
                        position.setDeviceId(deviceSession.getDeviceId());
                    }
                }
                case 2 -> position.setTime(new SimpleDateFormat("MM/dd/yy HH:mm:ss").parse(values[i]));
                case 3 -> position.setLatitude(Double.parseDouble(values[i]));
                case 4 -> position.setLongitude(Double.parseDouble(values[i]));
                case 11 -> position.set(Position.KEY_IGNITION, values[i].equals("ON"));
                case 13 -> position.setSpeed(UnitsConverter.knotsFromKph(Integer.parseInt(values[i])));
                case 17 -> position.setCourse(Integer.parseInt(values[i]));
                case 23 -> position.set(Position.KEY_ODOMETER, Double.parseDouble(values[i]) * 1000);
                case 27 -> position.setAltitude(UnitsConverter.metersFromFeet(Integer.parseInt(values[i])));
                case 46 -> position.set(Position.KEY_SATELLITES, Integer.parseInt(values[i]));
            }
        }

        return position.getDeviceId() != 0 ? position : null;
    }

}
