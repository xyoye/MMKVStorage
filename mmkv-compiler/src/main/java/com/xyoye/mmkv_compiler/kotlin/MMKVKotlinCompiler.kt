package com.xyoye.mmkv_compiler.kotlin

import com.squareup.kotlinpoet.asClassName
import com.xyoye.mmkv_annotation.MMKVKotlinClass
import com.xyoye.mmkv_compiler.utils.throwException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by xyoye on 2020/9/18.
 */

open class MMKVKotlinCompiler : AbstractProcessor() {
    lateinit var filer: Filer

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        filer = processingEnv?.filer!!
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val types = LinkedHashSet<String>()
        types.add(MMKVKotlinClass::class.asClassName().canonicalName)
        return types
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val elements = roundEnv?.getElementsAnnotatedWith(MMKVKotlinClass::class.java)
        if (elements == null || elements.isEmpty())
            return false

        for (element in elements) {
            element.getAnnotation(Metadata::class.java)
                    ?: throwException(MMKVKotlinClass::class, "注解不支持在java类中使用($element)")

            if (element.modifiers.contains(Modifier.PRIVATE))
                throwException(MMKVKotlinClass::class, "注解不能用于私有类($element)")

            //生成并写入文件
            MMKVKotlinBuilder.buildKotlinFileSpec(element).writeTo(filer)
        }

        return false
    }
}