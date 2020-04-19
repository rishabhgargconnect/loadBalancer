package com.cloud.loadBalancer.schedulers;

import com.cloud.loadBalancer.controllers.ExceptionHandlingController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@ComponentScan
public class ExceptionTasksScheduler {

    ExceptionHandlingController exceptionHandlingController = new ExceptionHandlingController();

    @Scheduled(fixedRate = 10000)
    public void handleExceptionTasks() {
        System.out.println("!!!!!!!Scheduler invoked!!!!!!!!!!!!!!!!!");
        exceptionHandlingController.handleException();
    }
}
