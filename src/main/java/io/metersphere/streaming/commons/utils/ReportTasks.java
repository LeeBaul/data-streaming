package io.metersphere.streaming.commons.utils;

import io.metersphere.streaming.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.streaming.base.mapper.LoadTestReportMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;

@Service
public class ReportTasks {
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Future<?>>> reportTasks = new ConcurrentHashMap<>();
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    private boolean isRunning = true;

    public static void addTask(String reportId, Future<?> future) {
        CopyOnWriteArraySet<Future<?>> tasks = reportTasks.get(reportId);
        if (tasks == null) {
            tasks = new CopyOnWriteArraySet<>();
            reportTasks.put(reportId, tasks);
        }
        tasks.add(future);
        LogUtil.info("添加任务: reportId: {}, taskSize: {}", reportId, tasks.size());
    }

    public static void clearTasks(String reportId) {
        CopyOnWriteArraySet<Future<?>> futures = reportTasks.getOrDefault(reportId, new CopyOnWriteArraySet<>());
        for (Future<?> task : futures) {
            try {
                if (!task.isDone()) {
                    task.cancel(false);
                }
            } catch (Exception e) {
                LogUtil.error("取消任务失败: ", e);
            }
        }
        reportTasks.remove(reportId);
        LogUtil.info("清理任务: reportId: {}", reportId);
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(1000 * 60 * 5);
                    Iterator<String> keys = reportTasks.keys().asIterator();
                    while (keys.hasNext()) {
                        String reportId = keys.next();
                        LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey(reportId);
                        if (report == null) {
                            clearTasks(reportId);
                            LogUtil.info("定时清理遗留任务, 报告已删除: reportId: {}", reportId);
                            return;
                        }
                        if (report.getStatus().equals("Completed")) {
                            clearTasks(reportId);
                            LogUtil.info("定时清理遗留任务, 报告已结束: reportId: {}", reportId);
                        }
                    }
                } catch (InterruptedException e) {
                    LogUtil.error("任务监控线程异常: ", e);
                } catch (Exception e) {
                    LogUtil.error("handle queue error: ", e);
                }
            }
        }).start();
    }

    @PreDestroy
    public void preDestroy() {
        isRunning = false;
    }
}
