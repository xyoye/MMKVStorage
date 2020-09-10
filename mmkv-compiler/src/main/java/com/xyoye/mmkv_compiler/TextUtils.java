package com.xyoye.mmkv_compiler;

import com.google.common.base.CaseFormat;

/**
 * Created by xyoye on 2020/9/10.
 */

public class TextUtils {

    /**
     * 字符串为null或空
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.length() == 0;
    }

    /**
     * 字符串不空
     */
    public static boolean isNoEmpty(String text) {
        return text != null && text.length() > 0;
    }

    /**
     * 首字母大写
     */
    public static String toUpperCaseInitials(String text) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, text);
    }

}
