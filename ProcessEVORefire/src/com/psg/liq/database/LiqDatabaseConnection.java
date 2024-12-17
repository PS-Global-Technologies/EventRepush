package com.psg.liq.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.psg.liq.util.LiqAPILoadProperties;

public class LiqDatabaseConnection {
  private static final Logger LOGGER = LogManager.getLogger(LiqDatabaseConnection.class);
  
  static Connection con = null;
  
  int intStatus;
  
  private static Properties properties;
  
  public static void loadConnection() {
    try {
      properties = LiqAPILoadProperties.getProperties();
      Properties jSqlAccessProperties = LiqAPILoadProperties.getJSQLProperties();
      String strJSqlAccessJdbcClass = jSqlAccessProperties.getProperty("JSqlAccessJdbcClass");
      String strJSqlAccessJdbcUrl = jSqlAccessProperties.getProperty("JSqlAccessJdbcUrl");
      String strDBDefaultPassword = jSqlAccessProperties.getProperty("DBDefaultPassword");
      String strDBDefaultUserName = jSqlAccessProperties.getProperty("DBDefaultUserName");
      Class.forName(strJSqlAccessJdbcClass);
      con = DriverManager.getConnection(strJSqlAccessJdbcUrl, strDBDefaultUserName, strDBDefaultPassword);
    } catch (SQLException e) {
      LOGGER.error("LOANIQ::SQLException::" + e.getMessage());
    } catch (ClassNotFoundException e) {
      LOGGER.error("LOANIQ::ClassNotFoundException::" + e.getMessage());
    } 
  }
  
  public static Connection getConnection() {
    return con;
  }
}