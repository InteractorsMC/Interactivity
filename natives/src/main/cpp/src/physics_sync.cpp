/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#include "org_clwn_interactivity_natives_PhysicsSyncNative.h"

#include <Jolt/Jolt.h>
#include <Jolt/Physics/PhysicsSystem.h>
#include <Jolt/Physics/Body/Body.h>
#include <Jolt/Physics/Body/BodyID.h>
#include <Jolt/Physics/Body/BodyLockInterface.h>

using namespace JPH;

namespace {
    constexpr jint INVALID_BODY_ID = static_cast<jint>(0xFFFFFFFFu);

    template<typename T>
    inline T* address(JNIEnv* env, jobject buffer) {
        return static_cast<T*>(env->GetDirectBufferAddress(buffer));
    }
}

/*
 * Reads every buffer address once, then walks the slots without any further JNI calls.
 * The no-lock body interface is safe here because bodies are only added/removed/updated on the
 * calling (physics) thread, and the call happens after PhysicsSystem::Update has returned.
 */
JNIEXPORT jint JNICALL Java_org_clwn_interactivity_natives_PhysicsSyncNative_syncRigidBodies
  (JNIEnv* env, jclass, jlong physicsSystemVa, jint slotCount, jobject joltBodyIdBuf,
   jobject posXBuf, jobject posYBuf, jobject posZBuf,
   jobject rotXBuf, jobject rotYBuf, jobject rotZBuf, jobject rotWBuf,
   jobject velXBuf, jobject velYBuf, jobject velZBuf,
   jobject angVelXBuf, jobject angVelYBuf, jobject angVelZBuf,
   jobject aabbMinXBuf, jobject aabbMinYBuf, jobject aabbMinZBuf,
   jobject aabbMaxXBuf, jobject aabbMaxYBuf, jobject aabbMaxZBuf,
   jobject isActiveBuf, jobject isDirtyBuf, jobject dirtyIndicesBuf, jint dirtyCount) {

    auto* physicsSystem = reinterpret_cast<PhysicsSystem*>(physicsSystemVa);
    if (physicsSystem == nullptr) {
        return dirtyCount;
    }

    const BodyLockInterface& lockInterface = physicsSystem->GetBodyLockInterfaceNoLock();

    const jint* joltBodyId = address<jint>(env, joltBodyIdBuf);
    jdouble* posX = address<jdouble>(env, posXBuf);
    jdouble* posY = address<jdouble>(env, posYBuf);
    jdouble* posZ = address<jdouble>(env, posZBuf);
    jfloat* rotX = address<jfloat>(env, rotXBuf);
    jfloat* rotY = address<jfloat>(env, rotYBuf);
    jfloat* rotZ = address<jfloat>(env, rotZBuf);
    jfloat* rotW = address<jfloat>(env, rotWBuf);
    jfloat* velX = address<jfloat>(env, velXBuf);
    jfloat* velY = address<jfloat>(env, velYBuf);
    jfloat* velZ = address<jfloat>(env, velZBuf);
    jfloat* angVelX = address<jfloat>(env, angVelXBuf);
    jfloat* angVelY = address<jfloat>(env, angVelYBuf);
    jfloat* angVelZ = address<jfloat>(env, angVelZBuf);
    jfloat* aabbMinX = address<jfloat>(env, aabbMinXBuf);
    jfloat* aabbMinY = address<jfloat>(env, aabbMinYBuf);
    jfloat* aabbMinZ = address<jfloat>(env, aabbMinZBuf);
    jfloat* aabbMaxX = address<jfloat>(env, aabbMaxXBuf);
    jfloat* aabbMaxY = address<jfloat>(env, aabbMaxYBuf);
    jfloat* aabbMaxZ = address<jfloat>(env, aabbMaxZBuf);
    jbyte* isActive = address<jbyte>(env, isActiveBuf);
    jbyte* isDirty = address<jbyte>(env, isDirtyBuf);
    jint* dirtyIndices = address<jint>(env, dirtyIndicesBuf);

    for (jint i = 0; i < slotCount; ++i) {
        if (joltBodyId[i] == INVALID_BODY_ID) {
            continue;
        }

        const Body* body = lockInterface.TryGetBody(BodyID(static_cast<uint32>(joltBodyId[i])));
        if (body == nullptr || !body->IsInBroadPhase()) {
            continue;
        }

        const bool joltActive = body->IsActive();
        const bool wasActive = isActive[i] != 0;
        const bool alreadyDirty = isDirty[i] != 0;

        if (!joltActive && !wasActive && !alreadyDirty) {
            continue;
        }

        const RVec3 position = body->GetPosition();
        posX[i] = position.GetX();
        posY[i] = position.GetY();
        posZ[i] = position.GetZ();

        const Quat rotation = body->GetRotation();
        rotX[i] = rotation.GetX();
        rotY[i] = rotation.GetY();
        rotZ[i] = rotation.GetZ();
        rotW[i] = rotation.GetW();

        const Vec3 linearVelocity = body->GetLinearVelocity();
        velX[i] = linearVelocity.GetX();
        velY[i] = linearVelocity.GetY();
        velZ[i] = linearVelocity.GetZ();

        const Vec3 angularVelocity = body->GetAngularVelocity();
        angVelX[i] = angularVelocity.GetX();
        angVelY[i] = angularVelocity.GetY();
        angVelZ[i] = angularVelocity.GetZ();

        const AABox& bounds = body->GetWorldSpaceBounds();
        aabbMinX[i] = bounds.mMin.GetX();
        aabbMinY[i] = bounds.mMin.GetY();
        aabbMinZ[i] = bounds.mMin.GetZ();
        aabbMaxX[i] = bounds.mMax.GetX();
        aabbMaxY[i] = bounds.mMax.GetY();
        aabbMaxZ[i] = bounds.mMax.GetZ();

        isActive[i] = joltActive ? 1 : 0;

        // Listed at most once per tick: the flag doubles as the membership test.
        if (!alreadyDirty) {
            isDirty[i] = 1;
            dirtyIndices[dirtyCount++] = i;
        }
    }

    return dirtyCount;
}
