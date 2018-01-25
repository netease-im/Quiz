package com.netease.nim.quizgame.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.netease.nim.quizgame.R;
import com.netease.nim.quizgame.common.LogUtil;
import com.netease.nim.quizgame.common.permission.MPermission;
import com.netease.nim.quizgame.common.permission.annotation.OnMPermissionDenied;
import com.netease.nim.quizgame.common.permission.annotation.OnMPermissionGranted;
import com.netease.nim.quizgame.common.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.quizgame.common.ui.ToolBarOptions;
import com.netease.nim.quizgame.common.ui.UI;
import com.netease.nim.quizgame.protocol.DemoServerController;
import com.netease.nim.quizgame.protocol.model.RoomInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends UI {

    @BindView(R.id.room_id_edit)
    EditText roomIdEdit;
    @BindView(R.id.enter_room_btn)
    Button enterRoomBtn;

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestBasicPermission();
        setTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_menu_btn:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTitle() {
        ToolBarOptions options = new ToolBarOptions();
        options.isNeedNavigate = false;
        options.isTitleHide = true;
        setToolBar(R.id.toolbar, options);
    }

    @OnClick(R.id.enter_room_btn)
    public void onViewClicked() {
        enterRoom();
    }

    private void enterRoom() {
        if (TextUtils.isEmpty(roomIdEdit.getText().toString())) {
            Toast.makeText(this, "请输入有效房间号", Toast.LENGTH_SHORT).show();
            return;
        }
        DemoServerController.getInstance().fetchRoomInfo(roomIdEdit.getText().toString(), new DemoServerController.IHttpCallback<RoomInfo>() {
            @Override
            public void onSuccess(RoomInfo roomInfo) {
                LogUtil.app("fetch room info success");
                QuizActivity.startActivity(MainActivity.this, roomInfo);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.app("fetch room info failed, code:" + code + ", errorMsg:" + errorMsg);
                if (code == 804) {
                    Toast.makeText(MainActivity.this, "房间号错误，可在主播端DEMO中获取。code：" + code, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "fetch room info failed, code:" + code, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * *********************  基本权限管理 **********************
     */

    private static final int BASIC_PERMISSION_REQUEST_CODE = 0x100;

    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(MainActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        try {
            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        try {
            Toast.makeText(this, "未全部授权，部分功能可能无法正常运行！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }
}
