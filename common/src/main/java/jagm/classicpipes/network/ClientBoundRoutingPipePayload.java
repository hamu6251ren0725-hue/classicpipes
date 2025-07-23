package jagm.classicpipes.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientBoundRoutingPipePayload(boolean matchComponents, boolean defaultRoute) {

    public static final StreamCodec<ByteBuf, ClientBoundRoutingPipePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientBoundRoutingPipePayload::matchComponents,
            ByteBufCodecs.BOOL,
            ClientBoundRoutingPipePayload::defaultRoute,
            ClientBoundRoutingPipePayload::new
    );

}
