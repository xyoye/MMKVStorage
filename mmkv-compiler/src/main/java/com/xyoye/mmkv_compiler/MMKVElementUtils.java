package com.xyoye.mmkv_compiler;

import com.google.common.base.CaseFormat;

import java.util.Set;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * Created by xyoye on 2020/9/10.
 */

public class MMKVElementUtils {

    /**
     * 获取存储的Key值
     * <p>
     * 例：String mmkvData -> key_mmkv_data
     */
    static String getMMKVKey(Element element, String defaultKey) {
        if (TextUtils.isNoEmpty(defaultKey))
            return defaultKey;

        String filedName = element.toString();
        if (TextUtils.isNullOrEmpty(filedName)) {
            throw new RuntimeException("@MMKVData filed name is empty");
        }
        String filedNameFormat = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, filedName);
        return "key_" + filedNameFormat;
    }

    /**
     * 获取默认值
     * <p>
     * 例： final String mmkvData = "123" -> "123"
     */
    @Nullable
    static String getDefaultValueString(Element element, Class<?> clazz) {
        if (clazz == byte[].class || clazz == Set.class)
            return null;
        Object defaultValue = getDefaultValue(element);
        if (clazz == String.class) {
            return defaultValue == null ? null : "\"" + defaultValue.toString() + "\"";
        } else if (clazz == int.class) {
            return defaultValue == null ? "0" : defaultValue.toString();
        } else if (clazz == float.class) {
            return defaultValue == null ? "0f" : defaultValue.toString() + "f";
        } else if (clazz == long.class) {
            return defaultValue == null ? "0L" : defaultValue.toString() + "L";
        } else if (clazz == boolean.class) {
            return defaultValue == null ? "float" : defaultValue.toString();
        }
        throw new RuntimeException("UKnow class : " + clazz.getName());
    }

    /**
     * 获取定义为final的变量的值
     */
    private static Object getDefaultValue(Element element) {
        //判断字段是否为final
        if (element.getModifiers().contains(Modifier.FINAL)) {
            //判断element是否可获取值
            if (element instanceof VariableElement) {
                VariableElement variableElement = (VariableElement) element;
                return variableElement.getConstantValue();
            }
        }
        return null;
    }

    /**
     * 获取MMKV的方法名
     */
    static String getMMKVMethodName(Class<?> clazz) {
        if (clazz == byte[].class) {
            return "Bytes";
        } else if (clazz == Set.class) {
            return "StringSet";
        } else {
            return TextUtils.toUpperCaseInitials(clazz.getSimpleName());
        }
    }
}
