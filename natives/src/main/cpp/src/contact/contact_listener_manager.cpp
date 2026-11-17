/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#include "contact_listener_manager.h"

#include <algorithm>

using namespace JPH;

namespace interactivity {

    namespace {
        constexpr uint64 EMPTY_CACHE_ENTRY = static_cast<uint64>(BodyID::cInvalidBodyID) << 32;

        ValidateResult toJolt(ContactResult result) {
            switch (result) {
                case ContactResult::Reject:
                    return ValidateResult::RejectContact;
                case ContactResult::AlwaysApprove:
                    return ValidateResult::AcceptAllContactsForThisBodyPair;
                case ContactResult::AlwaysReject:
                    return ValidateResult::RejectAllContactsForThisBodyPair;
                default:
                    return ValidateResult::AcceptContact;
            }
        }
    }

    ContactListenerManager::ContactListenerManager(PhysicsSystem* physicsSystem)
            : physicsSystem(physicsSystem),
              bodyPairIgnore(physicsSystem),
              contactNotifier(*this),
              bodyCache(std::make_unique<std::atomic<uint64>[]>(physicsSystem->GetMaxBodies())),
              bodyCacheSize(physicsSystem->GetMaxBodies()) {
        for (uint32 i = 0; i < bodyCacheSize; ++i) {
            bodyCache[i].store(EMPTY_CACHE_ENTRY, std::memory_order_relaxed);
        }

        addListener(&bodyPairIgnore, -1000);
        addListener(&contactNotifier, 1000);
        physicsSystem->SetContactListener(this);
    }

    ContactListenerManager::~ContactListenerManager() {
        physicsSystem->SetContactListener(nullptr);
    }

    void ContactListenerManager::addListener(InteractivityContactListener* listener, int priority) {
        auto position = std::upper_bound(listeners.begin(), listeners.end(), priority,
                                         [](int value, const auto& entry) { return value < entry.first; });
        listeners.insert(position, {priority, listener});
    }

    void ContactListenerManager::cacheBody(const Body& body) {
        BodyID id = body.GetID();
        uint64 entry = (static_cast<uint64>(id.GetIndexAndSequenceNumber()) << 32) | bodyTypeOf(body);

        // Most calls find the entry unchanged, skipping the store keeps the cache line shared between threads
        std::atomic<uint64>& slot = bodyCache[id.GetIndex()];
        if (slot.load(std::memory_order_relaxed) != entry) {
            slot.store(entry, std::memory_order_relaxed);
        }
    }

    int64 ContactListenerManager::cachedBodyType(BodyID id) const {
        if (id.GetIndex() >= bodyCacheSize) {
            return -1;
        }

        uint64 entry = bodyCache[id.GetIndex()].load(std::memory_order_relaxed);
        return static_cast<uint32>(entry >> 32) == id.GetIndexAndSequenceNumber() ? static_cast<int64>(entry & BODY_TYPE_MASK) : -1;
    }

    ValidateResult ContactListenerManager::OnContactValidate(const Body& body1, const Body& body2, RVec3Arg baseOffset, const CollideShapeResult& collisionResult) {
        for (const auto& [priority, listener] : listeners) {
            ContactResult result = listener->OnContactValidate(body1, body2, baseOffset, collisionResult);
            if (result != ContactResult::Nothing) {
                return toJolt(result);
            }
        }
        return ValidateResult::AcceptContact;
    }

    void ContactListenerManager::OnContactAdded(const Body& body1, const Body& body2, const ContactManifold& manifold, ContactSettings& settings) {
        cacheBody(body1);
        cacheBody(body2);
        for (const auto& [priority, listener] : listeners) {
            listener->OnContactAdded(body1, body2, manifold, settings);
        }
    }

    void ContactListenerManager::OnContactPersisted(const Body& body1, const Body& body2, const ContactManifold& manifold, ContactSettings& settings) {
        cacheBody(body1);
        cacheBody(body2);
        for (const auto& [priority, listener] : listeners) {
            listener->OnContactPersisted(body1, body2, manifold, settings);
        }
    }

    void ContactListenerManager::OnContactRemoved(const SubShapeIDPair& subShapePair) {
        for (const auto& [priority, listener] : listeners) {
            listener->OnContactRemoved(subShapePair);
        }
    }
}
