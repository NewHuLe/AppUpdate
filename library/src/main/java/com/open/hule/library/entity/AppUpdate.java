package com.open.hule.library.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.open.hule.library.R;

/**
 * @author hule
 * @date 2019/7/10 15:48
 * description:app下载更新的实体类,通过服务器返回的更新实体中取出赋值
 */
public class AppUpdate implements Parcelable {

    /**
     * 新版本的下载地址
     */
    private String newVersionUrl;
    /**
     * 新版本号
     */
    private String newVersionCode;
    /**
     * 是否采取强制更新，默认为0，不采取强制更新，否则强制更新
     */
    private int forceUpdate;
    /**
     * 新版本更新的内容
     */
    private String updateInfo;
    /**
     * 新版本文件的大小,单位一般为M，需要自己换算，因为不知道保留的位数，根据自己需求吧
     */
    private String fileSize;

    /**
     * 文件下的保存路径 以/开头 比如 /A/B
     */
    private String savePath;

    /**
     * 1.apk文件的md5值，用于校验apk文件签名是否一致，防止下载被拦截，
     * 2.用于校验文件大小的完整性
     * 3.若无MD5值，那么安装时不去校验
     */
    private String md5;
    /**
     * 浏览器的下载地址，如果下载失败，通过浏览器下载
     */
    private String downBrowserUrl;

    /**
     * 更新提示框的title提示语
     */
    private int updateTitle;
    /**
     * 更新内容的提示语
     */
    private int updateContentTitle;

    /**
     * 更新按钮的文字
     */
    private int updateText;
    /**
     * 取消更新按钮的文字
     */
    private int updateCancelText;

    /**
     * 更新按钮的颜色
     */
    private int updateColor;
    /**
     * 取消更新按钮的颜色
     */
    private int updateCancelColor;

    /**
     * 下载进度条的颜色，二级进度
     */
    private int updateProgressColor;

    /**
     * 风格：true代表默认静默下载模式，只弹出下载更新框,下载完毕自动安装， false 代表配合使用进度框与下载失败弹框
     */
    private boolean isSilentMode;

    /**
     * 更新对话框的id
     */
    private int updateResourceId;


    public String getNewVersionUrl() {
        return newVersionUrl;
    }

    public String getNewVersionCode() {
        return newVersionCode;
    }

    public int getForceUpdate() {
        return forceUpdate;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getSavePath() {
        return savePath;
    }

    public String getMd5() {
        return md5;
    }

    public String getDownBrowserUrl() {
        return downBrowserUrl;
    }

    public int getUpdateTitle() {
        return updateTitle;
    }

    public int getUpdateContentTitle() {
        return updateContentTitle;
    }

    public int getUpdateText() {
        return updateText;
    }

    public int getUpdateCancelText() {
        return updateCancelText;
    }

    public int getUpdateColor() {
        return updateColor;
    }

    public int getUpdateCancelColor() {
        return updateCancelColor;
    }

    public int getUpdateProgressColor() {
        return updateProgressColor;
    }

    public boolean getIsSlentMode() {
        return isSilentMode;
    }

    public int getUpdateResourceId() {
        return updateResourceId;
    }

    private AppUpdate(Builder builder) {
        this.newVersionUrl = builder.newVersionUrl;
        this.newVersionCode = builder.newVersionCode;
        this.forceUpdate = builder.forceUpdate;
        this.updateInfo = builder.updateInfo;
        this.fileSize = builder.fileSize;
        this.savePath = builder.savePath;
        this.md5 = builder.md5;
        this.downBrowserUrl = builder.downBrowserUrl;
        this.updateTitle = builder.updateTitle;
        this.updateContentTitle = builder.updateContentTitle;
        this.updateText = builder.updateText;
        this.updateCancelText = builder.updateCancelText;
        this.updateColor = builder.updateColor;
        this.updateCancelColor = builder.updateCancelColor;
        this.updateProgressColor = builder.updateProgressColor;
        this.isSilentMode = builder.isSilentMode;
        this.updateResourceId = builder.updateResourceId;
    }

    /**
     * 构造者模式，链式调用，构建和表示分离，可读性好
     */
    public static class Builder {

        private String newVersionUrl;

        private String newVersionCode;

        /**
         * 默认不采取强制更新
         */
        private int forceUpdate = 0;

        private String updateInfo;

        private String fileSize;

        /**
         * 默认的保存路径
         */
        private String savePath = "/download/";

        private String md5;

        /**
         * 默认的市场下载地址
         */
        private String downBrowserUrl = "";

        /**
         * 更新提示框的title提示语
         */
        private int updateTitle = R.string.update_title;
        /**
         * 更新内容的提示语
         */
        private int updateContentTitle = R.string.update_content_lb;

        /**
         * 默认的更新文本
         */
        private int updateText = R.string.update_text;

        /**
         * 默认的取消更新文本
         */
        private int updateCancelText = R.string.update_later;

        /**
         * 默认的更新颜色
         */
        private int updateColor = R.color.color_blue;

        /**
         * 默认的取消更新文本颜色
         */
        private int updateCancelColor = R.color.color_blue;

        /**
         * 默认的更新进度条颜色
         */
        private int updateProgressColor = R.color.color_blue;

        /**
         * 风格：true代表默认静默下载模式，只弹出下载更新框,下载完毕自动安装， false 代表配合使用进度框与下载失败弹框
         */
        private boolean isSilentMode = true;
        /**
         * 更新对话框的id
         */
        private int updateResourceId = R.layout.dialog_update;

        public Builder newVersionUrl(String newVersionUrl) {
            this.newVersionUrl = newVersionUrl;
            return this;
        }

        public Builder newVersionCode(String newVersionCode) {
            this.newVersionCode = newVersionCode;
            return this;
        }

        public Builder forceUpdate(int forceUpdate) {
            this.forceUpdate = forceUpdate;
            return this;
        }

        public Builder updateInfo(String updateInfo) {
            this.updateInfo = updateInfo;
            return this;
        }

        public Builder fileSize(String fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder savePath(String saveFilePath) {
            this.savePath = saveFilePath;
            return this;
        }


        public Builder md5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Builder downBrowserUrl(String downBrowserUrl) {
            this.downBrowserUrl = downBrowserUrl;
            return this;
        }

        public Builder updateTitle(int updateTitle) {
            this.updateTitle = updateTitle;
            return this;
        }

        public Builder updateContentTitle(int updateContentTitle) {
            this.updateContentTitle = updateContentTitle;
            return this;
        }

        public Builder updateText(int updateTextResId) {
            this.updateText = updateTextResId;
            return this;
        }

        public Builder updateTextCancel(int updateCancelResId) {
            this.updateCancelText = updateCancelResId;
            return this;
        }

        public Builder updateColor(int updateColorResId) {
            this.updateColor = updateColorResId;
            return this;
        }


        public Builder updateCancelColor(int updateCancelColorResId) {
            this.updateCancelColor = updateCancelColorResId;
            return this;
        }

        public Builder updateProgressColor(int updateProgressColorResId) {
            this.updateProgressColor = updateProgressColorResId;
            return this;
        }

        public Builder isSilentMode(boolean isSilentMode) {
            this.isSilentMode = isSilentMode;
            return this;
        }

        public Builder updateResourceId(int updateResourceId) {
            this.updateResourceId = updateResourceId;
            return this;
        }

        public AppUpdate build() {
            return new AppUpdate(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.newVersionUrl);
        dest.writeString(this.newVersionCode);
        dest.writeInt(this.forceUpdate);
        dest.writeString(this.updateInfo);
        dest.writeString(this.fileSize);
        dest.writeString(this.savePath);
        dest.writeString(this.md5);
        dest.writeString(this.downBrowserUrl);
        dest.writeInt(this.updateTitle);
        dest.writeInt(this.updateContentTitle);
        dest.writeInt(this.updateText);
        dest.writeInt(this.updateCancelText);
        dest.writeInt(this.updateColor);
        dest.writeInt(this.updateCancelColor);
        dest.writeInt(this.updateProgressColor);
        dest.writeByte(this.isSilentMode ? (byte) 1 : (byte) 0);
        dest.writeInt(this.updateResourceId);
    }

    protected AppUpdate(Parcel in) {
        this.newVersionUrl = in.readString();
        this.newVersionCode = in.readString();
        this.forceUpdate = in.readInt();
        this.updateInfo = in.readString();
        this.fileSize = in.readString();
        this.savePath = in.readString();
        this.md5 = in.readString();
        this.downBrowserUrl = in.readString();
        this.updateTitle = in.readInt();
        this.updateContentTitle = in.readInt();
        this.updateText = in.readInt();
        this.updateCancelText = in.readInt();
        this.updateColor = in.readInt();
        this.updateCancelColor = in.readInt();
        this.updateProgressColor = in.readInt();
        this.isSilentMode = in.readByte() != 0;
        this.updateResourceId = in.readInt();
    }

    public static final Creator<AppUpdate> CREATOR = new Creator<AppUpdate>() {
        @Override
        public AppUpdate createFromParcel(Parcel source) {
            return new AppUpdate(source);
        }

        @Override
        public AppUpdate[] newArray(int size) {
            return new AppUpdate[size];
        }
    };
}
