# MMKVStorage #
[MMKV](https://github.com/Tencent/MMKV)扩展库，通过注解自动生成get和set方法

## 1.作用，这个库是为什么产生的 ##
为了更快捷的使用MMKV

## 2.功能，它实现了哪些功能 ##
1. 自动生成get、put方法
2. 支持生成kotlin、java代码

代码示例：
	
	//你需要编写的代码
	@MMKVKotlinClass
	object TestKotlin{
	    @MMKVFiled
	    val kotlinVlaue = 12
	}

	//自动生成的代码
	object TestKotlin_MMKV {
	    private val mmkv: MMKV = MMKV.defaultMMKV()
	
	    fun putKotlinValue(value: Int) {
	        mmkv.putInt("key_kotlin_value", value)
	    }
	
	    fun getKotlinValue(): Int = mmkv.getInt("key_kotlin_value", com.xyoye.mmkvstorage.TestKotlin.kotlinValue)
	}


## 3.使用，如何快速上手 ##

使用前提：集成[MMKV](https://github.com/Tencent/MMKV) ，在Application初始化MMKV

### 1).集成module ###
	
	implementation project(':mmkv-annotation')
	annotationProcessor project(':mmkv-compiler')


### 2).使用注解 ###
建议新建类，用于集中放置数据

#### kotlin ####
	
	//1.添加类注解
	@MMKVKotlinClass
	object TestKotlin {
	
	    //2.添加属性注解
	    @MMKVFiled
	    val kotlinValue = 12
	
	    @MMKVFiled
	    val kotlinValueString: String = "test"
	}

#### java ####
	
	//1.添加类注解
	@MMKVJavaClass
	public class TestJava {

	    //2.添加属性注解
	    @MMKVFiled
	    protected static int javaValue = 1;
	
	    @MMKVFiled
	    protected static int javaValueString = "123";
	}



### 3).Rebuild Project，调用方法 ###
自动生成方法都是公开静态方法，你可以在需要的地方通过生成类的类名调用

类名：@MMKVKotlinClass和@MMKVJavaClass注解的类 + 下划线 + MMKV

如：TestKotlin -> TestKotlin_MMKV, TestJava -> TestJava_MMKV
	
    #kotlin
    //get
    val kotlinInt = TestKotlin_MMKV.getKotlinValue()
    //put  
    TestKotlin_MMKV.putKotlinValue(123)

    #java
    //get
    int javaInt = TestJava_MMKV.getJavaValue();
    //put   
    TestJava_MMKV.putJavaValue(123);


## 4.扩展，高级使用 ##
1) 自定义自动生成类的类名

        //在类注解添加类名
        @MMKVKotlinClass(className = "MMKVStorageKotlin")
        @MMKVJavaClass(className = "MMKVStorageJava")

2) 自定义mmkv实例化方式

        //在类注解设置initMMKV = true
        @MMKVKotlinClass(initMMKV = true)
        @MMKVJavaClass(initMMKV = true)

        //在使用前先初始化mmkv
        #java
        TestJava_MMKV.initMMKV(MMKV实例)
	
        #kotlin
        TestKotlin_MMKV.initMMKV(MMKV实例)

3) 自定义mmkv存储键
	
        //在属性注解设置自定义的key
        @MMKVFiled(key = "key_test_key")

4) 自定义存储方式（默认apply存储）
	
        //在属性注解设置以commit提交
        @MMKVFiled(commit = true)

## 5.说明，我写的代码到底用来干什么 ##

### kotlin ###

	@MMKVKotlinClass
	object TestKotlin{
	    @MMKVFiled
	    val kotlinVlaue: Int = 12
	}


| 代码 | 是否必须 | 说明 |
|:--:|:--:|:--:
| @MMKVKotlinClass | 必须 | 定义该类为需要自动生成方法的类 |
| object class | 非必须 | 在需要设置默认值时为必须 |
| TestKotlin | 必须 | 将根据类名生成默认类名 |
| @MMKVFiled | 必须 | 定义该变量需要自动生成方法 |
| val | 非必须 | 也可使用var |
| kotlinVlaue（变量名）| 必须 | 将根据变量名自动生成get和put的方法名（首字母大写）以及默认的key值（小写+下划线）|
| Int| 非必须 | 当指定值（如:12）时非必须，否则需要 |
| 12（变量值）| 非必须 | get方法的defaultValue（默认值）|

### java ###

	@MMKVJavaClass(initMMKV = true)
	public class TestJava {
	    @MMKVFiled
	    protected static int javaValue = 12;
	}

| 代码 | 是否必须 | 说明 |
|:--:|:--:|:--:
| @MMKVJavaClass | 必须 | 定义该类为需要自动生成方法的类 |
| public class | 必须 | 生成的类会继承于此类，且默认值将引用类中属性 |
| TestJava | 必须 | 将根据类名生成默认类名 |
| @MMKVFiled | 必须 | 定义该变量需要自动生成方法 |
| protected static | 必须 | protected可替换为publicc |
| int| 必须 | 用于指定mmkv存储及读取方式 |
| javaValue（变量名）| 必须 | 将根据变量名自动生成get和put的方法名（首字母大写）以及默认的key值（小写+下划线）|
| 12（变量值）| 非必须 | get方法的defaultValue（默认值）|

## 6.注意一些细节 ##
1. 注解不能使用于定义为final的java类，因为生成的类需要继承于该类
2. @MMKVFiled仅支持mmkv put、get方法所支持的6种类型，即：String，int，float，long，boolean，byte[]，Set<String\>
3. 使用注解定义后，需要rebuild project才能自动生成方法
