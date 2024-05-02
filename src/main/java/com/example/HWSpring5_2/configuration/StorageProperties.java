package com.example.HWSpring5_2.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Здесь задается директория для загрузки
 */
@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location = "upload-dir";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
