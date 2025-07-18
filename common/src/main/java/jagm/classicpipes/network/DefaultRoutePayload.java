package jagm.classicpipes.network;

import jagm.classicpipes.inventory.menu.NetheriteBasicPipeMenu;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record DefaultRoutePayload(boolean defaultRoute) implements CustomPacketPayload {

    public static final Type<DefaultRoutePayload> TYPE = new Type<>(MiscUtil.resourceLocation("default_route"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultRoutePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            DefaultRoutePayload::defaultRoute,
            DefaultRoutePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(Player player) {
        if (player != null && player.containerMenu instanceof NetheriteBasicPipeMenu menu) {
            menu.setDefaultRoute(this.defaultRoute());
        }
    }

}
