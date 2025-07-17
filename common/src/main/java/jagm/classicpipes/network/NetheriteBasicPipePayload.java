package jagm.classicpipes.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NetheriteBasicPipePayload(boolean matchComponents, boolean defaultRoute) {

    public static final StreamCodec<ByteBuf, NetheriteBasicPipePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            NetheriteBasicPipePayload::matchComponents,
            ByteBufCodecs.BOOL,
            NetheriteBasicPipePayload::defaultRoute,
            NetheriteBasicPipePayload::new
    );

}
