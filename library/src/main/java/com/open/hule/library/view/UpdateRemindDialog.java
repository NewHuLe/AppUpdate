package com.open.hule.library.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.open.hule.library.R;
import com.open.hule.library.entity.AppUpdate;
import com.open.hule.library.listener.UpdateDialogListener;

/**
 * @author hule
 * @date 2019/7/11 10:46
 * description:至于为什么使用DialogFragment而不是Dialog,
 * 相信你会明白这是google的推荐，在一个原因可高度定制你想要的任何更新界面
 */
public class UpdateRemindDialog extends BaseDialog {
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
            AppUpdate appUpdate = getArguments().getParcelable("appUpdate");
            if (appUpdate != null && appUpdate.getUpdateResourceId() != 0) {
                return inflater.inflate(appUpdate.getUpdateResourceId(), container, false);
            }
        }
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() == null) {
            dismiss();
            return;
        }
        AppUpdate appUpdate = getArguments().getParcelable("appUpdate");
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
            if (TextUtils.isEmpty(appUpdate.getNewVersionCode())) {
                tvVersion.setVisibility(View.GONE);
            } else {
                tvVersion.setVisibility(View.VISIBLE);
                tvVersion.setText(String.format(getResources().getString(R.string.update_version), appUpdate.getNewVersionCode()));
            }
            if(TextUtils.isEmpty(appUpdate.getFileSize())){
                tvFileSize.setVisibility(View.GONE);
            }else {
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
        }

        // 取消更新的按钮文本提示
        Button btnUpdateLater = view.findViewById(R.id.btnUpdateLater);
        // 更新的按钮文本提示
        Button btnUpdateNow = view.findViewById(R.id.btnUpdateNow);

        btnUpdateLater.setText(appUpdate.getUpdateCancelText());
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
            updateDialogListener.updateDownLoad();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //升级
                updateDialogListener.updateDownLoad();
            } else {
                //提示，并且关闭
                Toast.makeText(getActivity(), getResources().getString(R.string.update_permission), Toast.LENGTH_LONG).show();
                dismiss();
            }
        }
    }
}
