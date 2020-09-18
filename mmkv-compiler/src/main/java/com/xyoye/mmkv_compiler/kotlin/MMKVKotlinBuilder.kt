package com.xyoye.mmkv_compiler.kotlin

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass
import com.xyoye.mmkv_compiler.utils.MMKVElementUtils
import com.xyoye.mmkv_compiler.utils.MMKVKtElementUtils
import com.xyoye.mmkv_compiler.utils.TextUtils
import com.xyoye.mmkv_compiler.utils.throwException
import javax.lang.model.element.Element
import kotlin.reflect.KClass

/**
 * Created by xyoye on 2020/9/18.
 */

object MMKVKotlinBuilder {

    /**
     * 生成kotlin文件
     */
    fun buildKotlinFileSpec(element: Element): FileSpec {
        //获取类注解
        val mmkvClazzAnnotation = element.getAnnotation(MMKVKotlinClass::class.java)
        if (mmkvClazzAnnotation == null) {
            throwException(MMKVKotlinClass::class, "在类中无法找到注解($element)")
        }

        //是否有自定义的类名
        val fileName = if (mmkvClazzAnnotation.className.isEmpty())
            "${element.simpleName}_MMKV"
        else
            mmkvClazzAnnotation.className

        //获取类的构造器
        val clazzBuilder = getKotlinClazzBuilder(fileName, mmkvClazzAnnotation.initMMKV)

        //遍历带@MMKVFiled注解的属性
        for (valueElement in element.enclosedElements) {
            val mmkvData = valueElement.getAnnotation(MMKVFiled::class.java)
            if (mmkvData != null) {
                //检查是否支持自动生成方法，并获取需要生成的类型
                val clazz = MMKVKtElementUtils.checkSupportGenerate(valueElement)

                //获取key
                val key = MMKVElementUtils.getMMKVKey(valueElement, mmkvData.key)
                //获取默认值
                val defaultValue = element.asType().toString() + "." + valueElement

                //生成put方法
                val putMethodSpec = generatePutMethod(valueElement, clazz, key, mmkvData.commit)
                clazzBuilder.addFunction(putMethodSpec)

                //生成get方法
                val getMethodSpec = generateGetMethod(valueElement, clazz, key, defaultValue)
                clazzBuilder.addFunction(getMethodSpec)
            }
        }

        //生成的类在对应注解类的包下
        val className = ClassName.bestGuess(element.asType().toString())
        return FileSpec.builder(className.packageName, fileName)
                .addType(clazzBuilder.build())
                .build()
    }

    /**
     * 生成类的构造器
     */
    private fun getKotlinClazzBuilder(fileName: String, initMMKV: Boolean): TypeSpec.Builder {
        val typeSpecBuilder = TypeSpec.objectBuilder(fileName)

        //引入MMKV类
        val mmkvBundle = ClassName("com.tencent.mmkv", "MMKV")
        //属性构造器
        val propertySpec = PropertySpec.builder("mmkv", mmkvBundle)

        //是否自定义mmkv的初始化
        if (initMMKV) {
            //私有、延迟实例化、可变
            propertySpec.addModifiers(KModifier.PRIVATE, KModifier.LATEINIT).mutable()

            //添加mmkv初始化方法
            typeSpecBuilder.addFunction(FunSpec.builder("initMMKV")
                    .addParameter("initMMKV", mmkvBundle)
                    .addStatement("mmkv = initMMKV")
                    .build()
            )

        } else {
            //添加mmkv默认初始化方法
            propertySpec.initializer("MMKV.defaultMMKV()")
                    .addModifiers(KModifier.PRIVATE)
        }

        return typeSpecBuilder.addProperty(propertySpec.build())
    }

    /**
     * 生成put方法
     */
    private fun generatePutMethod(element: Element, clazz: KClass<*>, key: String, isCommit: Boolean): FunSpec {
        //方法名
        val putMethodName = "put${TextUtils.toUpperCaseInitials(element.toString())}"

        val funBuilder = FunSpec.builder(putMethodName)

        //set需要使用泛型
        if (clazz == Set::class) {
            funBuilder.addParameter("value", SET.parameterizedBy(String::class.asTypeName()))
        } else {
            funBuilder.addParameter("value", clazz.asTypeName())
        }

        //mmkv方法名
        val mmkvMethodName = "put${MMKVKtElementUtils.getMMKVMethodName(clazz)}"
        if (isCommit) {
            //使用commit，有boolean值返回
            funBuilder.addStatement("return mmkv.$mmkvMethodName(\"$key\", value).commit()")
                    .returns(Boolean::class)
        } else {
            funBuilder.addStatement("mmkv.$mmkvMethodName(\"$key\", value)")
        }

        return funBuilder.build()
    }


    /**
     * 生成get方法
     */
    private fun generateGetMethod(element: Element, clazz: KClass<*>, key: String, defaultValue: String): FunSpec {
        //方法名
        val getMethodName = "get${TextUtils.toUpperCaseInitials(element.toString())}"

        //mmkv方法名
        val mmkvMethodName = "get${MMKVKtElementUtils.getMMKVMethodName(clazz)}"

        return FunSpec.builder(getMethodName)
                //方法内容
                .addStatement("return mmkv.$mmkvMethodName(\"$key\", $defaultValue)")
                //返回值类型
                .returns(when (clazz) {
                    Set::class -> MUTABLE_SET.parameterizedBy(String::class.asTypeName()).copy(nullable = true)
                    String::class, ByteArray::class -> clazz.asTypeName().copy(nullable = true)
                    else -> clazz.asTypeName()
                }).build()
    }
}