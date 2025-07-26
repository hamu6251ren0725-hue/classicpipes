package jagm.classicpipes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ClientBoundItemListPayload(List<ItemStack> networkItems) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundItemListPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC),
            ClientBoundItemListPayload::networkItems,
            ClientBoundItemListPayload::new
    );

}
