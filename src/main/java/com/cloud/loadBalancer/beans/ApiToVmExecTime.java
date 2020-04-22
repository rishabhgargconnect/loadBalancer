package com.cloud.loadBalancer.beans;

import com.cloud.loadBalancer.constants.VMInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ApiToVmExecTime {

    //private Map<String, List<VmExecTimeToTaskEncounteredCount>> apiToVmExecTime = new ConcurrentHashMap<>();
    private Map<String, List<VmExecTimeToTaskEncounteredCount>> apiToVmExecTime = new ConcurrentHashMap<>();
    public void setExecTimeForApiVmPair(String api, int vmId, long execTime) {
        /*if (apiToVmExecTime.get(api) == null) {
            VmExecTimeToTaskEncounteredCount vm = new VmExecTimeToTaskEncounteredCount(vmId,execTime,1);
            List<VmExecTimeToTaskEncounteredCount> vm_list = new ArrayList<>();
            vm_list.add(vm);
            System.out.println("Darakshan null "+api);
            apiToVmExecTime.put(api, vm_list);
        }*/
        List<VmExecTimeToTaskEncounteredCount> vmExecTimeToTaskEncounteredCounts = apiToVmExecTime.get(api);
        /*for(int i=0;i<vmExecTimeToTaskEncounteredCounts.size();i++){
            if(vmExecTimeToTaskEncounteredCounts.get(i).getVmId() == vmId){
                VmExecTimeToTaskEncounteredCount vmExecTimeToTaskEncounteredCount = vmExecTimeToTaskEncounteredCounts.get(i);
                //System.out.println("Inside if" + vmId);
                //System.out.println("print the vm id index " +vmId +" and the actual vm id "+vmExecTimeToTaskEncounteredCount.getVmId());
                int taskEncounteredCountUntilNow = vmExecTimeToTaskEncounteredCount.getTaskEncounteredCount();
                Long executionTimeAvgUntilNow = vmExecTimeToTaskEncounteredCount.getExecutionTime();
                //long newExecTimeAvg = executionTimeAvgUntilNow + (execTime - executionTimeAvgUntilNow) / taskEncounteredCountUntilNow;
                long newExecTimeAvg = (taskEncounteredCountUntilNow * executionTimeAvgUntilNow + execTime)/ (taskEncounteredCountUntilNow+1);
                int newTaskEncounteredCount = taskEncounteredCountUntilNow+1;
                vmExecTimeToTaskEncounteredCount.setExecutionTime(newExecTimeAvg);
                vmExecTimeToTaskEncounteredCount.setTaskEncounteredCount(newTaskEncounteredCount);
                break;
            }
        }*/
        VmExecTimeToTaskEncounteredCount vmExecTimeToTaskEncounteredCount = vmExecTimeToTaskEncounteredCounts.get(vmId);
        int taskEncounteredCountUntilNow = vmExecTimeToTaskEncounteredCount.getTaskEncounteredCount();
        Long executionTimeAvgUntilNow = vmExecTimeToTaskEncounteredCount.getExecutionTime();
        //long newExecTimeAvg = executionTimeAvgUntilNow + (execTime - executionTimeAvgUntilNow) / taskEncounteredCountUntilNow;
        long newExecTimeAvg = (taskEncounteredCountUntilNow * executionTimeAvgUntilNow + execTime)/ (taskEncounteredCountUntilNow+1);
        int newTaskEncounteredCount = taskEncounteredCountUntilNow+1;
        vmExecTimeToTaskEncounteredCount.setExecutionTime(newExecTimeAvg);
        vmExecTimeToTaskEncounteredCount.setTaskEncounteredCount(newTaskEncounteredCount);

    }

    public List<VmExecTimeToTaskEncounteredCount> getVmExecutionTimesForApi(String api) {
        List <VmExecTimeToTaskEncounteredCount> vmExecTimeToTaskEncounteredCounts = apiToVmExecTime.get(api);
        if (vmExecTimeToTaskEncounteredCounts == null) {
            vmExecTimeToTaskEncounteredCounts = new CopyOnWriteArrayList<>();
            for (int i = 0; i < VMInfo.VM_COUNT; i++) {
                vmExecTimeToTaskEncounteredCounts.add(new VmExecTimeToTaskEncounteredCount(i, Long.MIN_VALUE, 0));
            }
        }
        apiToVmExecTime.put(api,vmExecTimeToTaskEncounteredCounts);

        return vmExecTimeToTaskEncounteredCounts;
    }

}
