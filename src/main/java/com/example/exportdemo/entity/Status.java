package com.example.exportdemo.entity;

import lombok.Data;

@Data
public class Status extends BaseEntity {
    private String id;
    private String name;
    private String value;
    private String mark;
    private String crateTime;

}
