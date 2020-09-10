# MMKVStorage #
[MMKV](https://github.com/Tencent/MMKV)扩展库，通过注解自动生成代码

* 自动生成put方法

* 自动生成get方法

* 自动生成key、defaultValue

## 1.使用 ##

使用前提：集成[MMKV](https://github.com/Tencent/MMKV) ，在Application初始化MMKV

### 1).集成moduler ###
	
	implementation project(':mmkv-annotation')
	annotationProcessor project(':mmkv-compiler')


### 2).创建属性，添加@MMKVData注解 ###
建议新建类，用于集中放置数据
	
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
	}

### 3).调用 ###
	
	//get
	int intData = MMKVStorage.getMmkvInt();
	
	//put
	MMKVStorage.putMmkvInt(123);


## 2.说明 ##
例：

	@MMKVData(key = "key_test_int_v4", commit = true)
	private static final int mmkvIntV4 = 3;

自动生成结果：
	
	  public static boolean putMmkvIntV4(int value) {
	    return mmkv.putInt("key_test_int_v4", value).commit();
	  }
	
	  public static int getMmkvIntV4() {
	    return mmkv.getInt("key_test_int_v4", 3);
	  }

| 代码 | 是否必须 | 说明 |
|:--:|:--:|:--:
| @MMKVData | 必须 | 定义该变量需要自动生成方法 |
| key | 非必须 | 不设置时，mmkv的key为key+属性名小写。如：key_mmkv_int_v4 |
| commit | 非必须 | 不设置时，默认提交方式为apply；设置时，提交方式为commit，且put方法返回值为boolean |
| private static | 非必须 | 建议使用 |
| final | 非必须 | 在设置默认值时必须 |
| int（变量类型）| 必须 | 且只能是为String，int，float，long，boolean，byte[]，Set<String\>中其中一种 |
| mmkvIntV4（变量名）| 必须 | 将变量名自动生成get和put的方法名（首字母大写）以及默认的key值（小写+下划线）|
| 3（变量值）| 非必须 | get方法的defaultValue（默认值）|

## 3.注意 ##
1.设置默认值时，属性必须添加final

2.byte[]和Set<String\>不支持设置默认值
