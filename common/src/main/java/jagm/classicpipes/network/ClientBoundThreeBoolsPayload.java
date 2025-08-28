package jagm.classicpipes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientBoundThreeBoolsPayload(boolean first, boolean second, boolean third) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundThreeBoolsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientBoundThreeBoolsPayload::first,
            ByteBufCodecs.BOOL,
            ClientBoundThreeBoolsPayload::second,
            ByteBufCodecs.BOOL,
            ClientBoundThreeBoolsPayload::third,
            ClientBoundThreeBoolsPayload::new
    );

}
