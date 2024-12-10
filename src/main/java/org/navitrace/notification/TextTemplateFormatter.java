
package org.navitrace.notification;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.navitrace.api.signature.TokenManager;
import org.navitrace.helper.model.UserUtil;
import org.navitrace.model.Server;
import org.navitrace.model.User;
import org.navitrace.storage.StorageException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Locale;

@Singleton
public class TextTemplateFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextTemplateFormatter.class);

    private final VelocityEngine velocityEngine;
    private final TokenManager tokenManager;

    @Inject
    public TextTemplateFormatter(VelocityEngine velocityEngine, TokenManager tokenManager) {
        this.velocityEngine = velocityEngine;
        this.tokenManager = tokenManager;
    }

    public VelocityContext prepareContext(Server server, User user) {

        VelocityContext velocityContext = new VelocityContext();

        if (user != null) {
            velocityContext.put("user", user);
            velocityContext.put("timezone", UserUtil.getTimezone(server, user));
            try {
                velocityContext.put("token", tokenManager.generateToken(user.getId()));
            } catch (IOException | GeneralSecurityException | StorageException e) {
                LOGGER.warn("Token generation failed", e);
            }
        }

        velocityContext.put("webUrl", velocityEngine.getProperty("web.url"));
        velocityContext.put("dateTool", new DateTool());
        velocityContext.put("numberTool", new NumberTool());
        velocityContext.put("locale", Locale.getDefault());

        return velocityContext;
    }

    public Template getTemplate(String name, String path) {
        String templateFilePath = Paths.get(path, name + ".vm").toString();
        return velocityEngine.getTemplate(templateFilePath, StandardCharsets.UTF_8.name());
    }

    public NotificationMessage formatMessage(VelocityContext velocityContext, String name, String templatePath) {
        StringWriter writer = new StringWriter();
        getTemplate(name, templatePath).merge(velocityContext, writer);
        return new NotificationMessage((String) velocityContext.get("subject"), writer.toString());
    }

}