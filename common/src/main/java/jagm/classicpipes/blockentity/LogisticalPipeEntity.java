package jagm.classicpipes.blockentity;

import jagm.classicpipes.block.RoutingPipeBlock;
import jagm.classicpipes.util.ItemInPipe;
import jagm.classicpipes.util.LogisticalNetwork;
import jagm.classicpipes.util.ScheduledRoute;
import jagm.classicpipes.util.SortingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public abstract class LogisticalPipeEntity extends RoundRobinPipeEntity {

    private final Map<ItemStack, ScheduledRoute> routingSchedule;
    private LogisticalNetwork logisticalNetwork;
    private boolean controller;

    public LogisticalPipeEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.routingSchedule = new HashMap<>();
        this.logisticalNetwork = null;
        this.controller = false;
    }

    @Override
    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        super.tickServer(level, pos, state);
        if (!this.routingSchedule.isEmpty()) {
            Iterator<ItemStack> iterator = this.routingSchedule.keySet().iterator();
            while (iterator.hasNext()) {
                ScheduledRoute route = this.routingSchedule.get(iterator.next());
                route.tick();
                if (route.timedOut()) {
                    iterator.remove();
                    this.setChanged();
                }
            }
        }
        if (this.isController()) {
            this.getLogisticalNetwork().tick(level);
        }
    }

    @Override
    protected void initialiseLogistics(ServerLevel level, BlockState state, BlockPos pos) {
        if (this.isController() && this.hasLogisticalNetwork()) {
            this.distributeLogisticalNetwork(level, this.getBlockPos(), new HashSet<>(), this.getLogisticalNetwork());
        }
        super.initialiseLogistics(level, state, pos);
    }

    protected void distributeLogisticalNetwork(ServerLevel level, BlockPos pos, Set<LogisticalPipeEntity> visited, LogisticalNetwork network) {
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
        for (Direction direction : this.logistics.keySet()) {
            BlockPos nextPos = this.logistics.get(direction).getA();
            if (level.getBlockEntity(nextPos) instanceof LogisticalPipeEntity nextPipe) {
                nextPipe.distributeLogisticalNetwork(level, nextPos, visited, network);
            }
        }
        this.setController(pos.equals(network.getPos()));
        this.setLogisticalNetwork(network, level);
    }

    @Override
    public void routeItem(BlockState state, ItemInPipe item) {
        if (!this.hasLogisticalNetwork()) {
            super.routeItem(state, item);
            return;
        }
        if (!this.checkRoutingSchedule(item)) {
            List<RoutingPipeEntity> validTargets = new ArrayList<>();
            for (RoutingPipeEntity routingPipe : this.logisticalNetwork.getRoutingPipes()) {
                if (routingPipe.canRouteItemHere(item.getStack())) {
                    validTargets.add(routingPipe);
                }
            }
            if (validTargets.isEmpty()) {
                validTargets.addAll(this.logisticalNetwork.getDefaultRoutes());
            }
            if (this.getLevel() instanceof ServerLevel serverLevel) {
                if (this instanceof RoutingPipeEntity && validTargets.contains(this) && state.getBlock() instanceof RoutingPipeBlock logisticalBlock) {
                    List<Direction> validDirections = new ArrayList<>();
                    for (Direction direction : Direction.values()) {
                        if (this.isPipeConnected(state, direction) && !logisticalBlock.isLinked(state, direction)) {
                            validDirections.add(direction);
                        }
                    }
                    if (!validDirections.isEmpty()) {
                        item.setEjecting(false);
                        item.setTargetDirection(validDirections.get(serverLevel.getRandom().nextInt(validDirections.size())));
                    } else {
                        item.setEjecting(true);
                        item.setTargetDirection(item.getFromDirection().getOpposite());
                    }
                } else if (!validTargets.isEmpty()) {
                    this.schedulePath(serverLevel, item, validTargets.get(serverLevel.getRandom().nextInt(validTargets.size())));
                    this.checkRoutingSchedule(item);
                } else {
                    super.routeItem(state, item);
                }
            }
        }
    }

    private boolean checkRoutingSchedule(ItemInPipe item) {
        Iterator<ItemStack> iterator = this.routingSchedule.keySet().iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (ItemStack.isSameItemSameComponents(stack, item.getStack()) && stack.getCount() == item.getStack().getCount()) {
                item.setEjecting(false);
                item.setTargetDirection(this.routingSchedule.get(stack).getDirection());
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public void schedule(ItemStack stack, Direction direction) {
        this.routingSchedule.put(stack, new ScheduledRoute(direction));
    }

    public void schedulePath(ServerLevel level, ItemInPipe item, LogisticalPipeEntity target) {
        Map<LogisticalPipeEntity, Tuple<LogisticalPipeEntity, Direction>> cameFrom = new HashMap<>();
        Map<LogisticalPipeEntity, Integer> gScore = new HashMap<>();
        gScore.put(this, 0);
        Map<LogisticalPipeEntity, Integer> fScore = new HashMap<>();
        fScore.put(this, 0);
        PriorityQueue<LogisticalPipeEntity> openSet = new PriorityQueue<>(Comparator.comparingInt(fScore::get));
        openSet.add(this);
        while (!openSet.isEmpty()) {
            LogisticalPipeEntity current = openSet.poll();
            if (current == target) {
                while (cameFrom.containsKey(current)) {
                    Tuple<LogisticalPipeEntity, Direction> tuple = cameFrom.get(current);
                    current = tuple.getA();
                    current.schedule(item.getStack(), tuple.getB());
                    current.setChanged();
                    level.sendBlockUpdated(current.getBlockPos(), current.getBlockState(), current.getBlockState(), 2);
                }
            }
            for (Direction side : current.logistics.keySet()) {
                Tuple<BlockPos, Integer> tuple = current.logistics.get(side);
                if (level.getBlockEntity(tuple.getA()) instanceof LogisticalPipeEntity neighbour) {
                    int newScore = gScore.get(current) + tuple.getB();
                    if (!gScore.containsKey(neighbour) || newScore < gScore.get(neighbour)) {
                        cameFrom.put(neighbour, new Tuple<>(current, side));
                        gScore.put(neighbour, newScore);
                        fScore.put(neighbour, newScore + this.worldPosition.distChessboard(neighbour.worldPosition));
                        if (!openSet.contains(neighbour)) {
                            openSet.add(neighbour);
                        }
                    }
                }
            }
        }
    }

    public void setLogisticalNetwork(LogisticalNetwork logisticalNetwork, ServerLevel level) {
        if (logisticalNetwork != null) {
            logisticalNetwork.addPipe(this);
        }
        this.logisticalNetwork = logisticalNetwork;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    public LogisticalNetwork getLogisticalNetwork() {
        return this.logisticalNetwork;
    }

    public boolean hasLogisticalNetwork() {
        return this.logisticalNetwork != null;
    }

    public void setController(boolean controller) {
        this.controller = controller;
    }

    public boolean isController() {
        return this.controller;
    }

    public void disconnect(ServerLevel level) {
        this.setController(false);
        this.routingSchedule.clear();
        if (this.hasLogisticalNetwork()) {
            this.getLogisticalNetwork().removePipe(this);
        }
        this.setLogisticalNetwork(null, level);
    }

    public boolean stillLinkedToNetwork(LogisticalNetwork network, ServerLevel level, BlockPos thisPos, Set<LogisticalPipeEntity> visited) {
        if (visited.contains(this)) {
            return false;
        }
        visited.add(this);
        if (network.getPos().equals(thisPos)) {
            return true;
        }
        for (Direction direction : this.logistics.keySet()) {
            BlockPos nextPos = this.logistics.get(direction).getA();
            if (level.getBlockEntity(nextPos) instanceof LogisticalPipeEntity nextPipe) {
                if (nextPipe.stillLinkedToNetwork(network, level, nextPos, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    public LogisticalNetwork findLinkedNetwork(ServerLevel level, BlockPos thisPos, Set<LogisticalPipeEntity> visited) {
        if (visited.contains(this)) {
            return null;
        }
        visited.add(this);
        if (this.hasLogisticalNetwork() && this.getLogisticalNetwork().getPos().equals(thisPos)) {
            return this.getLogisticalNetwork();
        }
        for (Direction direction : this.logistics.keySet()) {
            BlockPos nextPos = this.logistics.get(direction).getA();
            if (level.getBlockEntity(nextPos) instanceof LogisticalPipeEntity nextPipe) {
                LogisticalNetwork network = nextPipe.findLinkedNetwork(level, nextPos, visited);
                if (network != null) {
                    return network;
                }
            }
        }
        return null;
    }

    public void disconnectAllLinked(ServerLevel level, Set<LogisticalPipeEntity> visited) {
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
        this.disconnect(level);
        for (Direction direction : this.logistics.keySet()) {
            BlockPos nextPos = this.logistics.get(direction).getA();
            if (level.getBlockEntity(nextPos) instanceof LogisticalPipeEntity nextPipe) {
                nextPipe.disconnectAllLinked(level, visited);
            }
        }
    }

    private void joinOrMakeNetwork(ServerLevel level, BlockPos pos) {
        LogisticalNetwork network = this.findLinkedNetwork(level, pos, new HashSet<>());
        if (network != null) {
            this.setLogisticalNetwork(network, level);
            this.setController(network.getPos().equals(pos));
        } else {
            this.distributeLogisticalNetwork(level, pos, new HashSet<>(), new LogisticalNetwork(pos));
        }
    }

    public void networkChanged(ServerLevel level, BlockPos pos, boolean isLinked) {
        if (this.hasLogisticalNetwork()) {
            if (!isLinked) {
                if (!this.stillLinkedToNetwork(this.getLogisticalNetwork(), level, pos, new HashSet<>())) {
                    this.disconnectAllLinked(level, new HashSet<>());
                    this.distributeLogisticalNetwork(level, pos, new HashSet<>(), new LogisticalNetwork(pos));
                }
            } else {
                this.distributeLogisticalNetwork(level, pos, new HashSet<>(), this.getLogisticalNetwork());
            }
        } else {
            this.joinOrMakeNetwork(level, pos);
        }
    }

    @Override
    public short getTargetSpeed() {
        return ItemInPipe.SPEED_LIMIT;
    }

    @Override
    public short getAcceleration() {
        return ItemInPipe.DEFAULT_ACCELERATION * 64;
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.routingSchedule.clear();
        this.setController(valueInput.getBooleanOr("controller", false));
        if (this.isController()) {
            this.logisticalNetwork = new LogisticalNetwork(this.getBlockPos(), SortingMode.fromByte(valueInput.getByteOr("sorting_mode", (byte) 1)));
        }
        ValueInput.TypedInputList<ItemStackWithSlot> routingList = valueInput.listOrEmpty("routing_schedule", ItemStackWithSlot.CODEC);
        for (ItemStackWithSlot slotStack : routingList) {
            this.routingSchedule.put(slotStack.stack(), new ScheduledRoute(Direction.from3DDataValue(slotStack.slot())));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putBoolean("controller", this.isController());
        if (this.hasLogisticalNetwork() && this.isController()) {
            valueOutput.putByte("sorting_mode", this.getLogisticalNetwork().getSortingMode().getValue());
        }
        ValueOutput.TypedOutputList<ItemStackWithSlot> routingList = valueOutput.list("routing_schedule", ItemStackWithSlot.CODEC);
        for (ItemStack stack : this.routingSchedule.keySet()) {
            routingList.add(new ItemStackWithSlot(this.routingSchedule.get(stack).getDirection().get3DDataValue(), stack));
        }
    }

}
