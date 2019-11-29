package com.open.hule.library.view;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

import com.open.hule.library.R;

/**
 * @author hule
 * @date 2019/8/15 15:19
 * description:
 */
public abstract class BaseDialog extends AppCompatDialogFragment {

    /**
     * 是否正在显示,防止在特殊情况下弹出多层
     */
    public boolean isShowing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        setStyle(AppCompatDialogFragment.STYLE_NO_TITLE, R.style.BaseDialogFragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window dialogWindow = dialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setGravity(Gravity.CENTER);
                dialogWindow.setLayout(getWidth(dialogWindow) * 9 / 10, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getLayoutId() == 0) {
            throw new NullPointerException("请在getLayoutId()方法中传入布局Id");
        }
        return inflater.inflate(getLayoutId(), container, false);
    }

    /**
     * 获取屏幕的宽度
     *
     * @param dialogWindow Window
     * @return width
     */
    private int getWidth(Window dialogWindow) {
        WindowManager wm = dialogWindow.getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }


    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        try {
            if (isShowing) {
                return;
            }
            super.show(manager, tag);
            isShowing = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            // 不要使用super.dismiss()，会出现Can not perform this action after onSaveInstanceState异常
            // 当Activity被杀死或者按下Home回调用系统的onSaveInstance(),保存状态后，如果再次执行dismiss()会报错
            super.dismissAllowingStateLoss();
            isShowing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 资源布局Id
     *
     * @return layoutId
     */
    abstract int getLayoutId();
}
