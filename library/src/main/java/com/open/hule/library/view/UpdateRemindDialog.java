package com.open.hule.library.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.open.hule.library.R;
import com.open.hule.library.entity.AppUpdate;
import com.open.hule.library.listener.UpdateDialogListener;

import java.util.Objects;

/**
 * @author hule
 * @date 2019/7/11 10:46
 * description:至于为什么使用DialogFragment而不是Dialog,
 * 相信你会明白这是google的推荐，在一个原因可高度定制你想要的任何更新界面
 */
public class UpdateRemindDialog extends BaseDialog {
    /**
     * 8.0未知应用授权请求码
     */
    private static final int INSTALL_PACKAGES_REQUESTCODE = 1112;
    /**
     * 用户跳转未知应用安装的界面请求码
     */
    private static final int GET_UNKNOWN_APP_SOURCES = 1113;
    /**
     * 进度条
     */
    private NumberProgressBar progressBar;
    /**
     * 底部按钮事件的根布局
     */
    private LinearLayout llEvent;
    /**
     * 取消更新下载按钮
     */
    private Button btnCancelUpdate;
    /**
     * 稍后更新按钮
     */
    private Button btnUpdateLater;
    /**
     * 立即更新按钮
     */
    private Button btnUpdateNow;
    /**
     * 浏览器下载按钮
     */
    private Button btnUpdateBrowse;
    /**
     * 重试下载
     */
    private Button btnUpdateRetry;
    /**
     * 取消更新（退出应用）
     */
    private Button btnUpdateExit;

    /**
     * 更新数据
     */
    private AppUpdate appUpdate;
    /**
     * 监听接口
     */
    private UpdateDialogListener updateDialogListener;


    /**
     * 初始化弹框
     *
     * @param params 参数
     * @return DownloadDialog
     */
    public static UpdateRemindDialog newInstance(Bundle params) {
        UpdateRemindDialog downloadDialog = new UpdateRemindDialog();
        if (params != null) {
            downloadDialog.setArguments(params);
        }
        return downloadDialog;
    }

    /**
     * 回调监听
     *
     * @param updateListener 监听接口
     * @return DownloadDialog
     */
    public UpdateRemindDialog addUpdateListener(UpdateDialogListener updateListener) {
        this.updateDialogListener = updateListener;
        return this;
    }

    @Override
    int getLayoutId() {
        return R.layout.dialog_update;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            appUpdate = getArguments().getParcelable("appUpdate");
            if (appUpdate != null && appUpdate.getUpdateResourceId() != 0) {
                return inflater.inflate(appUpdate.getUpdateResourceId(), container, false);
            }
        }
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (appUpdate == null) {
            dismiss();
            return;
        }
        // 默认布局的显示
        if (appUpdate.getUpdateResourceId() == R.layout.dialog_update) {
            // 标题
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            // 强制更新的提示语
            TextView tvForceUpdate = view.findViewById(R.id.tvForceUpdate);
            // 版本号
            TextView tvVersion = view.findViewById(R.id.tvVersion);
            // 下载的文件大小
            TextView tvFileSize = view.findViewById(R.id.tvFileSize);
            // 更新内容的title
            TextView tvContentTips = view.findViewById(R.id.tvContentTips);
            // 跟新的内容
            TextView tvContent = view.findViewById(R.id.tvContent);
            // 更新的标题
            tvTitle.setText(appUpdate.getUpdateTitle());
            // 底部按钮事件的根布局
            llEvent = view.findViewById(R.id.llEvent);
            // 进度条
            progressBar = view.findViewById(R.id.nbpProgress);
            // 浏览器下载
            btnUpdateBrowse = view.findViewById(R.id.btnUpdateBrowse);
            // 取消更新
            btnCancelUpdate = view.findViewById(R.id.btnCancelUpdate);
            // 重新下载
            btnUpdateRetry = view.findViewById(R.id.btnUpdateRetry);
            // 取消更新（退出应用）
            btnUpdateExit = view.findViewById(R.id.btnUpdateExit);
            if (TextUtils.isEmpty(appUpdate.getNewVersionCode())) {
                tvVersion.setVisibility(View.GONE);
            } else {
                tvVersion.setVisibility(View.VISIBLE);
                tvVersion.setText(String.format(getResources().getString(R.string.update_version), appUpdate.getNewVersionCode()));
            }
            if (TextUtils.isEmpty(appUpdate.getFileSize())) {
                tvFileSize.setVisibility(View.GONE);
            } else {
                tvFileSize.setVisibility(View.VISIBLE);
                tvFileSize.setText(String.format(getResources().getString(R.string.update_size), appUpdate.getFileSize()));
            }
            tvContentTips.setText(appUpdate.getUpdateContentTitle());
            tvContent.setText(TextUtils.isEmpty(appUpdate.getUpdateInfo()) ? getResources().getString(R.string.default_update_content) : appUpdate.getUpdateInfo());
            tvContent.setMovementMethod(new ScrollingMovementMethod());
            if (appUpdate.getForceUpdate() == 0) {
                tvForceUpdate.setVisibility(View.GONE);
            } else {
                tvForceUpdate.setVisibility(View.VISIBLE);
            }
            btnCancelUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (updateDialogListener != null) {
                        updateDialogListener.cancelUpdate();
                    }
                }
            });
            btnUpdateBrowse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (updateDialogListener != null) {
                        updateDialogListener.downFromBrowser();
                    }
                }
            });
            btnUpdateRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (updateDialogListener != null) {
                        updateDialogListener.updateRetry();
                    }
                }
            });
            btnUpdateExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (updateDialogListener != null) {
                        updateDialogListener.cancelUpdate();
                    }
                }
            });
        }

        // 取消更新的按钮文本提示
        btnUpdateLater = view.findViewById(R.id.btnUpdateLater);
        btnUpdateLater.setText(appUpdate.getUpdateCancelText());
        // 更新的按钮文本提示
        btnUpdateNow = view.findViewById(R.id.btnUpdateNow);
        btnUpdateNow.setText(appUpdate.getUpdateText());
        if (appUpdate.getForceUpdate() == 0) {
            btnUpdateLater.setVisibility(View.VISIBLE);
        } else {
            btnUpdateLater.setVisibility(View.GONE);
        }
        btnUpdateLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateDialogListener != null) {
                    updateDialogListener.cancelUpdate();
                }
            }
        });

        btnUpdateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateDialogListener != null) {
                    requestPermission();
                }
            }
        });
    }

    /**
     * 更新下载的进度
     *
     * @param currentProgress 当前进度
     */
    public void setProgress(int currentProgress) {
        if (progressBar != null && currentProgress > 0) {
            progressBar.setProgress(currentProgress);
        }
    }

    /**
     * 开启进度条，若强制更新则隐藏底部所有按钮只显示进度条，
     * 否则显示取消更新按钮，隐藏稍后更新与立即更新
     */
    public void showProgressBtn() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }
        if (0 == appUpdate.getForceUpdate()) {
            // 非强制更新
            llEvent.setVisibility(View.VISIBLE);
            if (btnCancelUpdate != null) {
                btnUpdateLater.setVisibility(View.GONE);
                btnUpdateNow.setVisibility(View.GONE);
                btnCancelUpdate.setVisibility(View.VISIBLE);
                btnUpdateBrowse.setVisibility(View.GONE);
                btnUpdateExit.setVisibility(View.GONE);
                btnUpdateRetry.setVisibility(View.GONE);
            }
        } else {
            // 强制更新
            llEvent.setVisibility(View.GONE);
        }
    }

    /**
     * 显示下载失败的按钮，如果强制更新，显示重试下载和浏览器下载，退出应用
     * 如果非强制更新，显示重试下载和浏览器下载，取消
     */
    public void showFailBtn() {
        Toast.makeText(getContext(), "更新失败啦，请重试！", Toast.LENGTH_SHORT).show();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        // 非强制更新
        llEvent.setVisibility(View.VISIBLE);
        btnUpdateLater.setVisibility(View.GONE);
        btnUpdateNow.setVisibility(View.GONE);
        btnCancelUpdate.setVisibility(View.GONE);
        btnUpdateBrowse.setVisibility(View.VISIBLE);
        btnUpdateExit.setVisibility(View.VISIBLE);
        btnUpdateRetry.setVisibility(View.VISIBLE);
        if (0 == appUpdate.getForceUpdate()) {
            // 非强制更新
            btnUpdateExit.setText("取消");
        } else {
            // 强制更新
            btnUpdateExit.setText("退出");
        }
    }

    /**
     * 判断存储卡权限
     */
    private void requestPermission() {
        if (getActivity() == null) {
            return;
        }
        //权限判断是否有访问外部存储空间权限
        int flag = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (flag != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
                Toast.makeText(getActivity(), getResources().getString(R.string.update_permission), Toast.LENGTH_LONG).show();
            }
            // 申请授权
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            if (updateDialogListener != null) {
                updateDialogListener.updateDownLoad();
            }
        }
    }

    /**
     * 申请android O 安装权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestInstallPermission() {
        requestPermissions(new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUESTCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //8.0应用设置界面未知安装开源返回时候
        if (requestCode == GET_UNKNOWN_APP_SOURCES && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean allowInstall = Objects.requireNonNull(getContext()).getPackageManager().canRequestPackageInstalls();
            if (allowInstall) {
                dismiss();
                if (updateDialogListener != null) {
                    updateDialogListener.installApkAgain();
                }
            } else {
                Toast.makeText(getContext(), "您拒绝了安装未知来源应用，应用暂时无法更新！", Toast.LENGTH_SHORT).show();
                exit();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //6.0 存储权限授权结果回调
                if (updateDialogListener != null) {
                    updateDialogListener.updateDownLoad();
                }
            } else {
                //提示，并且关闭
                Toast.makeText(getActivity(), getResources().getString(R.string.update_permission), Toast.LENGTH_LONG).show();
                exit();
            }
        } else if (requestCode == INSTALL_PACKAGES_REQUESTCODE) {
            // 8.0的权限请求结果回调,授权成功
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (updateDialogListener != null) {
                    updateDialogListener.installApkAgain();
                }
            } else {
                // 授权失败，引导用户去未知应用安装的界面
                if (getContext() != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //注意这个是8.0新API
                    Uri packageUri = Uri.parse("package:" + getContext().getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
            }
        }
    }

    /**
     * 强制退出
     */
    private void exit() {
        if (0 != appUpdate.getForceUpdate()) {
            if (updateDialogListener != null) {
                updateDialogListener.forceExit();
            }
        } else {
            dismiss();
        }
    }
}
