package jagm.classicpipes.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface SelfHandler extends CustomPacketPayload {

    void handle(Player player);

}
