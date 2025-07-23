package jagm.classicpipes.util;

import jagm.classicpipes.blockentity.LogisticalPipeEntity;
import jagm.classicpipes.blockentity.ProviderPipeEntity;
import jagm.classicpipes.blockentity.RoutingPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.Set;

public class LogisticalNetwork {

    private final BlockPos pos;
    private final Set<RoutingPipeEntity> routingPipes;
    private final Set<RoutingPipeEntity> defaultRoutes;
    private final Set<ProviderPipeEntity> providerPipes;

    public LogisticalNetwork(BlockPos pos, LogisticalPipeEntity... pipes) {
        this.routingPipes = new HashSet<>();
        this.defaultRoutes = new HashSet<>();
        this.providerPipes = new HashSet<>();
        for (LogisticalPipeEntity pipe : pipes) {
            this.addPipe(pipe);
        }
        this.pos = pos;
    }

    public void merge(ServerLevel level, LogisticalNetwork otherNetwork) {
        otherNetwork.getAllPipes().forEach(pipe -> {
            this.addPipe(pipe);
            pipe.setLogisticalNetwork(this, level, pipe.getBlockPos(), pipe.getBlockState());
            pipe.setController(false);
        });
    }

    public Set<LogisticalPipeEntity> getAllPipes() {
        Set<LogisticalPipeEntity> allPipes = new HashSet<>();
        allPipes.addAll(this.routingPipes);
        allPipes.addAll(this.providerPipes);
        return allPipes;
    }

    public Set<RoutingPipeEntity> getRoutingPipes() {
        return this.routingPipes;
    }

    public Set<RoutingPipeEntity> getDefaultRoutes() {
        return this.defaultRoutes;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void destroy(ServerLevel level) {
        this.getAllPipes().forEach(pipe -> pipe.disconnect(level));
    }

    public void addPipe(LogisticalPipeEntity pipe) {
        if (pipe instanceof RoutingPipeEntity routingPipe) {
            this.routingPipes.add(routingPipe);
            if (routingPipe.isDefaultRoute()) {
                this.defaultRoutes.add(routingPipe);
            }
        } else if (pipe instanceof ProviderPipeEntity providerPipe) {
            this.providerPipes.add(providerPipe);
        }
    }

}
