package com.biotechpay.lab.config;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Opens the default web browser at the game URL once the embedded server is up
 * and listening. Fires on {@link ApplicationReadyEvent}, so the port is already
 * bound by the time we navigate to it.
 *
 * Disable with {@code app.open-browser=false} (set automatically as a no-op in
 * headless environments such as CI or a server with no desktop session).
 *
 * <p>{@code @ConditionalOnWebApplication(SERVLET)} keeps this bean out of test contexts
 * (e.g. {@code @SpringBootTest} without a real embedded server, which has no
 * {@link WebServerApplicationContext} to inject) without needing per-test exclusions.
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class BrowserLauncher {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    private final WebServerApplicationContext webServerApplicationContext;
    private final boolean openBrowser;

    public BrowserLauncher(
            WebServerApplicationContext webServerApplicationContext,
            @Value("${app.open-browser:true}") boolean openBrowser) {
        this.webServerApplicationContext = webServerApplicationContext;
        this.openBrowser = openBrowser;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void launchOnStartup() {
        int port = webServerApplicationContext.getWebServer().getPort();
        final String url = "http://localhost:" + port + "/";
        log.info("Game is live at {}", url);

        if (!openBrowser) {
            log.info("Auto-open disabled (app.open-browser=false). Open {} manually.", url);
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            log.info("Headless environment; not opening a browser. Open {} manually.", url);
            return;
        }

        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                log.info("Opened the default browser at {}", url);
                return;
            }
        } catch (Exception e) {
            log.warn("Desktop browse failed ({}); falling back to an OS command.", e.getMessage());
        }

        openWithOsCommand(url);
    }

    private void openWithOsCommand(String url) {
        final String os = System.getProperty("os.name", "").toLowerCase();
        try {
            final ProcessBuilder pb;
            if (os.contains("win")) {
                // Empty title argument prevents "start" from treating the URL as a window title.
                pb = new ProcessBuilder("cmd", "/c", "start", "", url);
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", url);
            } else {
                pb = new ProcessBuilder("xdg-open", url);
            }
            pb.start();
            log.info("Requested the OS to open {}", url);
        } catch (Exception e) {
            log.warn("Could not auto-open a browser ({}). Open {} manually.", e.getMessage(), url);
        }
    }
}
