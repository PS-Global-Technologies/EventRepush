package com.psg.liq.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LiqAPILoadProperties {
	private static final Logger LOGGER = LogManager.getLogger(LiqAPILoadProperties.class);

	 private static Properties properties = new Properties();
	  
	  private static Properties props = new Properties();
	  
	  private static Properties jsqlproperties = new Properties();
	  
	  public static void loadProperties() {
	    try {
	      LOGGER.info("Trying to Load Properties file");
	      LOGGER.info("LOGGER::LOADING LOG4J Properties");
	      props.load(new FileInputStream("./config/Log4j.properties"));
	      PropertyConfigurator.configure(props);
	      LOGGER.info("LOGGER::LOG4J Property File Path::" + props.getProperty("log"));
	      InputStream inputStream = null;
	      LOGGER.info("LOGGER::LOADING CONGIF Properties");
	      inputStream = new FileInputStream("./config/config.properties");
	      properties.load(inputStream);
	      loadPropertiesFileFromXml();
	    } catch (FileNotFoundException e1) {
	      LOGGER.error("LOGGER::FileNotFoundException::" + e1.getMessage());
	    } catch (IOException e) {
	      LOGGER.error("LOGGER::IO EXCEPTION::" + e.getMessage());
	    } 
	  }
	  
	  private static void loadPropertiesFileFromXml() {
	    String filePath = properties.getProperty("jsqlaccesspropertiesfilepath");
	    LOGGER.info("Trying to Load XML Properties file::" + filePath);
	    InputStream inputStream = null;
	    try {
	      inputStream = new FileInputStream(filePath);
	      jsqlproperties.loadFromXML(inputStream);
	    } catch (FileNotFoundException e1) {
	      LOGGER.error("LOGGER::FILE NOT FOUND EXCEPTION::" + e1.getMessage());
	    } catch (IOException e) {
	      LOGGER.error("LOGGER::IO EXCEPTION::" + e.getMessage());
	    } 
	  }
	  
	  public static Properties getProperties() {
	    return properties;
	  }
	  
	  public static Properties getJSQLProperties() {
	    return jsqlproperties;
	  }
}
