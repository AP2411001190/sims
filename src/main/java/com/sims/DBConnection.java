package com.sims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** JDBC connection helper. Change USER/PASS to match your MySQL setup. */
public class DBConnection {
    private static final String SERVER = "jdbc:mysql://localhost:3306/";
    private static final String URL = SERVER + "sims";
    private static final String USER = "root";
    private static final String PASS = "root";

    /** Creates the database and table on first run, so no manual setup is needed. */
    public static void init() throws SQLException {
        try (Connection c = DriverManager.getConnection(SERVER, USER, PASS);
             Statement st = c.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS sims");
        }
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS students ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "reg_no VARCHAR(20) NOT NULL UNIQUE,"
                + "name VARCHAR(100) NOT NULL,"
                + "email VARCHAR(100),"
                + "phone VARCHAR(15),"
                + "department VARCHAR(50),"
                + "year INT,"
                + "cgpa DECIMAL(3,2))");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
