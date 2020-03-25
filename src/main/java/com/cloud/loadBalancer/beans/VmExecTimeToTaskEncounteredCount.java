package com.cloud.loadBalancer.beans;

public class VmExecTimeToTaskEncounteredCount {
    public VmExecTimeToTaskEncounteredCount(int vmId, Long executionTime, int taskEncounteredCount) {
        this.vmId = vmId;
        this.executionTime = executionTime;
        this.taskEncounteredCount = taskEncounteredCount;
    }

    //TODO set vmIds universally in the code
    private int vmId;
    private Long executionTime;

    //TODO Find a way to remove int since we may reach the limit, maybe find some logic to reset it
    private int taskEncounteredCount;

    public int getVmId() {
        return vmId;
    }

    public void setVmId(int vmId) {
        this.vmId = vmId;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public int getTaskEncounteredCount() {
        return taskEncounteredCount;
    }

    public void setTaskEncounteredCount(int taskEncounteredCount) {
        this.taskEncounteredCount = taskEncounteredCount;
    }


}
