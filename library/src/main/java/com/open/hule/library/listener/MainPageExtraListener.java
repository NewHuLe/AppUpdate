package com.open.hule.library.listener;

/**
 * @author hule
 * @date 2019/7/10 15:48
 * description:主要是返回给调用检查更新的主页面来使用，是否需要强制退出及申请未知来源应用权限等
 */
public interface MainPageExtraListener {
    /**
     * 强制退出，回调给app处理退出应用
     */
    void forceExit();

    /**
     *  申请androidO 安装未知应用的权限
     */
    void applyAndroidOInstall();
}
