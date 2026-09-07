package org.clwn.interactivity.core.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HelloClientPacket(byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HelloClientPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("interactivity", "my_data"));

    // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
    // 'name' will be encoded and decoded as a string
    // 'age' will be encoded and decoded as an integer
    // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
    public static final StreamCodec<FriendlyByteBuf, HelloClientPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY,
            HelloClientPacket::data,
            HelloClientPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}