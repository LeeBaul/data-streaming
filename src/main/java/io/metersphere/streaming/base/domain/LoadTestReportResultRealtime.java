package io.metersphere.streaming.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoadTestReportResultRealtime extends LoadTestReportResultRealtimeKey implements Serializable {
    private Long duration;

    private String reportValue;

    private static final long serialVersionUID = 1L;
}