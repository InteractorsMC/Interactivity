/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#include "org_clwn_interactivity_natives_ContactNative.h"

#include "contact_listener_manager.h"

using namespace JPH;
using namespace interactivity;

namespace {
    inline ContactListenerManager* managerOf(jlong handle) {
        return reinterpret_cast<ContactListenerManager*>(handle);
    }

    inline BodyID bodyId(jint id) {
        return BodyID(static_cast<uint32>(id));
    }
}

JNIEXPORT jlong JNICALL Java_org_clwn_interactivity_natives_ContactNative_create
  (JNIEnv*, jclass, jlong physicsSystemVa) {
    return reinterpret_cast<jlong>(new ContactListenerManager(reinterpret_cast<PhysicsSystem*>(physicsSystemVa)));
}

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_ContactNative_destroy
  (JNIEnv*, jclass, jlong handle) {
    delete managerOf(handle);
}

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_ContactNative_addNotifier
  (JNIEnv*, jclass, jlong handle, jint id, jint kinds, jint body1, jint type1, jint body2, jint type2, jfloat minApproachSpeed) {
    managerOf(handle)->getContactNotifier().add(Notifier{
            static_cast<uint32>(id),
            static_cast<uint8>(kinds),
            static_cast<uint32>(body1),
            static_cast<uint32>(type1),
            static_cast<uint32>(body2),
            static_cast<uint32>(type2),
            minApproachSpeed
    });
}

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_ContactNative_removeNotifier
  (JNIEnv*, jclass, jlong handle, jint id) {
    managerOf(handle)->getContactNotifier().remove(static_cast<uint32>(id));
}

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_ContactNative_ignorePair
  (JNIEnv*, jclass, jlong handle, jint body1, jint body2) {
    managerOf(handle)->getBodyPairIgnore().add(bodyId(body1), bodyId(body2));
}

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_ContactNative_unignorePair
  (JNIEnv*, jclass, jlong handle, jint body1, jint body2) {
    managerOf(handle)->getBodyPairIgnore().remove(bodyId(body1), bodyId(body2));
}

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_ContactNative_removeBody
  (JNIEnv*, jclass, jlong handle, jint body) {
    managerOf(handle)->getBodyPairIgnore().removeBody(bodyId(body));
    managerOf(handle)->getContactNotifier().removeBody(bodyId(body));
}

JNIEXPORT jobject JNICALL Java_org_clwn_interactivity_natives_ContactNative_getEvents
  (JNIEnv* env, jclass, jlong handle) {
    std::vector<ContactRecord>& records = managerOf(handle)->getContactNotifier().getRecords();
    if (records.empty()) {
        return nullptr;
    }
    return env->NewDirectByteBuffer(records.data(), static_cast<jlong>(records.size() * sizeof(ContactRecord)));
}

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_ContactNative_clearEvents
  (JNIEnv*, jclass, jlong handle) {
    managerOf(handle)->getContactNotifier().getRecords().clear();
}
