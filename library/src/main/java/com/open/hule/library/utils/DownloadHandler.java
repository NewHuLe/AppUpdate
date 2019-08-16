package com.open.hule.library.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.open.hule.library.DownloadObserver;
import com.open.hule.library.entity.AppUpdate;
import com.open.hule.library.listener.MainPageExtraListener;
import com.open.hule.library.listener.UpdateDialogListener;
import com.open.hule.library.view.UpdateFailureDialog;
import com.open.hule.library.view.UpdateProgressDialog;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author hule
 * @date 2019/7/15 16:43
 * description:下载监听handler
 */
 public class DownloadHandler extends Handler {

    private final String TAG = getClass().getSimpleName();

    /**
     * 弱引用上下文
     */
    private final WeakReference<Context> wrfContext;
    /**
     * 弱引用DownloadObserver
     */
    private final WeakReference<DownloadObserver> wrfDownloadObserver;
    /**
     * 弱引用进度对话框
     */
    private final WeakReference<UpdateProgressDialog> wrfUpdateProgressDialog;
    /**
     * 弱引用与前台的通讯
     */
    private final WeakReference<MainPageExtraListener> wrfMainPageExtraListener;
    /**
     * 弱引用失败对话框的监听
     */
    private final WeakReference<UpdateDialogListener> wrfUpdateDialogListener;
    /**
     * 下载的id
     */
    private final long lastDownloadId;
    /**
     * 下载的管理器
     */
    private final DownloadManager downloadManager;
    /**
     * 更新实体
     */
    private final AppUpdate appUpdate;


    public DownloadHandler(Context context, DownloadObserver downloadObserver, UpdateProgressDialog progressDialog,
                           MainPageExtraListener mainPageExtraListener, UpdateDialogListener updateDialogListener, DownloadManager downloadManager, long lastDownloadId, AppUpdate appUpdate) {
        wrfContext = new WeakReference<>(context);
        wrfDownloadObserver = new WeakReference<>(downloadObserver);
        wrfUpdateProgressDialog = new WeakReference<>(progressDialog);
        wrfMainPageExtraListener = new WeakReference<>(mainPageExtraListener);
        wrfUpdateDialogListener = new WeakReference<>(updateDialogListener);
        this.lastDownloadId = lastDownloadId;
        this.downloadManager = downloadManager;
        this.appUpdate = appUpdate;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case DownloadManager.STATUS_PAUSED:
                // 暂停
                break;
            case DownloadManager.STATUS_PENDING:
                // 开始
                break;
            case DownloadManager.STATUS_RUNNING:
                // 下载中
                if (wrfUpdateProgressDialog.get() != null) {
                    wrfUpdateProgressDialog.get().setProgress(msg.arg1);
                }
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                if (wrfUpdateProgressDialog.get() != null) {
                    wrfUpdateProgressDialog.get().setProgress(100);
                }
                // 取消监听的广播
                if (wrfContext.get() != null && wrfDownloadObserver.get() != null) {
                    wrfContext.get().getContentResolver().unregisterContentObserver(wrfDownloadObserver.get());
                }
                //下载成功,做200ms安装延迟
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downloadSuccess();
                    }
                }, 200);
                break;
            case DownloadManager.STATUS_FAILED:
                try {
                    // 下载失败，清除本次的下载任务
                    if (lastDownloadId != -1) {
                        downloadManager.remove(lastDownloadId);
                    }
                    // 取消监听的广播
                    if (wrfContext.get() != null && wrfDownloadObserver.get() != null) {
                        wrfContext.get().getContentResolver().unregisterContentObserver(wrfDownloadObserver.get());
                    }
                    // 关闭下载中的进度框，显示下载失败对话框
                    if (wrfContext.get() != null && wrfUpdateProgressDialog.get() != null) {
                        UpdateProgressDialog progressDialog = wrfUpdateProgressDialog.get();
                        if (progressDialog != null && progressDialog.isShowing && wrfContext.get() != null && !((Activity) wrfContext.get()).isFinishing()) {
                            progressDialog.dismiss();
                        }
                        Bundle bundle = new Bundle();
                        bundle.putInt("forceUpdate", appUpdate.getForceUpdate());
                        bundle.putString("newVersionCode", appUpdate.getNewVersionCode());
                        UpdateFailureDialog updateFailureDialog = UpdateFailureDialog.newInstance(bundle);
                        updateFailureDialog.addUpdateDialogListener(wrfUpdateDialogListener.get());
                        updateFailureDialog.show(((FragmentActivity) wrfContext.get()).getSupportFragmentManager(), "AppUpdateUtils");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 下载成功
     */
    private void downloadSuccess() {
        // 关闭进度框
        UpdateProgressDialog progressDialog = wrfUpdateProgressDialog.get();
        if (progressDialog != null && progressDialog.isShowing && wrfContext.get() != null && !((Activity) wrfContext.get()).isFinishing()) {
            progressDialog.dismiss();
        }
        // 获取下载的文件并安装
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = downloadManager.query(query.setFilterById(lastDownloadId));
        if (cursor != null && cursor.moveToFirst()) {
            String fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            cursor.close();
            String path = Uri.parse(fileUri).getPath();
            if (!TextUtils.isEmpty(path)) {
                File apkFile = new File(path);
                if (!TextUtils.isEmpty(appUpdate.getMd5())) {
                    boolean md5IsRight = Md5Util.checkFileMd5(appUpdate.getMd5(), apkFile);
                    if (!md5IsRight) {
                        if (wrfContext.get() != null) {
                            Toast.makeText(wrfContext.get(), "为了安全性和更好的体验，为你推荐浏览器下载更新！", Toast.LENGTH_SHORT).show();
                        }
                        downloadFromBrowse();
                        return;
                    }
                }
                installApp(apkFile);
            }
        }
    }

    /**
     * 从浏览器打开下载，暂时没有选择应用市场，因为应用市场太多，而且协议不同，无法兼顾所有
     */
    private void downloadFromBrowse() {
        try {
            Intent intent = new Intent();
            Uri uri = Uri.parse(appUpdate.getDownBrowserUrl());
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            wrfContext.get().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "无法通过浏览器下载！");
        }
    }

    /**
     * 安装app
     *
     * @param apkFile 下载的文件
     */
    private void installApp(File apkFile) {
        Context context = wrfContext.get();
        if (context != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        boolean allowInstall = context.getPackageManager().canRequestPackageInstalls();
                        if (!allowInstall) {
                            //不允许安装未知来源应用，请求安装未知应用来源的权限
                            if (wrfMainPageExtraListener.get() != null) {
                                wrfMainPageExtraListener.get().applyAndroidOInstall();
                            }
                            return;
                        }
                    }
                    //Android7.0之后获取uri要用contentProvider
                    Uri apkUri = FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName() + ".fileProvider", apkFile);
                    //Granting Temporary Permissions to a URI
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "请点击通知栏完成应用的安装！", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
