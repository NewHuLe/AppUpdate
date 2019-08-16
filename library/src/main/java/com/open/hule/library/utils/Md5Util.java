package com.open.hule.library.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author hule
 * @date 2019/7/11 10:00
 * description: MD5工具类
 * 1.apk文件的md5值，用于校验apk文件签名是否一致，防止下载被拦截，
 * 2.用于校验文件大小的完整性
 */
public class Md5Util {

    /**
     * 检查文件的MD5的合法性，若不一致，则无法安装
     *
     * @param md5  服务器返回的文件md5值
     * @param file 下载的apk文件
     * @return true 则md5校验通过 false 则失败
     */
    public static boolean checkFileMd5(String md5, File file) {
        if (TextUtils.isEmpty(md5)) {
            return false;
        }
        String md5OfFile = getFileMd5ToString(file);
        if (TextUtils.isEmpty(md5OfFile)) {
            return false;
        }
        return md5.equalsIgnoreCase(md5OfFile);
    }

    /**
     * Return the MD5 of file.
     *
     * @param file The file.
     * @return the md5 of file
     */
    private static String getFileMd5ToString(final File file) {
        return bytes2HexString(getFileMd5(file));
    }

    private static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static String bytes2HexString(final byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        int len = bytes.length;
        if (len <= 0) {
            return "";
        }
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = HEX_DIGITS[bytes[i] >> 4 & 0x0f];
            ret[j++] = HEX_DIGITS[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

    /**
     * Return the MD5 of file.
     *
     * @param file The file.
     * @return the md5 of file
     */
    private static byte[] getFileMd5(final File file) {
        if (file == null) {
            return null;
        }
        DigestInputStream dis = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(fis, md);
            byte[] buffer = new byte[1024 * 256];
            while (true) {
                if (dis.read(buffer) <= 0) {
                    break;
                }
            }
            md = dis.getMessageDigest();
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
