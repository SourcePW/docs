# jni
## jni搭建
### java 方法
```
package com.example;

public class JniDemo {

    static {
        System.loadLibrary("jnidemo");
    }

    public static native byte[] jniCall(byte[] data);

    public static void main(String[] args) {
        String str = "Data From Java";
        byte[] result = jniCall(str.getBytes());
        System.out.println("Java Result:" + new String(result));
    }
    
}
``` 

### jni实现

jni文件夹下`src/jni_demo.c`
```
#include <jni.h>
/* Header for class com_example_JniDemo */

#ifndef _Included_com_example_JniDemo
#define _Included_com_example_JniDemo
#ifdef __cplusplus
extern "C"
{
#endif

    /*
    * Class:     com_example_JniDemo
    * Method:    jntCall
    * Signature: ([B)[B
    */
    JNIEXPORT jbyteArray JNICALL jni_call(JNIEnv *env, jclass clz, jbyteArray arrayData)
    {
        char* out = "Data From Jni!";
        unsigned int outlen = 14;
        // 获取数组指针和长度
        jbyte* pdata = (*env)->GetByteArrayElements(env, arrayData, NULL);

        if(pdata != NULL) {
            
            (*env)->ReleaseByteArrayElements(env, arrayData, pdata, JNI_ABORT);

            // char to byte[]
            jbyteArray result = (*env)->NewByteArray(env, outlen);
            if(result != NULL){
                (*env)->SetByteArrayRegion(env, result, 0, outlen, out);
                return result;
            }
        }
        return NULL;
    }

    static const JNINativeMethod gMethods[] = {
        {"jniCall", "([B)[B", (jbyteArray)jni_call}};

    static jclass jniClass;

    /** 如果类名变化需要修改类名 **/
    static const char *const className = "com/example/JniDemo";

    JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
    {
        JNIEnv *env = NULL;
        jint result = -1;
        if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_4) != JNI_OK)
        {
            return -1;
        }
        jniClass = (*env)->FindClass(env, className);
        if (jniClass == NULL)
        {
            printf("cannot get class:%s\n", className);
            return -1;
        }
        if ((*env)->RegisterNatives(env, jniClass, gMethods, sizeof(gMethods) / sizeof(gMethods[0])) < 0)
        {
            printf("register native method failed!\n");
            return -1;
        }
        return JNI_VERSION_1_4;
    }

#ifdef __cplusplus
}

#endif
#endif
```

jni文件夹下`Makefile`
```
.PHONY: clean

CFLAGS  += -Iinclude -I/usr/local/jdk8u312-b07/include/ -I/usr/local/jdk8u312-b07/include/linux  -fPIC 
LDFLAGS += -shared 

OBJS = $(patsubst %.c,%.o,$(wildcard src/*.c))

%.o: %.c
	gcc -c $< $(CFLAGS) -o $@

libjnidemo.so: $(OBJS)
	gcc -o $@ $(LDFLAGS) $(OBJS)

clean:
	-rm -f libjnidemo.so $(OBJS)
```  

