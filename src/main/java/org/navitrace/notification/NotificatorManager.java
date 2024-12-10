
package org.navitrace.notification;

import com.google.inject.Injector;
import org.navitrace.config.Config;
import org.navitrace.config.Keys;
import org.navitrace.model.Typed;
import org.navitrace.notificators.Notificator;
import org.navitrace.notificators.NotificatorCommand;
import org.navitrace.notificators.NotificatorFirebase;
import org.navitrace.notificators.NotificatorMail;
import org.navitrace.notificators.NotificatorPushover;
import org.navitrace.notificators.NotificatorSms;
import org.navitrace.notificators.NotificatorTelegram;
import org.navitrace.notificators.NotificatorTraccar;
import org.navitrace.notificators.NotificatorWeb;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class NotificatorManager {

    private static final Map<String, Class<? extends Notificator>> NOTIFICATORS_ALL = Map.of(
            "command", NotificatorCommand.class,
            "web", NotificatorWeb.class,
            "mail", NotificatorMail.class,
            "sms", NotificatorSms.class,
            "firebase", NotificatorFirebase.class,
            "traccar", NotificatorTraccar.class,
            "telegram", NotificatorTelegram.class,
            "pushover", NotificatorPushover.class);

    private final Injector injector;

    private final Set<String> types = new HashSet<>();

    @Inject
    public NotificatorManager(Injector injector, Config config) {
        this.injector = injector;
        String types = config.getString(Keys.NOTIFICATOR_TYPES);
        if (types != null) {
            this.types.addAll(Arrays.asList(types.split(",")));
        }
    }

    public Notificator getNotificator(String type) {
        var clazz = NOTIFICATORS_ALL.get(type);
        if (clazz != null && types.contains(type)) {
            var notificator = injector.getInstance(clazz);
            if (notificator != null) {
                return notificator;
            }
        }
        throw new RuntimeException("Failed to get notificator " + type);
    }

    public Set<Typed> getAllNotificatorTypes() {
        return types.stream().map(Typed::new).collect(Collectors.toUnmodifiableSet());
    }

}
