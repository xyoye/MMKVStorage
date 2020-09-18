package com.xyoye.mmkv_compiler.utils

import javax.lang.model.element.Element
import kotlin.reflect.KClass

/**
 * Created by xyoye on 2020/9/18.
 */

object MMKVKtElementUtils {

    /**
     * 检查是否支持自动生成方法
     */
    fun checkSupportGenerate(element: Element): KClass<*> {
        //在Kotlin使用注解，获取的拆箱类的typeName都是java类，原因未知
        return when (element.asType().toString()) {
            "java.lang.String" -> String::class
            "int" -> Int::class
            "boolean" -> Boolean::class
            "float" -> Float::class
            "long" -> Long::class
            "byte[]" -> ByteArray::class
            "java.util.Set<java.lang.String>" -> Set::class
            else -> {
                throwException("该类型不支持自动生成方法：${element.asType()}")
                return String::class
            }
        }
    }

    /**
     * 获取MMKV的方法名
     */
    fun getMMKVMethodName(clazz: KClass<*>): String {
        return when (clazz) {
            ByteArray::class -> {
                "Bytes"
            }
            Set::class -> {
                "StringSet"
            }
            else -> {
                TextUtils.toUpperCaseInitials(clazz.simpleName)
            }
        }
    }

}