package jagm.classicpipes.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientBoundCraftingPipePayload(Direction[] slotDirections, BlockPos pos) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundCraftingPipePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.byteArray(10),
            ClientBoundCraftingPipePayload::getDirectionBytes,
            BlockPos.STREAM_CODEC,
            ClientBoundCraftingPipePayload::pos,
            ClientBoundCraftingPipePayload::makePayload
    );

    private byte[] getDirectionBytes() {
        byte[] directionBytes = new byte[this.slotDirections().length];
        for (int i = 0; i < this.slotDirections().length; i++) {
            directionBytes[i] = (byte) this.slotDirections()[i].get3DDataValue();
        }
        return directionBytes;
    }

    private static ClientBoundCraftingPipePayload makePayload(byte[] directionBytes, BlockPos pos) {
        Direction[] directions = new Direction[directionBytes.length];
        for (int i = 0; i < directionBytes.length; i++) {
            directions[i] = Direction.from3DDataValue(directionBytes[i]);
        }
        return new ClientBoundCraftingPipePayload(directions, pos);
    }

}
