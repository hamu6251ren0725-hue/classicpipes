package jagm.classicpipes.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientBoundNetheritePipePayload(boolean matchComponents, boolean defaultRoute) {

    public static final StreamCodec<ByteBuf, ClientBoundNetheritePipePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientBoundNetheritePipePayload::matchComponents,
            ByteBufCodecs.BOOL,
            ClientBoundNetheritePipePayload::defaultRoute,
            ClientBoundNetheritePipePayload::new
    );

}
