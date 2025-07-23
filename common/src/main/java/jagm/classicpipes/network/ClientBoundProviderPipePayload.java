package jagm.classicpipes.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientBoundProviderPipePayload(boolean matchComponents, boolean leaveOne) {

    public static final StreamCodec<ByteBuf, ClientBoundProviderPipePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientBoundProviderPipePayload::matchComponents,
            ByteBufCodecs.BOOL,
            ClientBoundProviderPipePayload::leaveOne,
            ClientBoundProviderPipePayload::new
    );

}
