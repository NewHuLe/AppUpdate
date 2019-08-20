package com.open.hule.appupdate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.open.hule.library.entity.AppUpdate;
import com.open.hule.library.listener.MainPageExtraListener;
import com.open.hule.library.utils.AppUpdateUtils;

/**
 * @author hule
 * @date 2019/7/10 15:48
 * description:检查更新测试页面
 */
public class MainActivity extends AppCompatActivity implements MainPageExtraListener {

    // 8.0未知应用
    public static final int INSTALL_PACKAGES_REQUESTCODE = 1112;

    public static final int GET_UNKNOWN_APP_SOURCES = 1113;

    private AppUpdateUtils updateUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUpdate();
            }
        });
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        updateUtils = new AppUpdateUtils(MainActivity.this);
        AppUpdate appUpdate = new AppUpdate.Builder()
                //更新地址
                .newVersionUrl("https://imtt.dd.qq.com/16891/8EC4E86B648D57FDF114AF5D3002C09B.apk")
                // 版本号
                .newVersionCode("v1.2")
                // 通过传入资源id来自定义更新对话框，注意取消更新的id要定义为btnUpdateLater，立即更新的id要定义为btnUpdateNow
                .updateResourceId(R.layout.dialog_update)
                // 文件大小
                .fileSize("5.8M")
                //是否采取默认模式（只显示更新提示，后台下载完自动弹出安装界面），否则，显示下载进度，显示下载失败弹框
                .defaultMode(0)
                //默认不采取强制更新，否则，不更新无法使用
                .forceUpdate(0)
                .build();
        updateUtils.startUpdate(appUpdate, this);
    }

    @Override
    public void forceExit() {
        // 如果使用到了强制退出，需要自己控制
        finish();
        System.exit(0);
    }

    /**
     * 检测到无权限安装未知来源应用，回调接口中需要重新请求安装未知应用来源的权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void applyAndroidOInstall() {
        //请求安装未知应用来源的权限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUESTCODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 8.0的权限请求结果回调
        if (requestCode == INSTALL_PACKAGES_REQUESTCODE) {
            // 授权成功
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                installApkAgain();
            } else {
                // 授权失败，引导用户去未知应用安装的界面
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //注意这个是8.0新API
                    Uri packageUri = Uri.parse("package:" + getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //8.0应用设置界面未知安装开源返回时候
        if (requestCode == GET_UNKNOWN_APP_SOURCES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean allowInstall = getPackageManager().canRequestPackageInstalls();
                if (allowInstall) {
                    installApkAgain();
                } else {
                    Toast.makeText(MainActivity.this, "您拒绝了安装未知来源应用，应用暂时无法更新！", Toast.LENGTH_SHORT).show();
                    if (0 != updateUtils.getAppUpdate().getForceUpdate()) {
                        forceExit();
                    }
                }
            }
        }
    }

    private void installApkAgain() {
        if (updateUtils != null) {
            updateUtils.installAppAgain();
        }
    }
}
