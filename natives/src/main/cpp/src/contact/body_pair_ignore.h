/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#pragma once

#include "contact_listener.h"

#include <Jolt/Physics/PhysicsSystem.h>

#include <unordered_set>

namespace interactivity {

    /**
     * Rejects all contacts between ignored body pairs.
     */
    class BodyPairIgnore final : public InteractivityContactListener {
    public:
        explicit BodyPairIgnore(JPH::PhysicsSystem* physicsSystem) : physicsSystem(physicsSystem) {}

        void add(JPH::BodyID body1, JPH::BodyID body2);
        void remove(JPH::BodyID body1, JPH::BodyID body2);

        /// Stops ignoring every pair with this body
        void removeBody(JPH::BodyID body);

        ContactResult OnContactValidate(const JPH::Body& body1, const JPH::Body& body2, JPH::RVec3Arg baseOffset, const JPH::CollideShapeResult& collisionResult) override;

    private:
        /// A pair that is already touching keeps its cached contact and is not validated again unless the cache is invalidated
        void invalidate(JPH::BodyID body1, JPH::BodyID body2);

        JPH::PhysicsSystem* physicsSystem;
        std::unordered_set<JPH::uint64> pairs;
    };
}
