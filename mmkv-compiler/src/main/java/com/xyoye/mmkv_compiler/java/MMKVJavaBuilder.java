package com.xyoye.mmkv_compiler.java;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.xyoye.mmkv_annotation.MMKVFiled;
import com.xyoye.mmkv_annotation.MMKVJavaClass;
import com.xyoye.mmkv_compiler.utils.MMKVElementUtils;
import com.xyoye.mmkv_compiler.utils.ParameterizedTypeImpl;
import com.xyoye.mmkv_compiler.utils.TextUtils;

import java.lang.reflect.Type;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by xyoye on 2020/9/18.
 */

public class MMKVJavaBuilder {

    /**
     * 生成java文件
     */
    public static JavaFile buildJavaFileSpec(Element element) {
        //获取MMKV类建造器
        TypeSpec.Builder clazzBuilder = getJavaClassBuilder(element).superclass(element.asType());
        //遍历文件总所有MMKVData注解
        for (Element valueElement : element.getEnclosedElements()) {
            MMKVFiled mmkvFiled = valueElement.getAnnotation(MMKVFiled.class);
            if (mmkvFiled != null) {
                //检查是否支持自动生成代码，并且获取变量类型
                Class<?> clazz = MMKVElementUtils.checkSupportGenerate(valueElement);

                //获取提交方式
                boolean isCommit = mmkvFiled.commit();
                //获取key
                String key = MMKVElementUtils.getMMKVKey(valueElement, mmkvFiled.key());
                //获取默认值
                String defaultValue = MMKVElementUtils.getDefaultValueString(element, valueElement);

                //生成put方法
                MethodSpec putMethodSpec = generatePutMethod(valueElement, clazz, key, isCommit);
                clazzBuilder.addMethod(putMethodSpec);

                //生成get方法
                MethodSpec getMethodSpec = generateGetMethod(valueElement, clazz, key, defaultValue);
                clazzBuilder.addMethod(getMethodSpec);
            }
        }

        //生成的类在对应注解类的包下
        ClassName className = ClassName.get((TypeElement) element);
        return JavaFile.builder(className.packageName(), clazzBuilder.build()).build();
    }

    /**
     * 获取StorageMMKV类的建造器
     */
    private static TypeSpec.Builder getJavaClassBuilder(Element element) {
        //引入类
        ClassName mmkvBundle = ClassName.get("com.tencent.mmkv", "MMKV");

        MMKVJavaClass classAnnotation = element.getAnnotation(MMKVJavaClass.class);
        if (classAnnotation == null) {
            throw new RuntimeException("在类中无法找到注解(" + element + ") : " + MMKVJavaClass.class.getSimpleName());
        }

        //是否有自定义的类名
        String fileName = TextUtils.isNoEmpty(classAnnotation.className())
                ? classAnnotation.className()
                : element.getSimpleName() + "_MMKV";

        //类建造器
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC);

        //使用者自定义mmkv的实例化方式
        if (classAnnotation.initMMKV()) {
            FieldSpec mmkvField = FieldSpec.builder(mmkvBundle, "mmkv", Modifier.PRIVATE, Modifier.STATIC).build();
            classBuilder.addField(mmkvField);
            //添加自定义的初始化方法
            classBuilder.addMethod(MethodSpec.methodBuilder("initMMKV")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ParameterSpec.builder(mmkvBundle, "initMMKV").build())
                    .addStatement("mmkv = initMMKV")
                    .build());

        } else {
            //私有属性，默认实例化
            FieldSpec mmkvField = FieldSpec.builder(mmkvBundle, "mmkv", Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("$T.defaultMMKV()", mmkvBundle)
                    .build();
            classBuilder.addField(mmkvField);
        }

        //构造方法
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
        classBuilder.addMethod(constructor);

        return classBuilder;
    }

    /**
     * 生成put方法
     */
    private static MethodSpec generatePutMethod(Element element, Class<?> clazz, String key, boolean isCommit) {
        //方法名，例：String mmkvData -> putMmkvData()
        String putMethodName = "put" + TextUtils.toUpperCaseInitials(element.toString());

        //方法参数，Set<String>需要特殊处理
        ParameterSpec valueParam;
        if (clazz == Set.class) {
            ParameterizedTypeImpl setTypeImpl = ParameterizedTypeImpl.make(Set.class, new Type[]{String.class}, null);
            TypeName setTypeName = TypeName.get(setTypeImpl);
            valueParam = ParameterSpec.builder(setTypeName, "value").build();
        } else {
            valueParam = ParameterSpec.builder(clazz, "value").build();
        }

        //方法内容，例：String mmkvData -> mmkv.putString("key_mmkv_data", value);
        String mmkvMethod = "put" + MMKVElementUtils.getMMKVMethodName(clazz);
        String statement;
        if (isCommit) {
            statement = "return mmkv." + mmkvMethod + "($S, value).commit()";
        } else {
            statement = "mmkv." + mmkvMethod + "($S, value).apply()";
        }

        //返回类型
        TypeName returnType = isCommit ? TypeName.BOOLEAN : TypeName.VOID;

        return MethodSpec.methodBuilder(putMethodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(valueParam)
                .addStatement(statement, key)
                .returns(returnType)
                .build();
    }

    /**
     * 生成get方法
     */
    private static MethodSpec generateGetMethod(Element element, Class<?> clazz, String key, Object defaultValue) {
        //方法名，例：String mmkvData -> getMmkvData()
        String methodType = clazz == boolean.class ? "is" : "get";
        String getMethodName = methodType + TextUtils.toUpperCaseInitials(element.toString());

        //方法内容，例：final String mmkvData = "123" -> mmkv.getString("key_mmkv_data", "123");
        String mmkvMethod = "get" + MMKVElementUtils.getMMKVMethodName(clazz);
        String statement = "return mmkv." + mmkvMethod + "($S, " + defaultValue + ")";

        //返回类型，Set<String>需要特殊处理
        TypeName returnTypeName;
        if (clazz == Set.class) {
            ParameterizedTypeImpl setTypeImpl = ParameterizedTypeImpl.make(Set.class, new Type[]{String.class}, null);
            returnTypeName = TypeName.get(setTypeImpl);
        } else {
            returnTypeName = TypeName.get(clazz);
        }

        return MethodSpec.methodBuilder(getMethodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement(statement, key)
                .returns(returnTypeName)
                .build();
    }
}