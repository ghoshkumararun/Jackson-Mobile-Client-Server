/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zur.java_jackson_servlet;

/**
 *
 * @author Matt Roth
 */
public class JacksonConfig {

    private CustomPackageMappingConfig customPackageMappingConfigs;
    private static JacksonConfig config;

    private JacksonConfig() {
        //no public create
    }

    public static synchronized JacksonConfig getInstance() {
        if (config == null) {
            config = new JacksonConfig();
        }
        return config;
    }

    public void addCustomPackageMappingConfig(CustomPackageMappingConfig customPackageMappingConfig) {
        setCustomPackageMappingConfig(customPackageMappingConfig);
    }

    public CustomPackageMappingConfig getCustomPackageMappingConfig() {
        return customPackageMappingConfigs;
    }

    public void setCustomPackageMappingConfig(CustomPackageMappingConfig customPackageMappingConfigs) {
        this.customPackageMappingConfigs = customPackageMappingConfigs;
    }
}
