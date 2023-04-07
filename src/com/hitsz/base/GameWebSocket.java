package com.hitsz.base;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.hitsz.dao.GameDao;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

@ServerEndpoint("/websocket/{username}")
public class GameWebSocket {

    private static int onlineCount = 0;

    private static Map<String, GameWebSocket> userList = new ConcurrentHashMap<String, GameWebSocket>();
    // 等待队列
    private static LinkedBlockingQueue<String> queue =new LinkedBlockingQueue<String>();
    // game_id 与user1、user2 映射
    private static Map<Integer, String> gameIdToUser1 = new ConcurrentHashMap<Integer, String>();
    private static Map<Integer, String> gameIdToUser2 = new ConcurrentHashMap<Integer, String>();
    // game_id 与 棋盘 映射
    private static Map<Integer, Integer[][]> gameIdToMap = new ConcurrentHashMap<Integer, Integer[][]>();

    private Session session;
    private String username;
    private GameDao gameDao = new GameDao();


    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) throws IOException {
//        gameDao.gameFinish(2,2,"11");
        this.username = username;
        this.session = session;
        // 添加用户名和对应客户端
        userList.put(username, this);
        queue.add(username);

        sendMessageTo("{\"msg\":\""+queue.toString()+"\"}",username);

        System.out.println("已连接 "+username);
        match();
    }

    @OnClose
    public void onClose() throws IOException {
//        System.out.println("close server!");
//        userWaitList.remove(username);
//        subOnlineCount();
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        System.out.println(message);
        JSONObject jsonTo = JSONObject.fromObject(message);
//        System.out.println("on message!");
//        System.out.println("message:"+jsonTo.get("msg"));
        if(jsonTo.get("game_id") != null) {
            // 1. 获取参数
            int game_id = Integer.parseInt((String)jsonTo.get("game_id"));
            String user1 = (String) jsonTo.get("name");
            String user2 = "";
            // 另一user
            if (gameIdToUser1.get(game_id).equals(user1)) {
                user2 = gameIdToUser2.get(game_id);
            } else {
                user2 = gameIdToUser1.get(game_id);
            }
            // 判游戏过程中退出
            if (jsonTo.get("state")!=null && jsonTo.get("state").equals("quit_matched")){    // 游戏过程中返回
                userList.remove(user1);
                sendMessageTo("{\"flag\":\"quit\"}",user2);
                userList.remove(user2);
                gameIdToUser1.remove(game_id);
                gameIdToUser2.remove(game_id);
                gameIdToMap.remove(game_id);
                gameDao.gameFinish(game_id,1,null);  // 结束但没完成
                return;
            }
            int x = (int)jsonTo.get("x");
            int y = (int)jsonTo.get("y");
            int color = (int)jsonTo.get("color");

            System.out.println("user:");
            System.out.println(gameIdToUser1);
            System.out.println(gameIdToUser2);
            gameDao.addOp(game_id, user1, x, y, color);
            // 更新棋盘
            gameIdToMap.get(game_id)[x][y] = color;

            System.out.println("user1:"+user1+" user2:"+user2);
            // 2. 给另一user返回位置
            sendMessageTo("{\"x\":\""+x + "\",\"y\":"+y+"}",user2);
            // 3. check 3.1 能判断输赢则返回结果
            if(checkWin(game_id, x, y, color)){
                sendMessageTo("{\"flag\":\"true\"}",user1);
                sendMessageTo("{\"flag\":\"false\"}",user2);

                userList.remove(user1);
                userList.remove(user2);
                gameIdToUser1.remove(game_id);
                gameIdToUser2.remove(game_id);
                gameIdToMap.remove(game_id);
                gameDao.gameFinish(game_id, 2, user1); // 结束并且完成了
            }
        }
        if(jsonTo.get("state") != null) {
            if (jsonTo.get("state").equals("quit_matching")){   // 匹配过程中返回
                userList.remove(jsonTo.get("name"));
                queue.poll();
            }
        }

//        userList.get(username).session.getAsyncRemote().sendText(message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("error!");
        error.printStackTrace();
    }

    public void sendMessageTo(String message, String To) throws IOException {
        System.out.println("server send:"+message + " to " + To);
        GameWebSocket tmp = userList.get(To);
//        tmp.session.getAsyncRemote().sendText(message);
        tmp.session.getBasicRemote().sendText(message);
    }

    public synchronized void match() throws IOException {
//        System.out.println("queue size:"+queue.size());
        while(queue.size() > 1) {
            String user1 = queue.remove();
            String user2 = queue.remove();
            // 生成游戏
            int game_id = gameDao.add_game(user1, user2);
            System.out.println(user1+"   "+user2+"    "+game_id);
            gameIdToUser1.put(game_id,user1);
            gameIdToUser2.put(game_id,user2);
            // 初始化棋盘
            Integer[][] tmp = new Integer[10][10];
            for(int i=0; i<10; i++){
                for (int j=0; j<10; j++){
                    tmp[i][j]=0;
                }
            }

            gameIdToMap.put(game_id, tmp);
            // 通知用户
            sendMessageTo("{\"game_id\":\""+game_id + "\",\"color\":\"black\",\"opponent\":\""+user2+"\"}",user1);
            sendMessageTo("{\"game_id\":\""+game_id + "\",\"color\":\"white\",\"opponent\":\""+user1+"\"}",user2);


        }
    }




    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        GameWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        GameWebSocket.onlineCount--;
    }

    private boolean checkByStep(int game_id, int x, int y, int xdiff, int ydiff, int color) {
        Integer[][] arr = gameIdToMap.get(game_id);
        int i, tmp_x, tmp_y;
        int cnt = 0;

        //向反方向找到颜色相同的点
        for (i = 1;  i < 5; i++){
            tmp_x = x - xdiff * i;
            tmp_y = y - ydiff * i;
            if (tmp_x < 0 || tmp_x > 9 || tmp_y < 0 || tmp_y > 9 || arr[tmp_x][tmp_y] != color){
                break;
            }
            cnt++;
        }

        for (i = 1;  i < 5; i++){
            tmp_x = x + xdiff * i;
            tmp_y = y + ydiff * i;
            if (tmp_x < 0 || tmp_x > 9 || tmp_y < 0 || tmp_y > 9 || arr[tmp_x][tmp_y] != color){
                break;
            }
            cnt++;
        }
//        Log.e(TAG, "checkByStep: no reverse cnt = " + cnt);
        if (cnt >= 4) {
            return true;
        }
        return false;
    }
    private boolean checkWin(int game_id, int x, int y, int color){
        if (checkByStep(game_id, x, y, 0, 1, color))    //上下直线判断
        {
            return true;
        }
        if (checkByStep(game_id, x, y, 1, 0, color))    //左右直线判断
        {
            return true;
        }
        if (checkByStep(game_id, x, y, 1, 1, color))    //右朝上直线判断
        {
            return true;
        }
        if (checkByStep(game_id, x, y, -1, 1, color))   //右朝下直线判断
        {
            return true;
        }
        return false;
    }
}