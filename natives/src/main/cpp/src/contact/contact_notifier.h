/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#pragma once

#include "contact_listener.h"

#include <mutex>
#include <vector>

namespace interactivity {

    class ContactListenerManager;

    /// Bits of Notifier::kinds and ContactRecord::kind
    constexpr JPH::uint8 CONTACT_ADDED = 1;
    constexpr JPH::uint8 CONTACT_PERSISTED = 2;
    constexpr JPH::uint8 CONTACT_REMOVED = 4;

    /// Wildcard for the body and type fields of Notifier
    constexpr JPH::uint32 ANY = 0xFFFFFFFF;

    /**
     * Which contacts are reported. Body 1 / type 1 select the body the event is about, body 2 / type 2 the body it touched.
     */
    struct Notifier {
        JPH::uint32 id;
        JPH::uint8 kinds;
        JPH::uint32 body1, type1;
        JPH::uint32 body2, type2;
        /// Only for added and persisted contacts
        float minApproachSpeed;
    };

    /**
     * One reported contact, read by ContactNative.java. Body 1 is the side the notifier matched,
     * the normal points from body 1 to body 2. Removed contacts only have the ids and the kind.
     */
    struct ContactRecord {
        double pointX, pointY, pointZ;      // 0, contact point on body 1
        float normalX, normalY, normalZ;    // 24
        float penetration;                  // 36
        float approachSpeed;                // 40
        JPH::uint32 notifierId;             // 44
        JPH::uint32 body1;                  // 48
        JPH::uint32 body2;                  // 52
        JPH::uint32 subShapeUserData1;      // 56
        JPH::uint32 subShapeUserData2;      // 60
        JPH::uint32 type2;                  // 64
        JPH::uint32 kind;                   // 68
    };

    static_assert(sizeof(ContactRecord) == 72, "ContactRecord layout is mirrored in ContactNative.java");

    /**
     * Collects contacts that match one of its notifiers, they are read after PhysicsSystem::Update.
     */
    class ContactNotifier final : public InteractivityContactListener {
    public:
        explicit ContactNotifier(const ContactListenerManager& manager) : manager(manager) {}

        void add(const Notifier& notifier);

        void remove(JPH::uint32 id);

        /// Removes the notifiers bound to this body
        void removeBody(JPH::BodyID body);

        std::vector<ContactRecord>& getRecords() { return records; }

        void OnContactAdded(const JPH::Body& body1, const JPH::Body& body2, const JPH::ContactManifold& manifold, JPH::ContactSettings& settings) override;
        void OnContactPersisted(const JPH::Body& body1, const JPH::Body& body2, const JPH::ContactManifold& manifold, JPH::ContactSettings& settings) override;
        void OnContactRemoved(const JPH::SubShapeIDPair& subShapePair) override;

    private:
        void onContact(JPH::uint8 kind, JPH::BodyID id1, JPH::int64 type1, JPH::BodyID id2, JPH::int64 type2, const ContactRecord& contact);
        void updateKinds();

        const ContactListenerManager& manager;
        std::vector<Notifier> notifiers;

        /// Union of the kinds of all notifiers
        JPH::uint8 kinds = 0;

        /// Callbacks come from many threads at the same time
        std::mutex recordsMutex;
        std::vector<ContactRecord> records;
    };
}
