package jagm.classicpipes.util;

import jagm.classicpipes.blockentity.LogisticalPipeEntity;
import jagm.classicpipes.blockentity.NetheriteBasicPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.Set;

public class LogisticalNetwork {

    private final BlockPos pos;
    private final Set<NetheriteBasicPipeEntity> routingPipes;
    private final Set<NetheriteBasicPipeEntity> defaultRoutes;

    public LogisticalNetwork(BlockPos pos, LogisticalPipeEntity... pipes) {
        this.routingPipes = new HashSet<>();
        this.defaultRoutes = new HashSet<>();
        for (LogisticalPipeEntity pipe : pipes) {
            this.addPipe(pipe);
        }
        this.pos = pos;
    }

    public void merge(LogisticalNetwork otherNetwork) {
        this.routingPipes.addAll(otherNetwork.getRoutingPipes());
    }

    public Set<NetheriteBasicPipeEntity> getRoutingPipes() {
        return this.routingPipes;
    }

    public Set<NetheriteBasicPipeEntity> getDefaultRoutes() {
        return this.defaultRoutes;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void destroy(ServerLevel level) {
        this.routingPipes.forEach(pipe -> pipe.disconnect(level));
    }

    public void addPipe(LogisticalPipeEntity pipe) {
        if (pipe instanceof NetheriteBasicPipeEntity routingPipe) {
            this.routingPipes.add(routingPipe);
            if (routingPipe.isDefaultRoute()) {
                this.defaultRoutes.add(routingPipe);
            }
        }
    }

}
