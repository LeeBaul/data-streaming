package io.metersphere.streaming.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestReport implements Serializable {
    private String id;

    private String testId;

    private String name;

    private Long createTime;

    private Long updateTime;

    private String status;

    private static final long serialVersionUID = 1L;
}