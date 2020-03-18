package com.cloud.loadBalancer.responseDto;

import lombok.Data;

@Data
public class StudentInfoDto {
    private String name;
    private int age;

    public StudentInfoDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
