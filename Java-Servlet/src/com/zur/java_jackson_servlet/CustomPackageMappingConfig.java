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

    private String javaClassName;
    private String customClassName;

    public String getJavaClassName() {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
    }

    public String getCustomClassName() {
        return customClassName;
    }

    public void setCustomClassName(String customClassName) {
        this.customClassName = customClassName;
    }

    @Override
    public String toString() {
        return "javaClassName: "
                + javaClassName
                + "\ncustomClassName: "
                + customClassName + "\n";
    }
}
