package com.campusportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String[] possiblePublicPaths = {
            "../public",
            "./public",
            "./campusPortal-main/campusPortal-main/public",
            "../campusPortal-main/campusPortal-main/public"
        };

        for (String pathStr : possiblePublicPaths) {
            Path path = Paths.get(pathStr).toAbsolutePath();
            if (java.nio.file.Files.exists(path)) {
                String location = path.toUri().toString();
                registry.addResourceHandler("/**")
                        .addResourceLocations(location, "classpath:/static/", "classpath:/public/");
                System.out.println("Serving static files from: " + path);
                break;
            }
        }
        
        // Smart Autodiscovery for uploads
        String[] locationsToTry = {
            "uploads/",
            "../uploads/",
            "../../uploads/",
            "./campusPortal-main/campusPortal-main/uploads/",
            "./server/uploads/",
            "../server/uploads/"
        };

        boolean found = false;
        for (String loc : locationsToTry) {
            Path path = Paths.get(loc).toAbsolutePath().normalize();
            if (java.nio.file.Files.exists(path) && java.nio.file.Files.isDirectory(path)) {
                String location = "file:///" + path.toString().replace("\\", "/") + "/";
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(location);
                System.out.println("!!! SERVING UPLOADS FROM: " + location);
                found = true;
                break;
            }
        }
        
        if (!found) {
            System.err.println("!!! WARNING: UPLOADS DIRECTORY NOT FOUND !!!");
        }
    }
}
