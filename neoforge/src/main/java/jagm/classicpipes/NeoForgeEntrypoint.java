package jagm.classicpipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ClassicPipes.MOD_ID)
public class NeoForgeEntrypoint {

    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ClassicPipes.MOD_ID);
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ClassicPipes.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ClassicPipes.MOD_ID);

    public NeoForgeEntrypoint(IEventBus eventBus){
        ClassicPipes.ITEMS.forEach(ITEMS::register);
        ClassicPipes.BLOCKS.forEach(BLOCKS::register);
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

}
