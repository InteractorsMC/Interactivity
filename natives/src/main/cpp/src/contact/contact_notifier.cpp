/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
#include "contact_notifier.h"
#include "contact_listener_manager.h"

#include <Jolt/Physics/Collision/Shape/Shape.h>

#include <algorithm>

using namespace JPH;

namespace interactivity {

    namespace {
        bool matches(const Notifier& notifier, uint32 self, int64 selfType, uint32 other, int64 otherType) {
            return (notifier.body1 == ANY || notifier.body1 == self)
                    && (notifier.type1 == ANY || notifier.type1 == selfType)
                    && (notifier.body2 == ANY || notifier.body2 == other)
                    && (notifier.type2 == ANY || notifier.type2 == otherType);
        }

        ContactRecord swapped(ContactRecord record) {
            std::swap(record.body1, record.body2);
            std::swap(record.subShapeUserData1, record.subShapeUserData2);
            record.normalX = -record.normalX;
            record.normalY = -record.normalY;
            record.normalZ = -record.normalZ;
            return record;
        }

        /// Fills the contact data of a record, the point is on body 1 and the normal points to body 2
        ContactRecord contactOf(const Body& body1, const Body& body2, const ContactManifold& manifold) {
            RVec3 point = manifold.GetWorldSpaceContactPointOn1(0);
            Vec3 normal = manifold.mWorldSpaceNormal;

            // Approaching bodies have a negative relative velocity along the normal
            Vec3 relativeVelocity = body2.GetPointVelocity(point) - body1.GetPointVelocity(point);

            ContactRecord record{};
            record.pointX = point.GetX();
            record.pointY = point.GetY();
            record.pointZ = point.GetZ();
            record.normalX = normal.GetX();
            record.normalY = normal.GetY();
            record.normalZ = normal.GetZ();
            record.penetration = manifold.mPenetrationDepth;
            record.approachSpeed = -relativeVelocity.Dot(normal);
            record.subShapeUserData1 = static_cast<uint32>(body1.GetShape()->GetSubShapeUserData(manifold.mSubShapeID1));
            record.subShapeUserData2 = static_cast<uint32>(body2.GetShape()->GetSubShapeUserData(manifold.mSubShapeID2));
            return record;
        }
    }

    void ContactNotifier::updateKinds() {
        kinds = 0;
        for (const Notifier& notifier : notifiers) {
            kinds |= notifier.kinds;
        }
    }

    void ContactNotifier::add(const Notifier& notifier) {
        notifiers.push_back(notifier);
        updateKinds();
    }

    void ContactNotifier::remove(uint32 id) {
        notifiers.erase(std::remove_if(notifiers.begin(), notifiers.end(),
                                       [id](const Notifier& notifier) { return notifier.id == id; }),
                        notifiers.end());
        updateKinds();
    }

    void ContactNotifier::removeBody(BodyID body) {
        uint32 id = body.GetIndexAndSequenceNumber();
        notifiers.erase(std::remove_if(notifiers.begin(), notifiers.end(),
                                       [id](const Notifier& notifier) { return notifier.body1 == id || notifier.body2 == id; }),
                        notifiers.end());
        updateKinds();
    }

    void ContactNotifier::onContact(uint8 kind, BodyID id1, int64 type1, BodyID id2, int64 type2, const ContactRecord& contact) {
        uint32 body1 = id1.GetIndexAndSequenceNumber();
        uint32 body2 = id2.GetIndexAndSequenceNumber();

        for (const Notifier& notifier : notifiers) {
            if ((notifier.kinds & kind) == 0 || (kind != CONTACT_REMOVED && contact.approachSpeed < notifier.minApproachSpeed)) {
                continue;
            }

            ContactRecord record = contact;
            record.notifierId = notifier.id;
            record.kind = kind;
            record.body1 = body1;
            record.body2 = body2;
            record.type2 = static_cast<uint32>(type2);

            // A notifier without a body 1 filter reports the contact once, otherwise every matching side is reported
            bool matched = matches(notifier, body1, type1, body2, type2);
            if (matched) {
                std::lock_guard lock(recordsMutex);
                records.push_back(record);
            }
            if ((!matched || notifier.body1 != ANY || notifier.type1 != ANY) && matches(notifier, body2, type2, body1, type1)) {
                ContactRecord other = swapped(record);
                other.type2 = static_cast<uint32>(type1);
                std::lock_guard lock(recordsMutex);
                records.push_back(other);
            }
        }
    }

    void ContactNotifier::OnContactAdded(const Body& body1, const Body& body2, const ContactManifold& manifold, ContactSettings&) {
        if (kinds & CONTACT_ADDED) {
            onContact(CONTACT_ADDED, body1.GetID(), bodyTypeOf(body1), body2.GetID(), bodyTypeOf(body2), contactOf(body1, body2, manifold));
        }
    }

    void ContactNotifier::OnContactPersisted(const Body& body1, const Body& body2, const ContactManifold& manifold, ContactSettings&) {
        if (kinds & CONTACT_PERSISTED) {
            onContact(CONTACT_PERSISTED, body1.GetID(), bodyTypeOf(body1), body2.GetID(), bodyTypeOf(body2), contactOf(body1, body2, manifold));
        }
    }

    void ContactNotifier::OnContactRemoved(const SubShapeIDPair& subShapePair) {
        if (kinds & CONTACT_REMOVED) {
            BodyID id1 = subShapePair.GetBody1ID();
            BodyID id2 = subShapePair.GetBody2ID();
            onContact(CONTACT_REMOVED, id1, manager.cachedBodyType(id1), id2, manager.cachedBodyType(id2), ContactRecord{});
        }
    }
}
