
package org.navitrace.handler.events;

import jakarta.inject.Inject;
import org.navitrace.config.Keys;
import org.navitrace.helper.model.AttributeUtil;
import org.navitrace.helper.model.PositionUtil;
import org.navitrace.model.Device;
import org.navitrace.model.Event;
import org.navitrace.model.Position;
import org.navitrace.session.cache.CacheManager;

public class FuelEventHandler extends BaseEventHandler {

    private final CacheManager cacheManager;

    @Inject
    public FuelEventHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void analyzePosition(Position position, Callback callback) {

        Device device = cacheManager.getObject(Device.class, position.getDeviceId());
        if (device == null) {
            return;
        }
        if (!PositionUtil.isLatest(cacheManager, position)) {
            return;
        }

        if (position.hasAttribute(Position.KEY_FUEL_LEVEL)) {
            Position lastPosition = cacheManager.getPosition(position.getDeviceId());
            if (lastPosition != null && lastPosition.hasAttribute(Position.KEY_FUEL_LEVEL)) {
                double before = lastPosition.getDouble(Position.KEY_FUEL_LEVEL);
                double after = position.getDouble(Position.KEY_FUEL_LEVEL);
                double change = after - before;

                if (change > 0) {
                    double threshold = AttributeUtil.lookup(
                            cacheManager, Keys.EVENT_FUEL_INCREASE_THRESHOLD, position.getDeviceId());
                    if (threshold > 0 && change >= threshold) {
                        callback.eventDetected(new Event(Event.TYPE_DEVICE_FUEL_INCREASE, position));
                    }
                } else if (change < 0) {
                    double threshold = AttributeUtil.lookup(
                            cacheManager, Keys.EVENT_FUEL_DROP_THRESHOLD, position.getDeviceId());
                    if (threshold > 0 && Math.abs(change) >= threshold) {
                        callback.eventDetected(new Event(Event.TYPE_DEVICE_FUEL_DROP, position));
                    }
                }
            }
        }
    }

}
