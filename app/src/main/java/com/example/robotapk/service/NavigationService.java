package com.example.robotapk.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dcm360.controller.gs.controller.GsController;
import com.dcm360.controller.gs.controller.bean.data_bean.RobotPositions;
import com.dcm360.controller.gs.controller.bean.map_bean.RobotPosition;
import com.dcm360.controller.robot_interface.bean.Status;
import com.dcm360.controller.robot_interface.status.RobotStatus;
import com.example.robotapk.controller.RobotManagerController;
import com.example.robotapk.task.TaskManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * @author liyan
 */
public class NavigationService extends Service {
    private static final String TAG = "NavigationService";

    private CompositeDisposable disposables;

    private Context mContext;
    public static boolean isStartNavigationService = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        disposables = new CompositeDisposable();
        startGaoXianSdk();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null && !disposables.isDisposed())
            disposables.dispose();
    }

    public static void initialize() {//转圈初始化
        GsController.INSTANCE.getPosition(TaskManager.getInstances().mapName, 0, new RobotStatus<RobotPositions>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void success(RobotPositions robotPositions) {
                Log.d(TAG, "转圈地初始化222 == " + robotPositions.getData().size());
                for (int i = 0; i < robotPositions.getData().size(); i++) {
                    Log.d(TAG, "转圈地图名称222： " + robotPositions.getData().get(i));
                }

                RobotManagerController.getInstance().getRobotController().initialize(TaskManager.getInstances().mapName, robotPositions.getData().get(0).getName() + "", new RobotStatus<Status>() {
                    @Override
                    public void success(Status status) {
                        Log.d(TAG, "转圈地初始化成功");
                    }

                    @Override
                    public void error(Throwable error) {
                        Log.d(TAG, "转圈地初始化失败：" + error.getMessage());
                    }
                });

            }

            @Override
            public void error(Throwable error) {
                Log.d(TAG, "转圈地初始化失败222 == " + error.getMessage());
            }
        });
    }

    public static void stopInitialize() {//停止初始化
        RobotManagerController.getInstance().getRobotController().stop_initialize(new RobotStatus<Status>() {
            @Override
            public void success(Status status) {
                Log.d(TAG, "停止初始化");
            }

            @Override
            public void error(Throwable error) {
                Log.d(TAG, "停止初始化失败：" + error.getMessage());
            }
        });
    }

    public static void navigationUp() {
        Log.d(TAG, "执行命令 navigationUp=");
        //Tx2RosManager.getInstanse().chargeState(false);充电
        RobotManagerController.getInstance().getRobotController().move(0.2f, 0.0f, new RobotStatus<Status>() {
            @Override
            public void success(Status status) {

            }

            @Override
            public void error(Throwable error) {

            }
        });
    }

    public static void navigationDown() {
        Log.d(TAG, "执行命令 navigationDown=");
        RobotManagerController.getInstance().getRobotController().move(-0.2f, 0.0f, new RobotStatus<Status>() {
            @Override
            public void success(Status status) {

            }

            @Override
            public void error(Throwable error) {

            }
        });
    }

    public static void navigationLeft() {
        Log.d(TAG, "执行命令 navigationLeft=");
        RobotManagerController.getInstance().getRobotController().move(0.0f, 0.2f, new RobotStatus<Status>() {
            @Override
            public void success(Status status) {

            }

            @Override
            public void error(Throwable error) {

            }
        });
    }

    public static void navigationRight() {
        Log.d(TAG, "执行命令 navigationRight=");
        RobotManagerController.getInstance().getRobotController().move(0.0f, -0.2f, new RobotStatus<Status>() {
            @Override
            public void success(Status status) {

            }

            @Override
            public void error(Throwable error) {

            }
        });
    }

    public void startGaoXianSdk() {
        Log.d(TAG, "   导航服务启动");
        RobotManagerController.getInstance().getRobotController().connect_robot("http://10.7.6.88:8080");
        ping();

    }

    private void ping() {
        disposables.add(Observable.interval(2, TimeUnit.SECONDS).subscribe(aLong -> RobotManagerController.getInstance().getRobotController().ping(new RobotStatus<Status>() {
            @Override
            public void success(Status status) {
                Log.d(TAG, "gxRobotStatus = " + status.toString());
                connectStatus(status != null && status.isSuccessed());
            }

            @Override
            public void error(Throwable error) {
                connectStatus(false);
            }
        }), throwable -> {
            Log.d(TAG, "导航:ping:" + throwable.getMessage());
        }));
    }

    private void connectStatus(boolean connect) {
        if (isStartNavigationService != connect) {
            Log.d(TAG, "底盘连接状态：" + connect);
//            TaskQueueManager.getInstances().notifyMessage("底盘连接状态：" + connect, "01");
            isStartNavigationService = connect;
            TaskManager.getInstances().loadMapList();
        }
    }
}
