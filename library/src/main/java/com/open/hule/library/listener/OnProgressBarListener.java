package com.open.hule.library.listener;

/**
 * @author hule
 * @date 2019/7/10 15:48
 * description:进度条的监听，获取当前值和最大值
 */
public interface OnProgressBarListener {

    /**
     * onProgressChange
     *
     * @param current 当前进度
     * @param max     最大进度
     */
    void onProgressChange(int current, int max);
}
