package jagm.classicpipes.util;

import jagm.classicpipes.inventory.menu.FilterMenu;
import jagm.classicpipes.network.MatchComponentsPayload;
import net.minecraft.world.entity.player.Player;

public class NetworkHandler {

    public static void handleMatchComponents(Player player, MatchComponentsPayload payload) {
        if (player.containerMenu instanceof FilterMenu menu) {
            menu.getFilter().setMatchComponents(payload.matchComponents());
        }
    }

}
