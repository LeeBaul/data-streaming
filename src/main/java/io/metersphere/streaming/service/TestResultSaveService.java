package io.metersphere.streaming.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.streaming.base.domain.LoadTestReportResult;
import io.metersphere.streaming.base.domain.LoadTestReportResultPart;
import io.metersphere.streaming.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.streaming.base.mapper.LoadTestReportMapper;
import io.metersphere.streaming.base.mapper.LoadTestReportResultMapper;
import io.metersphere.streaming.base.mapper.LoadTestReportResultPartMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportResultMapper;
import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.commons.constants.TestStatus;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.report.summary.SummaryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestResultSaveService {
    @Resource
    private LoadTestReportResultMapper loadTestReportResultMapper;
    @Resource
    private LoadTestReportResultPartMapper loadTestReportResultPartMapper;
    @Resource
    private ExtLoadTestReportResultMapper extLoadTestReportResultMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ObjectMapper objectMapper;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(30, 30,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    public void saveResult(LoadTestReportResult record) {
        int i = extLoadTestReportResultMapper.updateReportValue(record);
        if (i == 0) {
            loadTestReportResultMapper.insertSelective(record);
        }
    }

    public boolean isReportingSet(String reportId) {
        int i = extLoadTestReportResultMapper.updateReportStatus(reportId, ReportKeys.ResultStatus.name(), "Ready", "Reporting");
        return i != 0;
    }

    public void saveReportReadyStatus(String reportId) {
        extLoadTestReportResultMapper.updateReportStatus(reportId, ReportKeys.ResultStatus.name(), "Reporting", "Ready");
    }

    public void saveReportCompletedStatus(String reportId) {
        // 保存最终 为 completed
        extLoadTestReportResultMapper.updateReportStatus(reportId, ReportKeys.ResultStatus.name(), "Reporting", "Completed");
        extLoadTestReportResultMapper.updateReportStatus(reportId, ReportKeys.ResultStatus.name(), "Ready", "Completed");
    }

    public void saveResultPart(LoadTestReportResultPart testResult) {
        if (loadTestReportResultPartMapper.updateByPrimaryKeyWithBLOBs(testResult) == 0) {
            loadTestReportResultPartMapper.insert(testResult);
        }
    }

    private void saveSummary(String reportId, String reportKey) {
        try {
            Object summary = SummaryFactory.getSummaryExecutor(reportKey).execute(reportId);
            LoadTestReportResult record = new LoadTestReportResult();
            record.setId(UUID.randomUUID().toString());
            record.setReportId(reportId);
            record.setReportKey(reportKey);
            record.setReportValue(objectMapper.writeValueAsString(summary));
            saveResult(record);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void saveAllSummary(String reportId, List<String> reportKeys) {
        CountDownLatch countDownLatch = new CountDownLatch(reportKeys.size());
        for (String key : reportKeys) {
            threadPoolExecutor.execute(() -> {
                try {
                    saveSummary(reportId, key);
                } catch (Exception e) {
                    LogUtil.error("reportId: " + reportId + ", key:" + key, e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public boolean updateReportStatus(String reportId) {
        LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey(reportId);
        if (report == null) {
            LogUtil.warn("报告不存在: {}", reportId);
            return false;
        }
        extLoadTestReportMapper.updateStatus(reportId, TestStatus.Running.name(), TestStatus.Starting.name());
        extLoadTestMapper.updateStatus(report.getTestId(), TestStatus.Running.name(), TestStatus.Starting.name());
        return true;
    }
}
