package com.open.hule.library.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.open.hule.library.R;
import com.open.hule.library.listener.UpdateDialogListener;

/**
 * @author hule
 * @date 2019/7/12 17:25
 * description:更新带进度的对话框
 */
public class UpdateProgressDialog extends BaseDialog {
    /**
     * 进度条
     */
    private NumberProgressBar progressBar;

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
    public static UpdateProgressDialog newInstance(Bundle params) {
        UpdateProgressDialog progressDialog = new UpdateProgressDialog();
        if (params != null) {
            progressDialog.setArguments(params);
        }
        return progressDialog;
    }

    /**
     * 取消下载监听
     *
     * @param updateDialogListener 取消下载监听
     */
    public void addUpdateDialogListener(UpdateDialogListener updateDialogListener) {
        this.updateDialogListener = updateDialogListener;
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

    @Override
    int getLayoutId() {
        return R.layout.dialog_downloading;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.nbpProgress);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        if (getArguments() != null) {
            int forceUpdate = getArguments().getInt("forceUpdate", 0);
            if (0 == forceUpdate) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (updateDialogListener != null) {
                            updateDialogListener.cancelUpdate();
                        }
                    }
                });
            } else {
                btnCancel.setVisibility(View.GONE);
            }
        }
    }
}
