package jagm.classicpipes.network;

import jagm.classicpipes.util.MiscUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ForgePacketHandler {

    private static final SimpleChannel INSTANCE = ChannelBuilder.named(MiscUtil.resourceLocation("main")).simpleChannel();

    public static void register() {
        INSTANCE.play().serverbound().add(ServerBoundMatchComponentsPayload.class, ServerBoundMatchComponentsPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> payload.handle(context.getSender())));
        INSTANCE.play().serverbound().add(ServerBoundDefaultRoutePayload.class, ServerBoundDefaultRoutePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> payload.handle(context.getSender())));
        INSTANCE.play().serverbound().add(ServerBoundLeaveOnePayload.class, ServerBoundLeaveOnePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> payload.handle(context.getSender())));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        INSTANCE.send(payload, PacketDistributor.SERVER.noArg());
    }

}
