package com.xyoye.mmkvstorage;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取数据，得到默认值3
        int intData = TestJava_MMKV.getMmkvInt();
        Log.d(TAG, "java 默认值：" + intData);

        //更新数据
        TestJava_MMKV.putMmkvInt(123);
        Log.d(TAG, "java 更新数据：123");

        //获取到新的数据为123
        int newIntData = TestJava_MMKV.getMmkvInt();
        Log.d(TAG, "java 获取到的新数据：" + newIntData);

        //获取数据，得到默认值3
        int intKotlinData = TestKotlin_MMKV.INSTANCE.getKotlinValueInt();
        Log.d(TAG, "kotlin 默认值：" + intKotlinData);

        //更新数据
        TestKotlin_MMKV.INSTANCE.putKotlinValueInt(123);
        Log.d(TAG, "kotlin 更新数据：123");

        //获取到新的数据为123
        int newIntKotlinData = TestKotlin_MMKV.INSTANCE.getKotlinValueInt();
        Log.d(TAG, "kotlin 获取到的新数据：" + newIntKotlinData);
    }
}