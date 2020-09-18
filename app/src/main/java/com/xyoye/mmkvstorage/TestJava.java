package com.xyoye.mmkvstorage;

import com.xyoye.mmkv_annotation.MMKVFiled;
import com.xyoye.mmkv_annotation.MMKVJavaClass;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xyoye on 2020/9/10.
 * <p>
 * java 使用示例
 */

@MMKVJavaClass
public class TestJava {
    //例1：设置默认值4
    @MMKVFiled
    protected static final int mmkvInt = KotlinEnum.TYPE_EXO_PLAYER.getValue();

    //例2：不设置默认值，默认值为0
    @MMKVFiled
    protected static int mmkvIntV2;

    //例3：设置自定义key
    @MMKVFiled(key = "key_test_int")
    protected static final int mmkvIntV3 = 3;

    //例4：设置自定义key，并使用commit提交
    @MMKVFiled(key = "key_test_int_v4", commit = true)
    protected static final int mmkvIntV4 = 3;

    //设置默认值为true
    @MMKVFiled
    protected static boolean mmkvBoolean = true;

    //设置默认值为xyoye
    @MMKVFiled
    protected static String mmkvString = "xyoye";

    //设置默认值为7
    @MMKVFiled
    protected static final long mmkvLong = 7L;

    //设置默认值为8.1
    @MMKVFiled
    protected static final float mmkvFloat = 8.1f;

    //设置默认值为HashSet
    @MMKVFiled
    protected static Set<String> mmkvSet = new HashSet<>();

    //设置默认值为byte[]{1, 2}
    @MMKVFiled
    protected static byte[] mmkvByteArrays = new byte[]{1, 2};
}
