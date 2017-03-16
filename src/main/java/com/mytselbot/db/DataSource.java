package com.mytselbot.db;


import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class DataSource {

   private static DataSource datasource;
   private BoneCP connectionPool;
   private ConcurrentHashMap<String, BoneCP> conn;
   private String jndi_master="master";
   
   private DataSource() throws IOException, SQLException, PropertyVetoException {
	   conn = new ConcurrentHashMap<String, BoneCP>();

	   try {
           // load the database driver (make sure this is in your classpath!)
           Class.forName("com.mysql.jdbc.Driver");
       } catch (Exception e) {
           e.printStackTrace();
           return;
       }

       try {
           // setup the connection pool using BoneCP Configuration
           BoneCPConfig config = new BoneCPConfig();
           // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
           config.setJdbcUrl("jdbc:mysql://localhost/mytselbot");
           config.setUsername("root");
           config.setPassword("");
           config.setMinConnectionsPerPartition(5);
           config.setMaxConnectionsPerPartition(10);
           config.setPartitionCount(1);
           // setup the connection pool
           BoneCP bc = new BoneCP(config);
           conn.put(jndi_master, bc);
           
       } catch (Exception e) {
           e.printStackTrace();
           return;
       }

   }
   
   private DataSource(String jndi, String url, String user, String pwd) throws IOException, SQLException, PropertyVetoException {
       try {
           // load the database driver (make sure this is in your classpath!)
           Class.forName("com.mysql.jdbc.Driver");
       } catch (Exception e) {
           e.printStackTrace();
           return;
       }

       try {
           // setup the connection pool using BoneCP Configuration
           BoneCPConfig config = new BoneCPConfig();
           // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
           config.setJdbcUrl(url);
           config.setUsername(user);
           config.setPassword(pwd);
           config.setMinConnectionsPerPartition(5);
           config.setMaxConnectionsPerPartition(10);
           config.setPartitionCount(1);
          
           // setup the connection pool
           BoneCP bc = new BoneCP(config);
           conn.put(jndi, bc);
           
       } catch (Exception e) {
           e.printStackTrace();
           return;
       }

   }


   public static DataSource getInstance() throws IOException, SQLException, PropertyVetoException {
       if (datasource == null) {
           datasource = new DataSource();
           return datasource;
       } else {
           return datasource;
       }
   }

   public Connection getConnection() throws SQLException {
       return this.connectionPool.getConnection();
   }
   
   //////////////
  


}