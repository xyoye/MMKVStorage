package com.xyoye.mmkv_compiler.utils;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.Type;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Created by xyoye on 2020/9/10.
 */

public class MMKVElementUtils {
    private final static Class<?>[] supportClazz = new Class[]{
            int.class,
            float.class,
            long.class,
            boolean.class
    };

    /**
     * 获取存储的Key值
     * <p>
     * 例：String mmkvData -> key_mmkv_data
     */
    public static String getMMKVKey(Element element, String defaultKey) {
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
     * 获取定义为静态的变量
     * <p>
     * 例： final String mmkvData = "123" -> "包名.类名.mmkvData"
     */
    public static String getDefaultValueString(Element clazzElement, Element valueElement) {
        //判断字段是否为静态变量
        Set<Modifier> modifiers = valueElement.getModifiers();
        if (modifiers.contains(Modifier.STATIC) && (modifiers.contains(Modifier.PROTECTED) || modifiers.contains(Modifier.PUBLIC))) {
            return clazzElement + "." + valueElement.getSimpleName();
        }
        return null;
    }

    /**
     * 获取MMKV的方法名
     */
    public static String getMMKVMethodName(Class<?> clazz) {
        if (clazz == byte[].class) {
            return "Bytes";
        } else if (clazz == Set.class) {
            return "StringSet";
        } else {
            return TextUtils.toUpperCaseInitials(clazz.getSimpleName());
        }
    }


    /**
     * 检查是否支持自动生成方法
     */
    public static Class<?> checkSupportGenerate(Element element) {
        TypeName elementTypeName = TypeName.get(element.asType());

        //支持String类型
        TypeName stringTypeName = TypeName.get(String.class);
        if (elementTypeName.equals(stringTypeName)) {
            return String.class;
        }

        //支持byte数组
        TypeName byteArrayName = TypeName.get(byte[].class);
        if (elementTypeName.equals(byteArrayName)) {
            return byte[].class;
        }

        //支持Set<String>
        ParameterizedTypeImpl setTypeImpl = ParameterizedTypeImpl.make(Set.class, new Type[]{String.class}, null);
        TypeName setTypeName = TypeName.get(setTypeImpl);
        if (elementTypeName.equals(setTypeName)) {
            return Set.class;
        }

        //不支持装箱的基本类型
        if (elementTypeName.isBoxedPrimitive()) {
            throw new RuntimeException("@MMKVData not a unboxed primitive filed! unboxed! : " + elementTypeName +
                    "\n@MMKVData注解的变量类型不是未装箱的类型 : " + elementTypeName);
        }

        //只支持基本类型
        if (!elementTypeName.isPrimitive()) {
            throw new RuntimeException("@MMKVData not a primitive filed! primitive! : " + elementTypeName +
                    "\n@MMKVData注解的变量类型不是基本类型 : " + elementTypeName);
        }

        //支持特定的基本类型
        for (Class<?> clazz : supportClazz) {
            TypeName supportTypeName = TypeName.get(clazz);
            if (supportTypeName.equals(elementTypeName)) {
                return clazz;
            }
        }
        throw new RuntimeException(elementTypeName + " does not support generation" +
                "\n" + elementTypeName + "不支持自动生成方法");
    }
}
