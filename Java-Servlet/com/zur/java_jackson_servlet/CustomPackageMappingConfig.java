package com.zur.java_jackson_servlet;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Matt Roth
 */
public class CustomPackageMappingConfig {

    private String serverPackageName;
    private String clientPackageName;    

    @Override
    public String toString() {
        return "serverPackageName: "
                + getServerPackageName()
                + "\nclientPackageName: "
                + getClientPackageName() + "\n";
    }

    public String getServerPackageName() {
        return serverPackageName;
    }

    public void setServerPackageName(String serverPackageName) {
        this.serverPackageName = serverPackageName;
    }

    public String getClientPackageName() {
        return clientPackageName;
    }

    public void setClientPackageName(String clientPackageName) {
        this.clientPackageName = clientPackageName;
    }
}
