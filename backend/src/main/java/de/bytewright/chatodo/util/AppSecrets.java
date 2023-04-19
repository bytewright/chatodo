package de.bytewright.chatodo.util;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class AppSecrets implements HealthIndicator, InitializingBean {
    private static final String FILENAME = "app.secret";
    private static final String HEALTHKEY = "test.key";
    private static final String HEALTHKEYVALUE = "testkey";
    private static final String CALDAV_URL = "caldav.url";
    private static final String CALDAV_USER = "caldav.user";
    private static final String CALDAV_USERPW = "caldav.userpw";
    private final Properties props = new Properties();

    public AppSecrets() {
    }

    public void init() {
        Path path = Path.of(FILENAME);
        if (!Files.isReadable(path)) {
            throw new IllegalStateException("Can't read secrets file at " + path.toAbsolutePath());
        }
        // Load the secretfile from the current directory
        try (FileInputStream fis = new FileInputStream(FILENAME)) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get the values of some secrets
        String property = props.getProperty(HEALTHKEY);
        if (!HEALTHKEYVALUE.equals(property)) {
            throw new IllegalStateException("secretfile does not contain healthkey");
        }
    }

    public ConnectionSettings getCaldavConnectionSettings() {
        return new ConnectionSettings(props.getProperty(CALDAV_URL),
                props.getProperty(CALDAV_USER),
                props.getProperty(CALDAV_USERPW));
    }

    @Override
    public Health health() {
        if (props.isEmpty()) {
            return Health.down().withDetail("secretfile", "empty or not found").build();
        }
        return Health.up().build();

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
