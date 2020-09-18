package com.xyoye.mmkv_compiler.java;

import com.google.auto.service.AutoService;
import com.xyoye.mmkv_annotation.MMKVJavaClass;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import kotlin.Metadata;

/**
 * Created by xyoye on 2020/9/10.
 */

@AutoService(Processor.class)
public class MMKVJavaCompiler extends AbstractProcessor {

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        filer = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(MMKVJavaClass.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MMKVJavaClass.class);
        if (elements == null || elements.isEmpty())
            return false;

        //根据注解生成方法
        for (Element element : elements) {
            generateJavaFile(element);
        }
        return false;
    }

    private void generateJavaFile(Element element) {
        if (element.getAnnotation(Metadata.class) != null) {
            throw new RuntimeException("注解不能用于kotlin类(" + element + ") : " + MMKVJavaClass.class.getSimpleName());
        }

        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.FINAL)) {
            throw new RuntimeException("@MMKVClass cannot used in final class! : " + element +
                    "\n@MMKVData注解不能用于final类 : " + element);
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            throw new RuntimeException("@MMKVClass cannot used in private class! : " + element +
                    "\n@MMKVData注解不能用于私有类 : " + element);
        }
        try {
            //生成并写入文件
            MMKVJavaBuilder.buildJavaFileSpec(element).writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}