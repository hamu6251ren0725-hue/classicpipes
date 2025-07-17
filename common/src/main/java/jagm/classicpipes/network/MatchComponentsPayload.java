package jagm.classicpipes.network;

import jagm.classicpipes.inventory.menu.FilterMenu;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record MatchComponentsPayload(boolean matchComponents) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MatchComponentsPayload> TYPE = new CustomPacketPayload.Type<>(MiscUtil.resourceLocation("match_components"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MatchComponentsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            MatchComponentsPayload::matchComponents,
            MatchComponentsPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(Player player) {
        if (player != null && player.containerMenu instanceof FilterMenu menu) {
            menu.getFilter().setMatchComponents(this.matchComponents());
        }
    }

}
