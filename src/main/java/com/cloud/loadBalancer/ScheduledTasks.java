package com.cloud.loadBalancer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import static com.cloud.loadBalancer.interceptors.HttpReqHandlerInterceptorOverload.controllerStats;

@Component
public class ScheduledTasks {
    @Scheduled(fixedRate = 50000)
    public void reportCurrentTime() throws IOException {

        String command = "docker stats --format \"{{.Name}}--{{.CPUPerc}}--{{.MemPerc}}\"";
        Process process = Runtime.getRuntime().exec(new String[] { "bash"
                ,"-c",command});
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line=in.readLine())!=null)
        {
            String[] controller_stat = line.split("--");
            String controller_port = controller_stat[0];
            String controller_stats_cpuper = controller_stat[1];
            String controller_stats_cpu = controller_stats_cpuper.substring(0,controller_stats_cpuper.length()-1);
            float controller_cpu = Float.valueOf(controller_stats_cpu);
            String controller_stats_memper = controller_stat[2];
            String controller_stats_mem = controller_stats_memper.substring(0,controller_stats_memper.length()-1);
            float controller_mem = Float.valueOf(controller_stats_mem);
            controllerStats.updateControllerStats(Integer.valueOf(controller_port.split(".-")[1]),controller_cpu,controller_mem);
            //System.out.println(controller_port);
            //String[] split = controller_port.split(".");
            //System.out.println(split[0]);
        }
    }
}

