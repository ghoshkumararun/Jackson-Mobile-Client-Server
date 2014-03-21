package com.zur.jackson_server;


import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.digester3.Digester;
import org.xml.sax.SAXException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Matt Roth
 */
public class ConfigDigester extends Digester {

    private static final String CUSTOM_PACKAGE_MAPPING_PATH = "config/custom-package-mapping";

    protected boolean configured = false;

    @Override
    public Object parse(InputStream input) throws IOException, SAXException {
        configure();
        return (super.parse(input));

    }

    @Override
    protected void configure() {
        if (configured) {
            return;
        }
        addCustomPackageMappingRules();

        // Mark this digester as having been configured
        configured = true;
    }

    private void addCustomPackageMappingRules() {
        addObjectCreate(
            CUSTOM_PACKAGE_MAPPING_PATH,
            CustomPackageMappingConfig.class);
        addSetNext(
            CUSTOM_PACKAGE_MAPPING_PATH,
            "addCustomPackageMappingConfig",
            "CustomPackageMappingConfig");
        addCallMethod(
            CUSTOM_PACKAGE_MAPPING_PATH + "/server-package",
            "setServerPackageName",
            0);
        addCallMethod(
            CUSTOM_PACKAGE_MAPPING_PATH + "/client-package",
            "setClientPackageName",
            0);
    }
}
