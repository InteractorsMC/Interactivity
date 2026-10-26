/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.constraint;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EConstraintSubType;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ConstraintType<S extends TwoBodyConstraintSettings, J extends TwoBodyConstraint> {
    private static final Map<EConstraintSubType, ConstraintType<?, ?>> BY_SUB_TYPE = new EnumMap<>(EConstraintSubType.class);

    public static final ConstraintType<FixedConstraintSettings, FixedConstraint> FIXED = new ConstraintType<>(FixedConstraintSettings::new, EConstraintSubType.Fixed);
    public static final ConstraintType<PointConstraintSettings, PointConstraint> POINT = new ConstraintType<>(PointConstraintSettings::new, EConstraintSubType.Point);
    public static final ConstraintType<HingeConstraintSettings, HingeConstraint> HINGE = new ConstraintType<>(HingeConstraintSettings::new, EConstraintSubType.Hinge);
    public static final ConstraintType<SliderConstraintSettings, SliderConstraint> SLIDER = new ConstraintType<>(SliderConstraintSettings::new, EConstraintSubType.Slider);
    public static final ConstraintType<DistanceConstraintSettings, DistanceConstraint> DISTANCE = new ConstraintType<>(DistanceConstraintSettings::new, EConstraintSubType.Distance);
    public static final ConstraintType<ConeConstraintSettings, ConeConstraint> CONE = new ConstraintType<>(ConeConstraintSettings::new, EConstraintSubType.Cone);
    public static final ConstraintType<SwingTwistConstraintSettings, SwingTwistConstraint> SWING_TWIST = new ConstraintType<>(SwingTwistConstraintSettings::new, EConstraintSubType.SwingTwist);
    public static final ConstraintType<SixDofConstraintSettings, SixDofConstraint> SIX_DOF = new ConstraintType<>(SixDofConstraintSettings::new, EConstraintSubType.SixDof);

    private final Supplier<S> settingsFactory;

    @Getter
    private final EConstraintSubType subType;

    private ConstraintType(Supplier<S> settingsFactory, EConstraintSubType subType) {
        this.settingsFactory = settingsFactory;
        this.subType = subType;
        BY_SUB_TYPE.put(subType, this);
    }

    /**
     * Creates a new settings object with Jolt defaults. The caller owns it and has to close it.
     */
    public S createSettings() {
        return settingsFactory.get();
    }

    public static @Nullable ConstraintType<?, ?> bySubType(EConstraintSubType subType) {
        return BY_SUB_TYPE.get(subType);
    }
}
