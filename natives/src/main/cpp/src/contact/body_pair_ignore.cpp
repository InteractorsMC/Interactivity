/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#include "body_pair_ignore.h"

#include <Jolt/Physics/Body/BodyInterface.h>
#include <Jolt/Physics/Body/BodyLockInterface.h>

#include <algorithm>

using namespace JPH;

namespace interactivity {

    namespace {
        uint64 key(BodyID body1, BodyID body2) {
            uint32 a = body1.GetIndexAndSequenceNumber();
            uint32 b = body2.GetIndexAndSequenceNumber();
            return (static_cast<uint64>(std::min(a, b)) << 32) | std::max(a, b);
        }
    }

    void BodyPairIgnore::invalidate(BodyID body1, BodyID body2) {
        for (BodyID body : {body1, body2}) {
            if (physicsSystem->GetBodyLockInterfaceNoLock().TryGetBody(body) != nullptr) {
                physicsSystem->GetBodyInterfaceNoLock().InvalidateContactCache(body);
            }
        }
    }

    void BodyPairIgnore::add(BodyID body1, BodyID body2) {
        if (body1 != body2 && pairs.insert(key(body1, body2)).second) {
            invalidate(body1, body2);
        }
    }

    void BodyPairIgnore::remove(BodyID body1, BodyID body2) {
        if (pairs.erase(key(body1, body2)) > 0) {
            invalidate(body1, body2);
        }
    }

    void BodyPairIgnore::removeBody(BodyID body) {
        uint32 id = body.GetIndexAndSequenceNumber();
        for (auto it = pairs.begin(); it != pairs.end();) {
            if (static_cast<uint32>(*it >> 32) == id || static_cast<uint32>(*it) == id) {
                it = pairs.erase(it);
            } else {
                ++it;
            }
        }
    }

    ContactResult BodyPairIgnore::OnContactValidate(const Body& body1, const Body& body2, RVec3Arg, const CollideShapeResult&) {
        if (!pairs.empty() && pairs.count(key(body1.GetID(), body2.GetID())) > 0) {
            return ContactResult::AlwaysReject;
        }
        return ContactResult::Nothing;
    }
}
