//
// Created by AhmedAli on 12/11/2022.
//

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_ahmdalii_weatherforecast_BaseApplication_getBaseURL(
        JNIEnv* env,
        jobject /* this */) {
    std::string BASE_URL = "https://api.openweathermap.org/data/2.5/";
    return env->NewStringUTF(BASE_URL.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ahmdalii_weatherforecast_BaseApplication_getWeatherAppID(
        JNIEnv* env,
        jobject /* this */) {
    std::string WEATHER_APP_ID = "8d773d346027c79a23566d76849b2716";
    return env->NewStringUTF(WEATHER_APP_ID.c_str());
}