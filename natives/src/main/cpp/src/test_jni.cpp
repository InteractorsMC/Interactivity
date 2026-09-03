#include <iostream>
#include <string>
#include "org_clwn_interactivity_natives_TestJni.h"

/*
 * Implementation of TestJni native methods.
 */

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_TestJni_printHello
  (JNIEnv *env, jclass clazz) {
    std::cout << "Hello World from JNI!" << std::endl;
    std::cout.flush();
}
