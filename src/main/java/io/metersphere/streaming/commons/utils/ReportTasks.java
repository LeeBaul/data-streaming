package io.metersphere.streaming.commons.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;

public class ReportTasks {
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Future<?>>> reportTasks = new ConcurrentHashMap<>();

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
        for (Future<?> task : getTasks(reportId)) {
            try {
                if (!task.isDone()) {
                    task.cancel(true);
                }
            } catch (Exception e) {
                LogUtil.error("取消任务失败: ", e);
            }
        }
        reportTasks.remove(reportId);
        LogUtil.info("清理任务: reportId: {}", reportId);
    }

    private static CopyOnWriteArraySet<Future<?>> getTasks(String reportId) {
        return reportTasks.getOrDefault(reportId, new CopyOnWriteArraySet<>());
    }
}
