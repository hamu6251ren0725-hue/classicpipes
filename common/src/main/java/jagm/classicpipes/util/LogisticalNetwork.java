package jagm.classicpipes.util;

import jagm.classicpipes.blockentity.LogisticalPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LogisticalNetwork {

    private final BlockPos pos;
    private final Set<LogisticalPipeEntity> pipes;

    public LogisticalNetwork(BlockPos pos, LogisticalPipeEntity... pipes) {
        this.pipes = new HashSet<>();
        this.pipes.addAll(Arrays.asList(pipes));
        this.pos = pos;
    }

    public void merge(LogisticalNetwork otherNetwork) {
        this.pipes.addAll(otherNetwork.getPipes());
    }

    public Set<LogisticalPipeEntity> getPipes() {
        return this.pipes;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void destroy(ServerLevel level) {
        for (LogisticalPipeEntity pipe : this.pipes) {
            pipe.setController(false);
            pipe.setLogisticalNetwork(null, level, pipe.getBlockPos(), pipe.getBlockState());
        }
    }

    public void addPipe(LogisticalPipeEntity pipe) {
        this.pipes.add(pipe);
    }

}
