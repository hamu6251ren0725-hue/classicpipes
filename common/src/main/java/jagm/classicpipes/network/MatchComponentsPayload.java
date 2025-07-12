package jagm.classicpipes.network;

import io.netty.buffer.ByteBuf;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MatchComponentsPayload(boolean matchComponents) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MatchComponentsPayload> TYPE = new CustomPacketPayload.Type<>(MiscUtil.resourceLocation("match_components"));
    public static final StreamCodec<ByteBuf, MatchComponentsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            MatchComponentsPayload::matchComponents,
            MatchComponentsPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
