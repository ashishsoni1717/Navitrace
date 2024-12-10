package org.navitrace.handler;

import org.junit.jupiter.api.Test;
import org.navitrace.config.Config;
import org.navitrace.model.Position;
import org.navitrace.session.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class DistanceHandlerTest {

    @Test
    public void testCalculateDistance() {

        DistanceHandler distanceHandler = new DistanceHandler(new Config(), mock(CacheManager.class));

        Position position = new Position();
        distanceHandler.handlePosition(position, p -> {});

        assertEquals(0.0, position.getAttributes().get(Position.KEY_DISTANCE));
        assertEquals(0.0, position.getAttributes().get(Position.KEY_TOTAL_DISTANCE));

        position.set(Position.KEY_DISTANCE, 100);

        distanceHandler.handlePosition(position, p -> {});

        assertEquals(100.0, position.getAttributes().get(Position.KEY_DISTANCE));
        assertEquals(100.0, position.getAttributes().get(Position.KEY_TOTAL_DISTANCE));

    }

}
