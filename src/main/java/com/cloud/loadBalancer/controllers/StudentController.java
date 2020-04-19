package com.cloud.loadBalancer.controllers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.netty.InvocationBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cloud.loadBalancer.responseDto.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StudentController {

    @GetMapping("/detail")
    public StudentInfoDto studentDetails(@RequestParam(value = "name") String name) {
        return new StudentInfoDto(name, 21);
    }


    @GetMapping("/containerInfo")
    public void getContainerDetails(@RequestParam(value = "containerId") String containerId) throws IOException, InterruptedException {
        System.out.println("Hello1");
//        RestTemplate restTemplate = new RestTemplate();
//        String responseEntity = restTemplate.getForObject("http://localhost:2375/containers/" + containerId + "/stats?stream=0", String.class);
//        System.out.println("Hello2");
//        System.out.println(responseEntity);
//        return new StudentInfoDto(name, 21);

        DockerClient client = DockerClientBuilder.getInstance().build();

        InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
        client.statsCmd(containerId).exec(callback);
        Statistics stats = null;
        try {
            stats = callback.awaitResult();
            Map<String, Object> cpuStats = stats.getCpuStats();
            for (String key : cpuStats.keySet()) {
                System.out.println(key + ":" + cpuStats.get(key));
                System.out.println("!!");
            }
            callback.close();
        } catch (RuntimeException | IOException e) {
            // you may want to throw an exception here
        }
        System.out.println(stats);

//        Process process = Runtime.getRuntime().exec("docker stats " + containerId);
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line = null;
////        while (true) {
//        line = in.readLine();
//        if (line != null) {
//            String[] s = line.split(" ");
//            for (String thing : s) {
//                System.out.println(thing);
//                System.out.println("!!");
//            }
////                System.out.println();
//        }
//        Thread.sleep(5000);

//        }


    }
}