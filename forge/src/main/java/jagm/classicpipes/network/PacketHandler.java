package jagm.classicpipes.network;

import jagm.classicpipes.util.MiscUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {

    private static final SimpleChannel INSTANCE = ChannelBuilder
            .named(MiscUtil.resourceLocation("main"))
            .serverAcceptedVersions((status, version) -> true)
            .clientAcceptedVersions((status, version) -> true)
            .networkProtocolVersion(1)
            .simpleChannel();

    public static void register() {
        INSTANCE.play().serverbound().add(MatchComponentsPayload.class, MatchComponentsPayload.STREAM_CODEC, (payload, context) -> payload.handle(context.getSender()));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        INSTANCE.send(payload, PacketDistributor.SERVER.noArg());
    }

}
