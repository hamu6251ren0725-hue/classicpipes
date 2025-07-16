package jagm.classicpipes.util;

import io.netty.buffer.Unpooled;
import jagm.classicpipes.inventory.menu.FilterMenu;
import jagm.classicpipes.network.MatchComponentsPayload;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class NetworkHandler {

    public static void handleMatchComponents(Player player, MatchComponentsPayload payload) {
        if (player.containerMenu instanceof FilterMenu menu) {
            menu.getFilter().setMatchComponents(payload.matchComponents());
        }
    }

    public static FriendlyByteBuf writeCustomData(Consumer<RegistryFriendlyByteBuf> dataWriter, RegistryAccess registryAccess) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            dataWriter.accept(buf);
            buf.readerIndex(0);
        } finally {
            buf.release();
        }
        return buf;
    }

}
