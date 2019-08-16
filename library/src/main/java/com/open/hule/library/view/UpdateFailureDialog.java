package com.open.hule.library.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.open.hule.library.R;
import com.open.hule.library.listener.UpdateDialogListener;

/**
 * @author hule
 * @date 2019/7/12 17:25
 * description:更新失败对话框
 */
public class UpdateFailureDialog extends BaseDialog {

    /**
     * 取消下载使用
     */
    private UpdateDialogListener updateDialogListener;

    /**
     * 初始化弹框
     *
     * @param params 参数
     * @return DownloadDialog
     */
    public static UpdateFailureDialog newInstance(Bundle params) {
        UpdateFailureDialog updateFailureDialog = new UpdateFailureDialog();
        if (params != null) {
            updateFailureDialog.setArguments(params);
        }
        return updateFailureDialog;
    }

    /**
     * 取消下载监听
     *
     * @param updateDialogListener 取消下载监听
     */
    public void addUpdateDialogListener(UpdateDialogListener updateDialogListener) {
        this.updateDialogListener = updateDialogListener;
    }

    @Override
    int getLayoutId() {
        return R.layout.dialog_download_failed;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView version = view.findViewById(R.id.version);
        if (getArguments() != null) {
            version.setVisibility(View.VISIBLE);
            version.setText(String.format(getResources().getString(R.string.down_version), getArguments().getString("newVersionCode")));
        } else {
            version.setVisibility(View.GONE);
        }
        Button btnBrowser = view.findViewById(R.id.btnBrowser);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);

        btnBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateDialogListener != null) {
                    updateDialogListener.downFromBrowser();
                }
                dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getArguments() != null) {
                    int forceUpdate = getArguments().getInt("forceUpdate", 0);
                    if (0 == forceUpdate) {
                        if (updateDialogListener != null) {
                            updateDialogListener.cancelUpdate();
                        }
                    } else {
                        if (updateDialogListener != null) {
                            updateDialogListener.forceExit();
                        }
                    }
                }
                dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateDialogListener != null) {
                    updateDialogListener.updateRetry();
                }
                dismiss();
            }
        });
    }
}
