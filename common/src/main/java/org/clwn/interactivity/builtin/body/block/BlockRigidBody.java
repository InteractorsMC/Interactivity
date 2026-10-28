/*
 * This file is dedicated to the public domain worldwide using the
 * Creative Commons CC0 1.0 Universal waiver.
 *
 * To the extent possible under law, the author(s) have waived all
 * copyright and related or neighboring rights to this file.
 * You can copy, modify, distribute, and perform the work, even for
 * commercial purposes, all without asking permission.
 *
 * For full legal text, see: https://creativecommons.org
 */
package org.clwn.interactivity.builtin.body.block;

import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.BoxShapeSettings;
import com.github.stephengold.joltjni.ShapeSettings;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.ObjectLayer;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.body.RigidBodyFactory;
import org.clwn.interactivity.api.physics.event.BodyEvent;
import org.clwn.interactivity.api.physics.event.SubscribeEvent;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

import java.util.UUID;

public class BlockRigidBody extends Body {
    private static final String BLOCK_STATE_KEY = "block_state";

    @Getter
    private volatile BlockState blockState = Blocks.STONE.defaultBlockState();

    public BlockRigidBody(BodyType<? extends Body> type, PhysicsWorld world, UUID uuid) {
        super(type, world, uuid);
    }

    public static int createJoltBody(Body body, RigidBodyFactory factory) {
        try (
                ShapeSettings shapeSettings = new BoxShapeSettings(new Vec3(0.5f, 0.5f, 0.5f));
                BodyCreationSettings bcs = new BodyCreationSettings()
        ) {
            bcs.setMotionType(EMotionType.Dynamic);
            bcs.setObjectLayer(ObjectLayer.MOVING.getValue());
            return factory.create(shapeSettings, bcs);
        }
    }

    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
        requestDataSync();
    }

    public static void writeData(Body body, FriendlyByteBuf buf) {
        buf.writeNbt(NbtUtils.writeBlockState(((BlockRigidBody) body).blockState));
    }

    public static void readData(Body body, FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag != null) {
            ((BlockRigidBody) body).blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag);
        }
    }

    public static void configureSummon(Body body, CompoundTag data) {
        if (!data.contains(BLOCK_STATE_KEY, Tag.TAG_STRING)) {
            return;
        }

        try {
            ((BlockRigidBody) body).blockState = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), data.getString(BLOCK_STATE_KEY), false).blockState();
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }


    @SubscribeEvent
    public void onBodyEvent(BodyEvent bodyEvent) {
        Interactivity.LOGGER.info("Body event {} called for {}", bodyEvent.getClass().getSimpleName(), bodyEvent.getBody().getClass().getSimpleName());
    }
}
