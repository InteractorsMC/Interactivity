/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#pragma once

#include "body_pair_ignore.h"
#include "contact_notifier.h"

#include <Jolt/Physics/PhysicsSystem.h>

#include <atomic>
#include <memory>
#include <vector>

namespace interactivity {

    /**
     * The JPH::ContactListener of a physics system, forwards every callback to its listeners in priority order.
     * <p>
     * OnContactValidate returns the result of the first listener that does not return Nothing, or Approve.
     * Listeners and their configuration may only be changed while PhysicsSystem::Update is not running.
     */
    class ContactListenerManager final : public JPH::ContactListener {
    public:
        explicit ContactListenerManager(JPH::PhysicsSystem* physicsSystem);
        ~ContactListenerManager() override;

        ContactListenerManager(const ContactListenerManager&) = delete;
        ContactListenerManager& operator=(const ContactListenerManager&) = delete;

        /// Adds a listener, lower priority runs first
        void addListener(InteractivityContactListener* listener, int priority);

        BodyPairIgnore& getBodyPairIgnore() { return bodyPairIgnore; }
        ContactNotifier& getContactNotifier() { return contactNotifier; }

        /// Body type from the last added or persisted contact of the body, -1 when unknown.
        /// For OnContactRemoved, where bodies can't be accessed.
        [[nodiscard]] JPH::int64 cachedBodyType(JPH::BodyID id) const;

        JPH::ValidateResult OnContactValidate(const JPH::Body& body1, const JPH::Body& body2, JPH::RVec3Arg baseOffset, const JPH::CollideShapeResult& collisionResult) override;
        void OnContactAdded(const JPH::Body& body1, const JPH::Body& body2, const JPH::ContactManifold& manifold, JPH::ContactSettings& settings) override;
        void OnContactPersisted(const JPH::Body& body1, const JPH::Body& body2, const JPH::ContactManifold& manifold, JPH::ContactSettings& settings) override;
        void OnContactRemoved(const JPH::SubShapeIDPair& subShapePair) override;

    private:
        void cacheBody(const JPH::Body& body);

        JPH::PhysicsSystem* physicsSystem;
        BodyPairIgnore bodyPairIgnore;
        ContactNotifier contactNotifier;
        std::vector<std::pair<int, InteractivityContactListener*>> listeners;

        /// Per body index: (BodyID << 32) | body type
        std::unique_ptr<std::atomic<JPH::uint64>[]> bodyCache;
        JPH::uint32 bodyCacheSize;
    };
}
