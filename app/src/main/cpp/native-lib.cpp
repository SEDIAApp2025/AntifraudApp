#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_scamdetectorapp_data_remote_RetrofitClient_getApiKey(
        JNIEnv* env,
        jobject /* this */) {
    // API_KEY is passed from build.gradle.kts via -DAPI_KEY="..."
    // If not defined, return empty string
#ifndef API_KEY
#define API_KEY ""
#endif
    return env->NewStringUTF(API_KEY);
}
