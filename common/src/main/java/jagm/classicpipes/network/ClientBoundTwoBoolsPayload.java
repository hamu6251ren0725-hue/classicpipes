package jagm.classicpipes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientBoundTwoBoolsPayload(boolean first, boolean second) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundTwoBoolsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientBoundTwoBoolsPayload::first,
            ByteBufCodecs.BOOL,
            ClientBoundTwoBoolsPayload::second,
            ClientBoundTwoBoolsPayload::new
    );

}
