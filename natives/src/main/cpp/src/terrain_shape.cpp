/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#include "org_clwn_interactivity_natives_TerrainNative.h"

#include <Jolt/Jolt.h>
#include <Jolt/Physics/PhysicsSystem.h>
#include <Jolt/Physics/Body/Body.h>
#include <Jolt/Physics/Body/BodyCreationSettings.h>
#include <Jolt/Physics/Body/BodyInterface.h>
#include <Jolt/Physics/Body/BodyLockInterface.h>
#include <Jolt/Physics/Collision/Shape/BoxShape.h>
#include <Jolt/Physics/Collision/Shape/StaticCompoundShape.h>

#include <algorithm>

using namespace JPH;

namespace {
    constexpr jint INVALID_BODY_ID = static_cast<jint>(BodyID::cInvalidBodyID);

    /// Small radius, so seams between neighbouring boxes stay nearly flat; boxes thinner than twice this use half their size
    constexpr float TERRAIN_CONVEX_RADIUS = 0.02f;

    /// Bodies this close to a changed terrain body are woken up
    constexpr float WAKE_MARGIN = 1.0f;

    template<typename T>
    inline T* address(JNIEnv* env, jobject buffer) {
        return static_cast<T*>(env->GetDirectBufferAddress(buffer));
    }

    /// Builds a compound shape out of boxes [boxStart, boxStart + boxCount), returns null on failure
    Ref<Shape> buildShape(const jfloat* boxes, const jint* tags, jint boxStart, jint boxCount) {
        StaticCompoundShapeSettings settings;
        settings.mSubShapes.reserve(static_cast<size_t>(boxCount));

        for (jint i = boxStart; i < boxStart + boxCount; ++i) {
            const jfloat* box = boxes + static_cast<size_t>(i) * 6;
            Vec3 min(box[0], box[1], box[2]);
            Vec3 max(box[3], box[4], box[5]);
            Vec3 halfExtent = 0.5f * (max - min);

            float convexRadius = std::min(TERRAIN_CONVEX_RADIUS, 0.5f * halfExtent.ReduceMin());
            settings.AddShape(0.5f * (min + max), Quat::sIdentity(), new BoxShape(halfExtent, convexRadius), static_cast<uint32>(tags[i]));
        }

        ShapeSettings::ShapeResult result = settings.Create();
        if (result.HasError()) {
            Trace("Interactivity: failed to build terrain shape: %s", result.GetError().c_str());
            return nullptr;
        }
        return result.Get();
    }

    void wake(BodyInterface& bodyInterface, AABox bounds) {
        bounds.ExpandBy(Vec3::sReplicate(WAKE_MARGIN));

        // The default filters accept every layer
        BroadPhaseLayerFilter broadPhaseFilter;
        ObjectLayerFilter objectFilter;
        bodyInterface.ActivateBodiesInAABox(bounds, broadPhaseFilter, objectFilter);
    }

    AABox boundsOf(PhysicsSystem* physicsSystem, BodyID id) {
        const Body* body = physicsSystem->GetBodyLockInterfaceNoLock().TryGetBody(id);
        return body != nullptr ? body->GetWorldSpaceBounds() : AABox();
    }
}

/*
 * The no-lock interfaces are used because terrain bodies are only changed on the calling (physics) thread,
 * outside of PhysicsSystem::Update.
 */
JNIEXPORT jint JNICALL Java_org_clwn_interactivity_natives_TerrainNative_createTerrainBodies
  (JNIEnv* env, jclass, jlong physicsSystemVa, jint objectLayer, jint bodyCount,
   jobject originsBuf, jobject boxStartsBuf, jobject boxCountsBuf, jobject boxesBuf, jobject tagsBuf, jobject outBodyIdsBuf) {

    auto* physicsSystem = reinterpret_cast<PhysicsSystem*>(physicsSystemVa);
    jint* outBodyIds = address<jint>(env, outBodyIdsBuf);
    if (physicsSystem == nullptr || bodyCount <= 0) {
        return 0;
    }

    const jdouble* origins = address<jdouble>(env, originsBuf);
    const jint* boxStarts = address<jint>(env, boxStartsBuf);
    const jint* boxCounts = address<jint>(env, boxCountsBuf);
    const jfloat* boxes = address<jfloat>(env, boxesBuf);
    const jint* tags = address<jint>(env, tagsBuf);

    BodyInterface& bodyInterface = physicsSystem->GetBodyInterfaceNoLock();

    // AddBodiesPrepare may reorder its array, so it gets a copy and outBodyIds keeps the caller's order
    Array<BodyID> added;
    added.reserve(static_cast<size_t>(bodyCount));

    for (jint i = 0; i < bodyCount; ++i) {
        outBodyIds[i] = INVALID_BODY_ID;

        Ref<Shape> shape = buildShape(boxes, tags, boxStarts[i], boxCounts[i]);
        if (shape == nullptr) {
            continue;
        }

        RVec3 origin(origins[i * 3], origins[i * 3 + 1], origins[i * 3 + 2]);
        BodyCreationSettings settings(shape, origin, Quat::sIdentity(), EMotionType::Static, static_cast<ObjectLayer>(objectLayer));

        Body* body = bodyInterface.CreateBody(settings);
        if (body == nullptr) {
            Trace("Interactivity: out of bodies while creating terrain");
            break;
        }

        outBodyIds[i] = static_cast<jint>(body->GetID().GetIndexAndSequenceNumber());
        added.push_back(body->GetID());
    }

    if (added.empty()) {
        return 0;
    }

    int addedCount = static_cast<int>(added.size());
    BodyInterface::AddState state = bodyInterface.AddBodiesPrepare(added.data(), addedCount);
    bodyInterface.AddBodiesFinalize(added.data(), addedCount, state, EActivation::DontActivate);

    for (const BodyID& id : added) {
        wake(bodyInterface, boundsOf(physicsSystem, id));
    }

    return addedCount;
}

JNIEXPORT jboolean JNICALL Java_org_clwn_interactivity_natives_TerrainNative_setTerrainShape
  (JNIEnv* env, jclass, jlong physicsSystemVa, jint bodyId, jobject boxesBuf, jobject tagsBuf, jint boxStart, jint boxCount) {

    auto* physicsSystem = reinterpret_cast<PhysicsSystem*>(physicsSystemVa);
    if (physicsSystem == nullptr) {
        return JNI_FALSE;
    }

    Ref<Shape> shape = buildShape(address<jfloat>(env, boxesBuf), address<jint>(env, tagsBuf), boxStart, boxCount);
    if (shape == nullptr) {
        return JNI_FALSE;
    }

    BodyID id(static_cast<uint32>(bodyId));
    BodyInterface& bodyInterface = physicsSystem->GetBodyInterfaceNoLock();

    // Both the old and the new extent are woken, so bodies fall into removed space and get pushed out of added space
    AABox bounds = boundsOf(physicsSystem, id);
    bodyInterface.SetShape(id, shape, false, EActivation::DontActivate);
    bounds.Encapsulate(boundsOf(physicsSystem, id));

    wake(bodyInterface, bounds);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_org_clwn_interactivity_natives_TerrainNative_removeTerrainBodies
  (JNIEnv* env, jclass, jlong physicsSystemVa, jobject bodyIdsBuf, jint count, jboolean wakeBodies) {

    auto* physicsSystem = reinterpret_cast<PhysicsSystem*>(physicsSystemVa);
    if (physicsSystem == nullptr || count <= 0) {
        return;
    }

    const jint* bodyIds = address<jint>(env, bodyIdsBuf);
    BodyInterface& bodyInterface = physicsSystem->GetBodyInterfaceNoLock();

    Array<BodyID> ids;
    Array<AABox> bounds;
    ids.reserve(static_cast<size_t>(count));

    for (jint i = 0; i < count; ++i) {
        BodyID id(static_cast<uint32>(bodyIds[i]));
        if (bodyIds[i] == INVALID_BODY_ID || physicsSystem->GetBodyLockInterfaceNoLock().TryGetBody(id) == nullptr) {
            continue;
        }

        ids.push_back(id);
        if (wakeBodies) {
            bounds.push_back(boundsOf(physicsSystem, id));
        }
    }

    if (ids.empty()) {
        return;
    }

    // RemoveBodies may reorder the array, DestroyBodies doesn't care about the order
    bodyInterface.RemoveBodies(ids.data(), static_cast<int>(ids.size()));
    bodyInterface.DestroyBodies(ids.data(), static_cast<int>(ids.size()));

    for (const AABox& box : bounds) {
        wake(bodyInterface, box);
    }
}
