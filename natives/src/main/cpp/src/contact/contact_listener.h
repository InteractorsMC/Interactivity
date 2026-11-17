/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#pragma once

#include <Jolt/Jolt.h>
#include <Jolt/Physics/Body/Body.h>
#include <Jolt/Physics/Collision/ContactListener.h>

namespace interactivity {

    /// Low 16 bits of Body::GetUserData hold the body type id, the other bits are reserved
    constexpr JPH::uint64 BODY_TYPE_MASK = 0xFFFF;

    /// Body without a registered type
    constexpr JPH::uint32 BODY_TYPE_NONE = 0;

    /// Terrain bodies (see terrain_shape.cpp)
    constexpr JPH::uint32 BODY_TYPE_TERRAIN = 0xFFFF;

    inline JPH::uint32 bodyTypeOf(const JPH::Body& body) {
        return static_cast<JPH::uint32>(body.GetUserData() & BODY_TYPE_MASK);
    }

    /// Result of InteractivityContactListener::OnContactValidate
    enum class ContactResult {
        /// No opinion, the next listener decides
        Nothing,
        /// Accept this contact (JPH::ValidateResult::AcceptContact)
        Approve,
        /// Reject this contact (JPH::ValidateResult::RejectContact)
        Reject,
        /// Accept this and further contacts of the body pair in this step, later listeners won't see them
        AlwaysApprove,
        /// Reject this and further contacts of the body pair in this step
        AlwaysReject
    };

    /**
     * A contact listener attached to ContactListenerManager.
     * <p>
     * The same rules as for JPH::ContactListener apply: callbacks come from many threads at the same time,
     * bodies can only be read through the no-lock interfaces, and OnContactRemoved may not access the bodies at all.
     */
    class InteractivityContactListener {
    public:
        virtual ~InteractivityContactListener() = default;

        virtual ContactResult OnContactValidate(
                [[maybe_unused]] const JPH::Body& body1,
                [[maybe_unused]] const JPH::Body& body2,
                [[maybe_unused]] JPH::RVec3Arg baseOffset,
                [[maybe_unused]] const JPH::CollideShapeResult& collisionResult) {
            return ContactResult::Nothing;
        }

        virtual void OnContactAdded(
                [[maybe_unused]] const JPH::Body& body1,
                [[maybe_unused]] const JPH::Body& body2,
                [[maybe_unused]] const JPH::ContactManifold& manifold,
                [[maybe_unused]] JPH::ContactSettings& settings) {}

        virtual void OnContactPersisted(
                [[maybe_unused]] const JPH::Body& body1,
                [[maybe_unused]] const JPH::Body& body2,
                [[maybe_unused]] const JPH::ContactManifold& manifold,
                [[maybe_unused]] JPH::ContactSettings& settings) {}

        virtual void OnContactRemoved([[maybe_unused]] const JPH::SubShapeIDPair& subShapePair) {}
    };
}
