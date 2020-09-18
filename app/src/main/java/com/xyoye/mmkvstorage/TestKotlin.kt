package com.xyoye.mmkvstorage

import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

/**
 * Created by xyoye on 2020/9/16.
 *
 * kotlin 使用示例
 */

@MMKVKotlinClass
object TestKotlin {

    @MMKVFiled
    val kotlinValue2: String? = null

    @MMKVFiled(commit = true)
    val kotlinValueInt = 3

    @MMKVFiled(key = "key_only_test_key")
    val kotlinValueBoolean = true

    @MMKVFiled(key = "key_test_key_and_commit", commit = true)
    val kotlinValueFloat = 5f

    @MMKVFiled
    val kotlinValueLong = 4L

    @MMKVFiled
    val kotlinValueByte = byteArrayOf(9.toByte(), 2.toByte())

    @MMKVFiled
    val kotlinValueSet = setOf("test", "kotlin")
}