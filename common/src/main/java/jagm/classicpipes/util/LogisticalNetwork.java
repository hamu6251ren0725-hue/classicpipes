package jagm.classicpipes.util;

import jagm.classicpipes.blockentity.LogisticalPipeEntity;

import java.util.Arrays;
import java.util.List;

public class LogisticalNetwork {

    private final List<LogisticalPipeEntity> pipes;

    public LogisticalNetwork(LogisticalPipeEntity... pipes) {
        this.pipes = Arrays.asList(pipes);
    }

    public void merge(LogisticalNetwork otherNetwork) {
        this.pipes.addAll(otherNetwork.getPipes());
    }

    public List<LogisticalPipeEntity> getPipes() {
        return pipes;
    }

}
