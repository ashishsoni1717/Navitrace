
package org.navitrace.protocol;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.navitrace.BaseHttpProtocolDecoder;
import org.navitrace.session.DeviceSession;
import org.navitrace.Protocol;
import org.navitrace.model.Position;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.SocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MoovboxProtocolDecoder extends BaseHttpProtocolDecoder {

    private final DocumentBuilder documentBuilder;
    private final XPath xPath;
    private final XPathExpression messageExpression;

    public MoovboxProtocolDecoder(Protocol protocol) {
        super(protocol);
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            builderFactory.setXIncludeAware(false);
            builderFactory.setExpandEntityReferences(false);
            documentBuilder = builderFactory.newDocumentBuilder();
            xPath = XPathFactory.newInstance().newXPath();
            messageExpression = xPath.compile("//gps/coordinates/coordinate");
        } catch (ParserConfigurationException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;

        Document document = documentBuilder.parse(new ByteBufferBackedInputStream(request.content().nioBuffer()));

        String id = document.getDocumentElement().getAttribute("id");
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        NodeList nodes = (NodeList) messageExpression.evaluate(document, XPathConstants.NODESET);
        List<Position> positions = new LinkedList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.setValid(true);
            position.setTime(new Date(Long.parseLong(xPath.evaluate("time", node)) * 1000));
            position.setLatitude(Double.parseDouble(xPath.evaluate("longitude", node)));
            position.setLongitude(Double.parseDouble(xPath.evaluate("latitude", node)));
            position.setAltitude(Double.parseDouble(xPath.evaluate("altitude", node)));
            position.setSpeed(Double.parseDouble(xPath.evaluate("speed", node)));

            position.set(Position.KEY_SATELLITES, Integer.parseInt(xPath.evaluate("satellites", node)));

            positions.add(position);
        }

        sendResponse(channel, HttpResponseStatus.OK);
        return positions;
    }

}
