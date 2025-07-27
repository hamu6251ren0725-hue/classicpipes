package jagm.classicpipes.network;

import jagm.classicpipes.util.SortingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ClientBoundItemListPayload(List<ItemStack> networkItems, SortingMode sortingMode, BlockPos networkPos) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundItemListPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC),
            ClientBoundItemListPayload::networkItems,
            ByteBufCodecs.BYTE,
            payload -> payload.sortingMode().getValue(),
            BlockPos.STREAM_CODEC,
            ClientBoundItemListPayload::networkPos,
            (networkItems, value, networkPos) -> new ClientBoundItemListPayload(networkItems, SortingMode.fromByte(value), networkPos)
    );

}
