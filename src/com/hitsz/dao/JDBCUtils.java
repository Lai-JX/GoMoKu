package com.hitsz.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtils {
    static {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Connection getConn() {
        Connection  conn = null;
        try {
            conn=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/gomoku"+"?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true"
                    +"&useSSL=false&serverTimezone=GMT%2B8","root","12345As#");
//            conn=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/gomoku"+"?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true"
//                    +"&useSSL=false&serverTimezone=GMT%2B8","root","123456");
            conn.setAutoCommit(true);
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return conn;
    }

    public static void close(Connection conn){
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

