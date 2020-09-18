package com.xyoye.mmkvstorage

/**
 * Created by xyoye on 2020/9/16.
 */

enum class KotlinEnum(val value: Int) {
    TYPE_IJK_PLAYER(1),
    TYPE_EXO_PLAYER(2);


    companion object {
        fun valueOf(value: Int): KotlinEnum {
            return when (value) {
                1 -> TYPE_IJK_PLAYER
                else -> TYPE_EXO_PLAYER
            }
        }
    }
}