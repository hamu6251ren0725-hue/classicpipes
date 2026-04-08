package jagm.classicpipes.blockentity;

import jagm.classicpipes.util.FluidInPipe;
import jagm.classicpipes.util.Tuple;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class NeoForgeFluidPipeWrapper extends SnapshotJournal<Tuple<Fluid, FluidInPipe>> implements ResourceHandler<FluidResource> {

    private final FluidPipeEntity pipe;
    private final Direction side;

    private Tuple<Fluid, FluidInPipe> fluidPacketToInsert;

    public NeoForgeFluidPipeWrapper(FluidPipeEntity pipe, Direction side) {
        this.pipe = pipe;
        this.side = side;
        this.fluidPacketToInsert = new Tuple<>(Fluids.EMPTY, null);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int tank) {
        return FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int tank) {
        return 0;
    }

    @Override
    public long getCapacityAsLong(int tank, FluidResource fluidResource) {
        return 1000;
    }

    @Override
    public boolean isValid(int tank, FluidResource fluidResource) {
        return this.side != null && this.pipe.isPipeConnected(this.pipe.getBlockState(), this.side);
    }

    @Override
    public int insert(int tank, FluidResource fluidResource, int maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0 || !this.pipe.emptyOrMatches(fluidResource.getFluid()) || !this.isValid(tank, fluidResource)) {
            return 0;
        } else {
            int amount = Math.min(this.pipe.remainingCapacity(), maxAmount);
            this.updateSnapshots(transaction);
            this.fluidPacketToInsert = new Tuple<>(fluidResource.getFluid(), new FluidInPipe(amount, this.pipe.getTargetSpeed(), (short) 0, this.side, this.side, (short) 0));
            return amount;
        }
    }

    @Override
    public int extract(int tank, FluidResource fluidResource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    protected Tuple<Fluid, FluidInPipe> createSnapshot() {
        return this.fluidPacketToInsert;
    }

    @Override
    protected void revertToSnapshot(Tuple<Fluid, FluidInPipe> fluidPacketToInsert) {
        this.fluidPacketToInsert = fluidPacketToInsert;
    }

    @Override
    protected void onRootCommit(Tuple<Fluid, FluidInPipe> originalState) {
        if (!this.fluidPacketToInsert.a().isSame(Fluids.EMPTY) && this.fluidPacketToInsert.b() != null && this.pipe.getLevel() instanceof ServerLevel serverLevel) {
            this.pipe.setFluid(fluidPacketToInsert.a());
            this.pipe.insertFluidPacket(serverLevel, fluidPacketToInsert.b());
            serverLevel.sendBlockUpdated(this.pipe.getBlockPos(), this.pipe.getBlockState(), this.pipe.getBlockState(), 2);
        }
    }

}
