/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prestashop.odoo.Odoo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Properties;

/**
 *
 * @author marccunillera
 */
public class OdooConfig {

    private static String ODOO_URL = "";
    private static String ODOO_DB_NAME = "";
    private static String ODOO_USER_ID = "";
    private static String ODOO_PASSWORD = "";
    private static FileTime fileTime;
    private static final Path PATH = Paths.get("/home/marccunillera/Documentos/odoo-config.properties");

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(PATH.toFile())) {
            props.load(fis);

            ODOO_URL = props.getProperty("ODOO_URL", "");
            ODOO_DB_NAME = props.getProperty("ODOO_DB_NAME", "");
            ODOO_USER_ID = props.getProperty("ODOO_USER_ID", "");
            ODOO_PASSWORD = props.getProperty("ODOO_PASSWORD", "");
            fileTime = Files.getLastModifiedTime(PATH);

        } catch (IOException e) {
            System.err.println("[CONFIG] Error carregant fitxer de configuració: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getODOO_URL() {
        return ODOO_URL;
    }

    public static String getODOO_DB_NAME() {
        return ODOO_DB_NAME;
    }

    public static String getODOO_USER_ID() {
        return ODOO_USER_ID;
    }
    
    public static String getODOO_PASSWORD() {
        return ODOO_PASSWORD;
    }
}

