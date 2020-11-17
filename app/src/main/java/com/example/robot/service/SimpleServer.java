package com.example.robot.service;

import android.util.Log;

import com.example.robot.task.TaskManager;
import com.example.robot.utils.Content;
import com.example.robot.utils.EventBusMessage;
import com.example.robot.utils.GsonUtils;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class SimpleServer extends WebSocketServer {
    private GsonUtils gsonUtils;
    private JSONObject jsonObject;
    private String mapName = "";
    private String taskName = "";

    public SimpleServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        broadcast("OK"); //This method sends a message to all clients connected
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from " + conn.getRemoteSocketAddress() + ": " + message);
        //EventBus.getDefault().post(new EventBusMessage(10001, message));
        try {
            differentiateType(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println("received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            System.err.println("an error occured on connection " + conn.getRemoteSocketAddress() + ":" + ex);
        } else {
            System.err.println("an error occured on connection " + ex.getMessage());
            ex.printStackTrace();

        }
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
        gsonUtils = new GsonUtils();
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        super.stop();
        System.out.println("server stop successfully");
    }

    @Override
    public void stop(int timeout) throws InterruptedException {
        super.stop(timeout);
        System.out.println("server timeout stop successfully :" + timeout);
    }

    private void differentiateType(String message) throws JSONException {
        Log.d("zdzd ", "getType :  " + gsonUtils.getType(message));
        switch (gsonUtils.getType(message)) {
            case Content.STARTDOWN:
                EventBus.getDefault().post(new EventBusMessage(10001, message));
                break;
            case Content.STARTUP:
                EventBus.getDefault().post(new EventBusMessage(10002, message));
                break;
            case Content.STARTLEFT:
                EventBus.getDefault().post(new EventBusMessage(10003, message));
                break;
            case Content.STARTRIGHT:
                EventBus.getDefault().post(new EventBusMessage(10004, message));
                break;
            case Content.STARTLIGHT:
                jsonObject = new JSONObject(message);
                int string = jsonObject.getInt(Content.SPINNERTIME);
                EventBus.getDefault().post(new EventBusMessage(10005, string));
                break;
            case Content.STOPLIGHT:
                EventBus.getDefault().post(new EventBusMessage(10006, message));
                break;
            case Content.STOPUP:
                EventBus.getDefault().post(new EventBusMessage(10007, message));
                break;
            case Content.STOPDOWN:
                EventBus.getDefault().post(new EventBusMessage(10008, message));
                break;
            case Content.STOPLEFT:
                EventBus.getDefault().post(new EventBusMessage(10009, message));
                break;
            case Content.STOPRIGHT:
                EventBus.getDefault().post(new EventBusMessage(10010, message));
                break;
            case Content.GETMAPLIST://地图列表
                EventBus.getDefault().post(new EventBusMessage(10011, message));
                break;
            case Content.SAVETASKQUEUE://存储任务
                EventBus.getDefault().post(new EventBusMessage(10013, message));
                break;
            case Content.DELETETASKQUEUE://删除任务
                EventBus.getDefault().post(new EventBusMessage(10014, message));
                break;
            case Content.GETTASKQUEUE://任务列表
                jsonObject = new JSONObject(message);
                mapName = jsonObject.getString(Content.MAP_NAME);
                EventBus.getDefault().post(new EventBusMessage(10015, mapName));
                break;
            case Content.GETMAPPIC://地图图片
                jsonObject = new JSONObject(message);
                mapName = jsonObject.getString(Content.MAP_NAME);
                EventBus.getDefault().post(new EventBusMessage(10019, mapName));
                break;
            case Content.ADD_POSITION:
                EventBus.getDefault().post(new EventBusMessage(10021, message));
                break;
            case Content.STARTTASKQUEUE:
                jsonObject = new JSONObject(message);
                EventBus.getDefault().post(new EventBusMessage(10022, jsonObject));
                break;
            case Content.STOPTASKQUEUE:
                jsonObject = new JSONObject(message);
                mapName = jsonObject.getString(Content.MAP_NAME);
                EventBus.getDefault().post(new EventBusMessage(10023, jsonObject));
                break;
            default:
                break;
        }

    }
}
