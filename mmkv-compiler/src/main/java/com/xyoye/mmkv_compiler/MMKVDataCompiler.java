package com.xyoye.mmkv_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.xyoye.mmkv_annotation.MMKVData;

import java.io.IOException;
import java.lang.reflect.Type;
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

/**
 * Created by xyoye on 2020/9/10.
 */

@AutoService(Processor.class)
public class MMKVDataCompiler extends AbstractProcessor {
    private final static String PACKAGE_NAME = "com.xyoye.mmkv";
    private final static String JAVA_FILE_NAME = "MMKVStorage";

    private final static Class<?>[] supportClazz = new Class[]{
            int.class,
            float.class,
            long.class,
            boolean.class
    };

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
        types.add(MMKVData.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MMKVData.class);
        if (elements == null || elements.isEmpty())
            return false;

        //获取StorageMMKV类建造器
        TypeSpec.Builder clazzBuilder = getClassBuilder();

        //根据注解生成方法
        for (Element element : elements) {
            //检查是否支持自动生成代码，并且获取变量类型
            Class<?> clazz = checkSupportGenerate(element);

            MMKVData mmkvData = element.getAnnotation(MMKVData.class);
            //获取提交方式
            boolean isCommit = mmkvData.commit();
            //获取key
            String key = MMKVElementUtils.getMMKVKey(element, mmkvData.key());
            //获取默认值
            String defaultValue = MMKVElementUtils.getDefaultValueString(element, clazz);

            //生成put方法
            MethodSpec putMethodSpec = generatePutMethod(element, clazz, key, isCommit);
            clazzBuilder.addMethod(putMethodSpec);

            //生成get方法
            MethodSpec getMethodSpec = generateGetMethod(element, clazz, key, defaultValue);
            clazzBuilder.addMethod(getMethodSpec);
        }

        //写入文件
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, clazzBuilder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取StorageMMKV类的建造器
     */
    private TypeSpec.Builder getClassBuilder() {
        //引入类
        ClassName mmkvBundle = ClassName.get("com.tencent.mmkv", "MMKV");

        //类建造器
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(JAVA_FILE_NAME)
                .addModifiers(Modifier.PUBLIC);

        //私有属性
        FieldSpec mmkvField = FieldSpec.builder(mmkvBundle, "mmkv", Modifier.PRIVATE, Modifier.STATIC)
                .initializer("$T.defaultMMKV()", mmkvBundle)
                .build();
        classBuilder.addField(mmkvField);

        //构造方法
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
        classBuilder.addMethod(constructor);

        return classBuilder;
    }


    /**
     * 检查是否支持自动生成方法
     */
    private Class<?> checkSupportGenerate(Element element) {
        //判断注解是否用于字段
        if (!element.getKind().isField()) {
            throw new RuntimeException("@MMKVData is used for fields only! fields! \n @MMKVData注解只能用于变量！变量！");
        }
        TypeName elementTypeName = TypeName.get(element.asType());

        //支持String类型
        TypeName stringTypeName = TypeName.get(String.class);
        if (elementTypeName.equals(stringTypeName)) {
            return String.class;
        }

        //支持byte数组
        TypeName byteArrayName = TypeName.get(byte[].class);
        if (elementTypeName.equals(byteArrayName)) {
            return byte[].class;
        }

        //支持Set<String>
        ParameterizedTypeImpl setTypeImpl = ParameterizedTypeImpl.make(Set.class, new Type[]{String.class}, null);
        TypeName setTypeName = TypeName.get(setTypeImpl);
        if (elementTypeName.equals(setTypeName)) {
            return Set.class;
        }

        //不支持装箱的基本类型
        if (elementTypeName.isBoxedPrimitive()) {
            throw new RuntimeException("@MMKVData not a unboxed primitive filed! unboxed! : " + elementTypeName +
                    "\n@MMKVData注解的变量类型不是未装箱的类型 : " + elementTypeName);
        }

        //只支持基本类型
        if (!elementTypeName.isPrimitive()) {
            throw new RuntimeException("@MMKVData not a primitive filed! primitive! : " + elementTypeName +
                    "\n@MMKVData注解的变量类型不是基本类型 : " + elementTypeName);
        }

        //支持特定的基本类型
        for (Class<?> clazz : supportClazz) {
            TypeName supportTypeName = TypeName.get(clazz);
            if (supportTypeName.equals(elementTypeName)) {
                return clazz;
            }
        }
        throw new RuntimeException(elementTypeName + " does not support generation" +
                "\n" + elementTypeName + "不支持自动生成方法");
    }

    /**
     * 生成put方法
     */
    private MethodSpec generatePutMethod(Element element, Class<?> clazz, String key, boolean isCommit) {
        //方法名，例：String mmkvData -> putMmkvData
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
    private MethodSpec generateGetMethod(Element element, Class<?> clazz, String key, Object defaultValue) {
        //方法名，例：String mmkvData -> getMmkvData
        String getMethodName = "get" + TextUtils.toUpperCaseInitials(element.toString());

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