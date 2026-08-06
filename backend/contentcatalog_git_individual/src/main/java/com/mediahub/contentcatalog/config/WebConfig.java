package com.mediahub.contentcatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${mediahub.assets.root}")
    private String assetsRoot;

    // Serves local demo Video/Image/Article files for the Content Catalog's preview panel.
    // Public (see SecurityConfig) since a plain <video>/<img> tag never carries a JWT header.
    // assetsRoot may be relative (repo-committed demo-assets/, resolved against the JVM's
    // working directory) or an absolute path (e.g. an external C:/MediaHubAssets/ override) —
    // "file:///" only makes sense for the latter, so branch on which one we were given.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = assetsRoot.replace("\\", "/");
        if (!location.endsWith("/")) {
            location += "/";
        }
        boolean isAbsolute = location.matches("^[A-Za-z]:/.*") || location.startsWith("/");
        String resourceLocation = isAbsolute ? "file:///" + location : "file:" + location;
        registry.addResourceHandler("/media/**")
                .addResourceLocations(resourceLocation);
    }
}
