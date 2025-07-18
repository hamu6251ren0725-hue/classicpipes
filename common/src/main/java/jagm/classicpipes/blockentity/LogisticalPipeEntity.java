package jagm.classicpipes.blockentity;

import jagm.classicpipes.block.AbstractPipeBlock;
import jagm.classicpipes.block.NetheritePipeBlock;
import jagm.classicpipes.util.ItemInPipe;
import jagm.classicpipes.util.LogisticalNetwork;
import jagm.classicpipes.util.ScheduledRoute;
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
    public BlockPos toLoad;
    private byte loadAttempts;

    public LogisticalPipeEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.routingSchedule = new HashMap<>();
        this.logisticalNetwork = null;
        this.controller = false;
        this.toLoad = null;
        this.loadAttempts = 0;
    }

    public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        super.tickServer(level, pos, state);
        Iterator<ItemStack> iterator = this.routingSchedule.keySet().iterator();
        while (iterator.hasNext()) {
            ScheduledRoute route = this.routingSchedule.get(iterator.next());
            route.tick();
            if (route.timedOut()) {
                iterator.remove();
                this.setChanged();
            }
        }
        if (!this.hasLogisticalNetwork()) {
            if (this.toLoad != null && this.loadAttempts < 3) {
                if (level.getBlockEntity(this.toLoad) instanceof LogisticalPipeEntity controllerPipe) {
                    if (controllerPipe.hasLogisticalNetwork()) {
                        this.setLogisticalNetwork(controllerPipe.getLogisticalNetwork(), level, pos, state);
                        this.toLoad = null;
                    }
                }
                this.loadAttempts++;
            } else {
                boolean foundNetwork = false;
                for (Direction direction : this.logistics.keySet()) {
                    BlockPos nextPos = this.logistics.get(direction).getA();
                    if (level.getBlockEntity(nextPos) instanceof LogisticalPipeEntity nextPipe) {
                        if (nextPipe.getLogisticalNetwork() != null) {
                            this.setLogisticalNetwork(nextPipe.getLogisticalNetwork(), level, pos, state);
                            foundNetwork = true;
                            break;
                        }
                    }
                }
                if (!foundNetwork) {
                    this.distributeLogisticalNetwork(level, null, pos, state, new HashSet<>(), new LogisticalNetwork(pos));
                    this.setController(true);
                }
            }
        }
    }

    protected void distributeLogisticalNetwork(ServerLevel level, Direction fromDirection, BlockPos pos, BlockState state, Set<LogisticalPipeEntity> visited, LogisticalNetwork logisticalNetwork) {
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
        if (this.hasLogisticalNetwork()) {
            logisticalNetwork.merge(this.getLogisticalNetwork());
        } else {
            for (Direction direction : this.logistics.keySet()) {
                if (!direction.equals(fromDirection)) {
                    BlockPos nextPos = this.logistics.get(direction).getA();
                    if (level.getBlockEntity(nextPos) instanceof LogisticalPipeEntity nextPipe) {
                        nextPipe.distributeLogisticalNetwork(level, direction, nextPos, state, visited, logisticalNetwork);
                    }
                }
            }
        }
        this.setController(false);
        this.setLogisticalNetwork(logisticalNetwork, level, pos, state);

    }

    @Override
    public void routeItem(BlockState state, ItemInPipe item) {
        if (!this.hasLogisticalNetwork()) {
            super.routeItem(state, item);
            return;
        }
        if (!this.checkRoutingSchedule(item)) {
            List<NetheriteBasicPipeEntity> validTargets = new ArrayList<>();
            for (NetheriteBasicPipeEntity routingPipe : this.logisticalNetwork.getRoutingPipes()) {
                if (routingPipe.canRouteItemHere(item.getStack())) {
                    validTargets.add(routingPipe);
                }
            }
            if (validTargets.isEmpty()) {
                validTargets.addAll(this.logisticalNetwork.getDefaultRoutes());
            }
            if (this.getLevel() instanceof ServerLevel serverLevel) {
                if (this instanceof NetheriteBasicPipeEntity && validTargets.contains(this)) {
                    List<Direction> validDirections = new ArrayList<>();
                    for (Direction direction : Direction.values()) {
                        if (state.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction)) && !state.getValue(NetheritePipeBlock.LINKED_PROPERTY_BY_DIRECTION.get(direction))) {
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
        routingSchedule.put(stack, new ScheduledRoute(direction));
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

    public void setLogisticalNetwork(LogisticalNetwork logisticalNetwork, ServerLevel level, BlockPos pos, BlockState state) {
        if (logisticalNetwork != null) {
            logisticalNetwork.addPipe(this);
        }
        this.logisticalNetwork = logisticalNetwork;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(pos, state, state, 2);
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
        this.setLogisticalNetwork(null, level, this.getBlockPos(), this.getBlockState());
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
        this.setController(valueInput.getBooleanOr("controller", false));
        if (this.isController()) {
            this.logisticalNetwork = new LogisticalNetwork(this.getBlockPos(), this);
            this.toLoad = this.getBlockPos();
        } else {
            valueInput.read("network_pos", BlockPos.CODEC).ifPresent(pos -> this.toLoad = pos);
        }
        this.routingSchedule.clear();
        ValueInput.TypedInputList<ItemStackWithSlot> routingList = valueInput.listOrEmpty("routing_schedule", ItemStackWithSlot.CODEC);
        for (ItemStackWithSlot slotStack : routingList) {
            this.routingSchedule.put(slotStack.stack(), new ScheduledRoute(Direction.from3DDataValue(slotStack.slot())));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (this.hasLogisticalNetwork()) {
            valueOutput.store("network_pos", BlockPos.CODEC, this.getLogisticalNetwork().getPos());
        }
        valueOutput.putBoolean("controller", this.isController());
        ValueOutput.TypedOutputList<ItemStackWithSlot> routingList = valueOutput.list("routing_schedule", ItemStackWithSlot.CODEC);
        for (ItemStack stack : this.routingSchedule.keySet()) {
            routingList.add(new ItemStackWithSlot(this.routingSchedule.get(stack).getDirection().get3DDataValue(), stack));
        }
    }

}
