package com.cloud.loadBalancer.beans;
import com.cloud.loadBalancer.constants.VMInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ControllerStats {
    private HashMap<Integer, VmCpuMemPerc> controller_stats_map = new HashMap<>();

    public ControllerStats() {
        for (int i = 0; i < VMInfo.VM_COUNT; i++) {
            VmCpuMemPerc stats = new VmCpuMemPerc(0,0);
            controller_stats_map.put(i,stats);
        }
    }

    public  void updateControllerStats(int controller_port, float cpu_per, float mem_per) {
        VmCpuMemPerc stats= new VmCpuMemPerc(cpu_per,mem_per);
        controller_stats_map.put(controller_port,stats);
    }
    public VmCpuMemPerc getControllerStats(int controller_port){
        return controller_stats_map.get(controller_port);
    }
}
