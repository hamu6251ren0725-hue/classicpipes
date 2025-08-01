package jagm.classicpipes.blockentity;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.menu.RequestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class RequestPipeEntity extends NetworkedPipeEntity implements MenuProvider {

    public RequestPipeEntity(BlockPos pos, BlockState state) {
        super(ClassicPipes.REQUEST_PIPE_ENTITY, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + ClassicPipes.MOD_ID + ".request");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        if (this.hasLogisticalNetwork()) {
            return new RequestMenu(id, inventory, this.getLogisticalNetwork().requestItemList(this.getBlockPos()));
        }
        return null;
    }

}
