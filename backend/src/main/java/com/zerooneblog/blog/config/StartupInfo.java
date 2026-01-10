package com.zerooneblog.blog.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupInfo implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupInfo.class);
    private final Environment env;

    public StartupInfo(Environment env) {
        this.env = env;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!(event.getApplicationContext() instanceof WebServerApplicationContext)) {
            // Non-web or mock web context (e.g., tests) — skip startup info
            return;
        }
        WebServerApplicationContext ctx = (WebServerApplicationContext) event.getApplicationContext();
        int port = ctx.getWebServer().getPort();
        String contextPath = env.getProperty("server.servlet.context-path", "");
        if (contextPath == null || contextPath.isBlank()) contextPath = "/";

        String host = "localhost";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            host = addr.getHostAddress();
        } catch (UnknownHostException e) {
            // fallback to localhost
        }

        String base = String.format("http://%s:%d%s", host, port, contextPath.equals("/") ? "" : contextPath);
        String apiBase = base.endsWith("/") ? base + "api/v1" : base + "/api/v1";

        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("----------------------------------------------------------").append(System.lineSeparator());
    sb.append("  Application '").append(env.getProperty("spring.application.name", "blog")).append("' is running!").append(System.lineSeparator());
        sb.append("  Local:      ").append(base).append(System.lineSeparator());
        sb.append("  API base:   ").append(apiBase).append(System.lineSeparator());
        sb.append("----------------------------------------------------------").append(System.lineSeparator());

        // Avoid printing the same block multiple times (DevTools restart can trigger ApplicationReadyEvent twice).
        // Create a marker file in the system temp directory and only print when the file is successfully created.
        // This is robust across restarts and classloader boundaries.
        String appName = env.getProperty("spring.application.name", "blog").replaceAll("\\s+", "_");
        String marker = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + ".startupInfoPrinted-" + appName;
        Path markerPath = Paths.get(marker);
        try {
            // If this call succeeds, this JVM/context is the first to create the marker — print the info.
            Files.createFile(markerPath);
            log.info(sb.toString());
        } catch (FileAlreadyExistsException faee) {
            // marker already exists — another context already printed, so skip
        } catch (IOException ioe) {
            // If we can't create or check the marker for some reason, fallback to logging once (best-effort)
            log.info(sb.toString());
        }
    }
}
