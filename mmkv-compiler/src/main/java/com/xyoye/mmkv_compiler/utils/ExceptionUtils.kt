package com.xyoye.mmkv_compiler.utils

import kotlin.reflect.KClass

/**
 * Created by xyoye on 2020/9/18.
 */

fun throwException(msg: String) {
    println("-------------ERROR start-----------------")
    println(msg)
    println("-------------ERROR end-------------------")
    throw RuntimeException(msg)
}

fun throwException(clazz: KClass<*>, msg: String) {
    println("-------------ERROR start-----------------")
    println(msg + ": ${clazz.simpleName}")
    println("-------------ERROR end-------------------")
    throw RuntimeException(msg)
}