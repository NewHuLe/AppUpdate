package com.open.hule.library.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.open.hule.library.downloadmanager.DownloadHandler;
import com.open.hule.library.downloadmanager.DownloadObserver;
import com.open.hule.library.entity.AppUpdate;
import com.open.hule.library.listener.MainPageExtraListener;
import com.open.hule.library.listener.UpdateDialogListener;
import com.open.hule.library.view.UpdateRemindDialog;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * @author hule
 * @date 2019/7/11 9:34
 * description: 下载更新工具类
 */
public class UpdateManager implements UpdateDialogListener {

    private static final String TAG = "UpdateManager";
    /**
     * 是否启动自动安装
     */
    public static boolean isAutoInstall;
    /**
     * context的弱引用
     */
    private final WeakReference<Context> wrfContext;
    /**
     * 系统DownloadManager
     */
    private DownloadManager downloadManager;
    /**
     * 上次下载的id
     */
    private long lastDownloadId = -1;
    /**
     * 更新的实体参数
     */
    private AppUpdate appUpdate;
    /**
     * 下载与主页之间的通信
     */
    private MainPageExtraListener mainPageExtraListener;
    /**
     * 下载监听
     */
    private DownloadObserver downloadObserver;
    /**
     * 更新提醒对话框
     */
    private UpdateRemindDialog updateRemindDialog;

    /**
     * 配置上下文，必须传
     *
     * @param context 上下文
     */
    public UpdateManager(Context context) {
        wrfContext = new WeakReference<>(context);
    }

    /**
     * 开启下载更新
     *
     * @param appUpdate             更新数据
     * @param mainPageExtraListener 与当前页面交互的接口
     */
    public void startUpdate(AppUpdate appUpdate, MainPageExtraListener mainPageExtraListener) {
        Context context = wrfContext.get();
        if (context == null) {
            throw new NullPointerException("UpdateManager======context不能为null，请先在构造方法中传入！");
        }
        if (appUpdate == null) {
            throw new NullPointerException("UpdateManager======appUpdate不能为null，请配置相关更新信息！");
        }
        this.appUpdate = appUpdate;
        isAutoInstall = appUpdate.getIsSlentMode();
        this.mainPageExtraListener = mainPageExtraListener;
        Bundle bundle = new Bundle();
        bundle.putParcelable("appUpdate", appUpdate);
        updateRemindDialog = UpdateRemindDialog.newInstance(bundle).addUpdateListener(this);
        updateRemindDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "UpdateManager");
    }

    /**
     * 获取实体性喜
     *
     * @return AppUpdate
     */
    public AppUpdate getAppUpdate() {
        return appUpdate;
    }

    /**
     * 下载apk
     */
    private void downLoadApk() {
        try {
            Context context = wrfContext.get();
            if (context != null) {
                // 获取下载管理器
                downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                clearCurrentTask();
                // 下载地址如果为null,抛出异常
                String downloadUrl = Objects.requireNonNull(appUpdate.getNewVersionUrl());
                Uri uri = Uri.parse(downloadUrl);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                // 下载中和下载完成显示通知栏
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                if (TextUtils.isEmpty(appUpdate.getSavePath())) {
                    //使用系统默认的下载路径 此处为应用内 /android/data/packages ,所以兼容7.0
                    request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, context.getPackageName() + ".apk");
                    deleteApkFile(Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + File.separator + context.getPackageName() + ".apk")));
                } else {
                    // 自定义的下载目录,注意这是涉及到android Q的存储权限，建议不要用getExternalStorageDirectory（）
                    request.setDestinationInExternalFilesDir(context, appUpdate.getSavePath(), context.getPackageName() + ".apk");
                    deleteApkFile(Objects.requireNonNull(context.getExternalFilesDir(appUpdate.getSavePath() + File.separator + context.getPackageName() + ".apk")));
                }

                // 设置通知栏的标题
                request.setTitle(getAppName());
                // 设置通知栏的描述
                request.setDescription("正在下载中...");
                // 设置媒体类型为apk文件
                request.setMimeType("application/vnd.android.package-archive");
                // 开启下载，返回下载id
                lastDownloadId = downloadManager.enqueue(request);
                // 如需要进度及下载状态，增加下载监听
                if (!appUpdate.getIsSlentMode()) {
                    DownloadHandler downloadHandler = new DownloadHandler(this);
                    downloadObserver = new DownloadObserver(downloadHandler, downloadManager, lastDownloadId);
                    context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, downloadObserver);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 防止有些厂商更改了系统的downloadManager
            downloadFromBrowse();
        }
    }

    /**
     * 设置下载的进度
     *
     * @param progress 进度
     */
    public void setProgress(int progress) {
        if (updateRemindDialog != null) {
            updateRemindDialog.setProgress(progress);
        }
    }

    /**
     * 取消下载的监听
     */
    public void unregisterContentObserver() {
        if (wrfContext.get() != null) {
            wrfContext.get().getContentResolver().unregisterContentObserver(downloadObserver);
        }
    }

    /**
     * 显示下载失败
     */
    public void showFail() {
        if (updateRemindDialog != null) {
            updateRemindDialog.showFailBtn();
        }
    }

    /**
     * 关闭提醒弹框
     */
    public void dismissDialog() {
        if (updateRemindDialog != null && updateRemindDialog.isShowing && wrfContext.get() != null && !((Activity) wrfContext.get()).isFinishing()) {
            updateRemindDialog.dismiss();
        }
    }

    /**
     * 检查本地是否有已经下载的最新apk文件
     *
     * @param filePath 文件相对路劲
     */
    private File checkLocalUpdate(String filePath) {
        try {
            Context context = wrfContext.get();
            File apkFile;
            if (TextUtils.isEmpty(filePath)) {
                apkFile = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + File.separator + context.getPackageName() + ".apk");
            } else {
                apkFile = context.getExternalFilesDir(filePath + File.separator + context.getPackageName() + ".apk");
            }
            // 注意系统的getExternalFilesDir（）方法如果找不到文件会默认当成目录创建
            if (apkFile != null && apkFile.isFile()) {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) {
                    long apkVersionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? packageInfo.getLongVersionCode() : packageInfo.versionCode;
                    if (apkVersionCode > getAppCode()) {
                        return apkFile;
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "checkLocalUpdate:本地目录没有已经下载的新版本");
        }
        return null;
    }

    /**
     * 下载前清空本地缓存的文件
     */
    private void deleteApkFile(File destFileDir) {
        if (!destFileDir.exists()) {
            return;
        }
        if (destFileDir.isDirectory()) {
            File[] files = destFileDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteApkFile(f);
                }
            }
        }
        destFileDir.delete();
    }

    /**
     * 获取应用程序名称
     *
     * @return 应用名称
     */
    private String getAppName() {
        try {
            Context context = wrfContext.get();
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "下载";
    }

    /**
     * 获取应用的版本号
     *
     * @return 应用版本号
     */
    private long getAppCode() {
        try {
            Context context = wrfContext.get();
            //获取包管理器
            PackageManager pm = context.getPackageManager();
            //获取包信息
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? packageInfo.getLongVersionCode() : packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 从浏览器打开下载，暂时没有选择应用市场，因为应用市场太多，而且协议不同，无法兼顾所有
     */
    private void downloadFromBrowse() {
        try {
            String downloadUrl = TextUtils.isEmpty(appUpdate.getDownBrowserUrl()) ? appUpdate.getNewVersionUrl() : appUpdate.getDownBrowserUrl();
            Intent intent = new Intent();
            Uri uri = Uri.parse(downloadUrl);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            wrfContext.get().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "无法通过浏览器下载！");
        }
    }

    /**
     * 清除上一个任务，防止apk重复下载
     */
    public void clearCurrentTask() {
        try {
            if (lastDownloadId != -1) {
                downloadManager.remove(lastDownloadId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void forceExit() {
        // 回到退出整个应用
        if (mainPageExtraListener != null) {
            mainPageExtraListener.forceExit();
        }
    }

    @Override
    public void updateDownLoad() {
        // 立即更新
        File apkFile = checkLocalUpdate(appUpdate.getSavePath());
        if (apkFile != null) {
            // 本地存在新版本，直接安装
            installApp(apkFile);
        } else {
            // 不存在新版本，需要下载
            if (!appUpdate.getIsSlentMode()) {
                // 非静默模式，直接在下载更新框内部显示下载进度
                updateRemindDialog.showProgressBtn();
            } else {
                // 静默模式，不显示下载进度
                dismissDialog();
            }
            // 开启下载
            downLoadApk();
        }
    }

    @Override
    public void updateRetry() {
        // 重试
        File apkFile = checkLocalUpdate(appUpdate.getSavePath());
        if (apkFile != null) {
            // 本地存在新版本，直接安装
            installApp(apkFile);
        } else {
            // 不存在新版本，需要下载
            if (!appUpdate.getIsSlentMode()) {
                // 非静默模式，直接在下载更新框内部显示下载进度
                updateRemindDialog.showProgressBtn();
            }
            // 开启下载
            downLoadApk();
        }
    }

    @Override
    public void downFromBrowser() {
        // 从浏览器下载
        downloadFromBrowse();
    }

    @Override
    public void cancelUpdate() {
        // 取消更新
        clearCurrentTask();
        dismissDialog();
        if (0 != appUpdate.getForceUpdate()) {
            forceExit();
        }
    }

    /**
     * 安装app
     *
     * @param apkFile 下载的文件
     */
    public void installApp(File apkFile) {
        try {
            // 本地覆盖安装前取消更新弹框
            if (updateRemindDialog != null && updateRemindDialog.isShowing) {
                updateRemindDialog.dismiss();
            }
            Context context = wrfContext.get();
            // 验证md5
            if (!TextUtils.isEmpty(appUpdate.getMd5())) {
                boolean md5IsRight = Md5Util.checkFileMd5(appUpdate.getMd5(), apkFile);
                if (!md5IsRight) {
                    Toast.makeText(context, "为了安全性和更好的体验，为你推荐浏览器下载更新！", Toast.LENGTH_SHORT).show();
                    downloadFromBrowse();
                    return;
                }
            }
            // 安装
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    boolean allowInstall = context.getPackageManager().canRequestPackageInstalls();
                    if (!allowInstall) {
                        //不允许安装未知来源应用，请求安装未知应用来源的权限
                        if (mainPageExtraListener != null) {
                            mainPageExtraListener.applyAndroidOInstall();
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
        }
    }

    /**
     * 重新安装app
     */
    public void installAppAgain() {
        Context context = wrfContext.get();
        if (context != null) {
            try {
                File downloadFile = getDownloadFile();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    intent.setDataAndType(Uri.fromFile(downloadFile), "application/vnd.android.package-archive");
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        boolean allowInstall = context.getPackageManager().canRequestPackageInstalls();
                        if (!allowInstall) {
                            //不允许安装未知来源应用，请求安装未知应用来源的权限
                            if (mainPageExtraListener != null) {
                                mainPageExtraListener.applyAndroidOInstall();
                            }
                            return;
                        }
                    }
                    //Android7.0之后获取uri要用contentProvider
                    Uri apkUri = FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName() + ".fileProvider", downloadFile);
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


    /**
     * 获取下载的文件
     *
     * @return file
     */
    public File getDownloadFile() {
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = downloadManager.query(query.setFilterById(lastDownloadId));
        if (cursor != null && cursor.moveToFirst()) {
            String fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String apkPath = Uri.parse(fileUri).getPath();
            if (!TextUtils.isEmpty(apkPath)) {
                return new File(apkPath);
            }
            cursor.close();
        }
        return null;
    }

}
