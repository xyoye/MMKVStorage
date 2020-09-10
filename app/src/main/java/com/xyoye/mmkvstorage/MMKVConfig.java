package com.xyoye.mmkvstorage;

import com.xyoye.mmkv_annotation.MMKVData;

import java.util.Set;

/**
 * Created by xyoye on 2020/9/10.
 *
 * 使用示例
 */

public class MMKVConfig {
    //例1：设置默认值4
    @MMKVData
    private static final int mmkvInt = 3;

    //例2：不设置默认值，默认值为0
    @MMKVData
    private static int mmkvIntV2;

    //例3：设置自定义key
    @MMKVData(key = "key_test_int")
    private static final int mmkvIntV3 = 3;

    //例4：设置自定义key，并使用commit提交
    @MMKVData(key = "key_test_int_v4", commit = true)
    private static final int mmkvIntV4 = 3;

    //设置默认值为true
    @MMKVData
    private static final boolean mmkvBoolean = true;

    //设置默认值为xyoye
    @MMKVData
    private static final String mmkvString = "xyoye";

    //设置默认值为7
    @MMKVData
    private static final long mmkvLong = 7L;

    //设置默认值为8.1
    @MMKVData
    private static final float mmkvFloat = 8.1f;

    //无法设置默认值， 默认值为null
    @MMKVData
    private static Set<String> mmkvSet;

    //无法设置默认值， 默认值为null
    @MMKVData
    private static byte[] mmkvByteArrays;
}
