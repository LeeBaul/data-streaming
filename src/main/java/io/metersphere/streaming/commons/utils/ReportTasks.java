package io.metersphere.streaming.commons.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

public class ReportTasks {
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Runnable>> reportTasks = new ConcurrentHashMap<>();
    private static final LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    public static void addTask(String reportId, Runnable task) {
        CopyOnWriteArraySet<Runnable> tasks = reportTasks.get(reportId);
        if (tasks == null) {
            tasks = new CopyOnWriteArraySet<>();
            reportTasks.put(reportId, tasks);
        }
        tasks.add(task);
        LogUtil.info("添加任务: reportId: {}, taskSize: {}", reportId, tasks.size());
    }

    public static void clearTasks(String reportId) {
        taskQueue.removeAll(getTasks(reportId));
        reportTasks.remove(reportId);
        LogUtil.info("清理任务: reportId: {}", reportId);
    }

    private static CopyOnWriteArraySet<Runnable> getTasks(String reportId) {
        return reportTasks.getOrDefault(reportId, new CopyOnWriteArraySet<>());
    }

    public static LinkedBlockingQueue<Runnable> getTaskQueue() {
        return taskQueue;
    }
}
