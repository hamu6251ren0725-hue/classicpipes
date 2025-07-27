package jagm.classicpipes.network;

import jagm.classicpipes.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ForgePacketHandler {

    private static final SimpleChannel INSTANCE = ChannelBuilder.named(MiscUtil.resourceLocation("main")).simpleChannel();

    public static <T extends SelfHandler> void registerServerPayload(Class<T> clazz, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        INSTANCE.play().serverbound().add(clazz, codec, (payload, context) -> context.enqueueWork(() -> payload.handle(context.getSender())));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        INSTANCE.send(payload, PacketDistributor.SERVER.noArg());
    }

}
