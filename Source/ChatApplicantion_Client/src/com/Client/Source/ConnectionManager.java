/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Client.Source;

import java.sql.Connection;
import java.sql.DriverManager;
/**
 *
 * @author ASUS
 */
public class ConnectionManager {
   private static Connection conn;
 private static String DRIVER = "com.mysql.jdbc.Driver";
 private static String URL = "jdbc:mysql://localhost:3306/qltk";
 private static String USER = "root";
 private static String PASS = "1234";
 
 public static Connection getConnection(){
  try {
   Class.forName(DRIVER);
   conn = DriverManager.getConnection(URL,USER,PASS);
      System.out.println("com.Client.GUI.ConnectionManager.getConnection() kết nối thành công" );
  } catch (Exception e) {
       System.out.println(e);
  }
  return conn;
 }
}
