package com.cloud.loadBalancer.beans;

public class VmCpuMemPerc {
    private float cpu_utilisation;
    private float mem_utilisation;
    public VmCpuMemPerc(float cpu, float mem) {
        this.cpu_utilisation= cpu;
        this.mem_utilisation = mem;
    }

    public float getCpu_utilisation() {
        return cpu_utilisation;
    }

    public void setCpu_utilisation(float cpu_utilisation) {
        this.cpu_utilisation = cpu_utilisation;
    }

    public float getMem_utilisation() {
        return mem_utilisation;
    }

    public void setMem_utilisation(float mem_utilisation) {
        this.mem_utilisation = mem_utilisation;
    }
}

