package com.cloud.loadBalancer.beans;

import com.cloud.loadBalancer.constants.VMInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiToVmExecTime {

    private Map<String, List<VmExecTimeToTaskEncounteredCount>> apiToVmExecTime = new ConcurrentHashMap<>();

    public void setExecTimeForApiVmPair(String api, int vmId, long execTime) {
        if (apiToVmExecTime.get(api) == null) {
            apiToVmExecTime.put(api, new ArrayList<>());
        }
        List<VmExecTimeToTaskEncounteredCount> vmExecTimeToTaskEncounteredCounts = apiToVmExecTime.get(api);

        VmExecTimeToTaskEncounteredCount vmExecTimeToTaskEncounteredCount = vmExecTimeToTaskEncounteredCounts.get(vmId);
        int taskEncounteredCountUntilNow = vmExecTimeToTaskEncounteredCount.getTaskEncounteredCount();
        Long executionTimeAvgUntilNow = vmExecTimeToTaskEncounteredCount.getExecutionTime();
        long newExecTimeAvg = executionTimeAvgUntilNow + (execTime - executionTimeAvgUntilNow) / taskEncounteredCountUntilNow;
        int newTaskEncounteredCount = taskEncounteredCountUntilNow + 1;
        vmExecTimeToTaskEncounteredCount.setExecutionTime(newExecTimeAvg);
        vmExecTimeToTaskEncounteredCount.setTaskEncounteredCount(newTaskEncounteredCount);
    }

    public List<VmExecTimeToTaskEncounteredCount> getVmExecutionTimesForApi(String api) {
        List<VmExecTimeToTaskEncounteredCount> vmExecTimeToTaskEncounteredCounts = apiToVmExecTime.get(api);
        if (vmExecTimeToTaskEncounteredCounts == null) {
            vmExecTimeToTaskEncounteredCounts = new ArrayList<>();
            for (int i = 0; i < VMInfo.VM_COUNT; i++) {
                vmExecTimeToTaskEncounteredCounts.add(new VmExecTimeToTaskEncounteredCount(0, Long.MAX_VALUE, 0));
            }
        }

        return vmExecTimeToTaskEncounteredCounts;
    }

}
