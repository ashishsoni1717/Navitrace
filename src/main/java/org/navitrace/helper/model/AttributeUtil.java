
package org.navitrace.helper.model;

import org.navitrace.api.security.PermissionsService;
import org.navitrace.config.Config;
import org.navitrace.config.ConfigKey;
import org.navitrace.config.KeyType;
import org.navitrace.config.Keys;
import org.navitrace.model.Device;
import org.navitrace.model.Group;
import org.navitrace.model.Server;
import org.navitrace.session.cache.CacheManager;
import org.navitrace.storage.Storage;
import org.navitrace.storage.StorageException;
import org.navitrace.storage.query.Columns;
import org.navitrace.storage.query.Condition;
import org.navitrace.storage.query.Request;

public final class AttributeUtil {

    private AttributeUtil() {
    }

    public interface Provider {
        Device getDevice();
        Group getGroup(long groupId);
        Server getServer();
        Config getConfig();
    }

    public static <T> T lookup(CacheManager cacheManager, ConfigKey<T> key, long deviceId) {
        return lookup(new CacheProvider(cacheManager, deviceId), key);
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    public static <T> T lookup(Provider provider, ConfigKey<T> key) {
        Device device = provider.getDevice();
        Object result = device.getAttributes().get(key.getKey());
        long groupId = device.getGroupId();
        while (result == null && groupId > 0) {
            Group group = provider.getGroup(groupId);
            if (group != null) {
                result = group.getAttributes().get(key.getKey());
                groupId = group.getGroupId();
            } else {
                groupId = 0;
            }
        }
        if (result == null && key.hasType(KeyType.SERVER)) {
            result = provider.getServer().getAttributes().get(key.getKey());
        }
        if (result == null && key.hasType(KeyType.CONFIG)) {
            result = provider.getConfig().getString(key.getKey());
        }

        if (result != null) {
            Class<T> valueClass = key.getValueClass();
            if (valueClass.equals(Boolean.class)) {
                return (T) (result instanceof String stringResult
                        ? Boolean.parseBoolean(stringResult)
                        : result);
            } else if (valueClass.equals(Integer.class)) {
                return (T) (Object) (result instanceof String stringResult
                        ? Integer.parseInt(stringResult)
                        : ((Number) result).intValue());
            } else if (valueClass.equals(Long.class)) {
                return (T) (Object) (result instanceof String stringResult
                        ? Long.parseLong(stringResult)
                        : ((Number) result).longValue());
            } else if (valueClass.equals(Double.class)) {
                return (T) (Object) (result instanceof String stringResult
                        ? Double.parseDouble(stringResult)
                        : ((Number) result).doubleValue());
            } else {
                return (T) result;
            }
        }
        return key.getDefaultValue();
    }

    public static String getDevicePassword(
            CacheManager cacheManager, long deviceId, String protocol, String defaultPassword) {

        String password = lookup(cacheManager, Keys.DEVICE_PASSWORD, deviceId);
        if (password != null) {
            return password;
        }

        if (protocol != null) {
            password = cacheManager.getConfig().getString(Keys.PROTOCOL_DEVICE_PASSWORD.withPrefix(protocol));
            if (password != null) {
                return password;
            }
        }

        return defaultPassword;
    }

    public static class CacheProvider implements Provider {

        private final CacheManager cacheManager;
        private final long deviceId;

        public CacheProvider(CacheManager cacheManager, long deviceId) {
            this.cacheManager = cacheManager;
            this.deviceId = deviceId;
        }

        @Override
        public Device getDevice() {
            return cacheManager.getObject(Device.class, deviceId);
        }

        @Override
        public Group getGroup(long groupId) {
            return cacheManager.getObject(Group.class, groupId);
        }

        @Override
        public Server getServer() {
            return cacheManager.getServer();
        }

        @Override
        public Config getConfig() {
            return cacheManager.getConfig();
        }
    }

    public static class StorageProvider implements Provider {

        private final Config config;
        private final Storage storage;
        private final PermissionsService permissionsService;
        private final Device device;

        public StorageProvider(Config config, Storage storage, PermissionsService permissionsService, Device device) {
            this.config = config;
            this.storage = storage;
            this.permissionsService = permissionsService;
            this.device = device;
        }

        @Override
        public Device getDevice() {
            return device;
        }

        @Override
        public Group getGroup(long groupId) {
            try {
                return storage.getObject(
                        Group.class, new Request(new Columns.All(), new Condition.Equals("id", groupId)));
            } catch (StorageException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Server getServer() {
            try {
                return permissionsService.getServer();
            } catch (StorageException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Config getConfig() {
            return config;
        }
    }

}
