package savestring.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

  private static String dbhost = "jdbc:mysql://localhost:3306/5ai_savestrings";
  private static String username = "root";
  private static String password = "";
  private static Connection conn;

  @SuppressWarnings("finally")
  public static Connection createNewDBconnection() throws FileNotFoundException, IOException {
    try {
      conn = DriverManager.getConnection(dbhost, username, password);
      System.out.println(conn.toString());
    } catch (SQLException e) {
      System.out.println("Cannot create database connection");
      System.out.println(e.getMessage());
      System.out.println(e.getErrorCode());
      e.printStackTrace();
    } finally {
      return conn;
    }
  }

}