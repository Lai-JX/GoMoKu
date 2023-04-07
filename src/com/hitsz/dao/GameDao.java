package com.hitsz.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameDao {

    public int add_game(String user1, String user2){
//        String sql = "insert into users(name,username,password,age,phone) values (?,?,?,?,?)";
        String sql = "insert into game(state, user1, user2) values (?,?,?)";
        Connection con = JDBCUtils.getConn();
//        System.out.println("get conn");
        try {
            PreparedStatement pst=con.prepareStatement(sql);

            pst.setString(1,"0");
            pst.setString(2,user1);
            pst.setString(3,user2);
//            pst.setInt(4,user.getAge());
//            pst.setString(5,user.getPhone());

            int value = pst.executeUpdate();

            if(value>0){
                sql = "select game_id from game where user1=(?) and user2=(?)";
                pst=con.prepareStatement(sql);
                pst.setString(1,user1);
                pst.setString(2,user2);
                ResultSet res = pst.executeQuery();
                if (res.next()) {
                    return res.getInt("game_id");
                }
                return 0;
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }
        return 0;
    }

    public int gameFinish(int game_id, int state, String winner){
//        String sql = "insert into users(name,username,password,age,phone) values (?,?,?,?,?)";
        String sql = "update game set state ="+state+", winner="+winner+" where game_id = "+game_id;
        Connection con = JDBCUtils.getConn();
//        System.out.println("get conn");
        try {
            PreparedStatement pst=con.prepareStatement(sql);

//            pst.setInt(4,user.getAge());
//            pst.setString(5,user.getPhone());

            pst.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }
        return 0;
    }

    public int addOp(int game_id, String user, int x, int y, int color){
//        String sql = "insert into users(name,username,password,age,phone) values (?,?,?,?,?)";
        String sql = "insert into game_detail(game_id, user1, locationx, locationy, color) values (?,?,?,?,?)";
        Connection con = JDBCUtils.getConn();
//        System.out.println("get conn");
        try {
            PreparedStatement pst=con.prepareStatement(sql);

            pst.setInt(1,game_id);
            pst.setString(2,user);
            pst.setInt(3, x);
            pst.setInt(4, y);
            pst.setInt(5, color);

            pst.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }
        return 0;
    }

}
