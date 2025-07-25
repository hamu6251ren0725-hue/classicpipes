package jagm.classicpipes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientBoundBoolPayload(boolean value) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundBoolPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientBoundBoolPayload::value,
            ClientBoundBoolPayload::new
    );

}
