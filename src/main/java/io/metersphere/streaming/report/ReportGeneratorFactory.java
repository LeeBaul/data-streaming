package io.metersphere.streaming.report;

import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.report.impl.AbstractReport;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReportGeneratorFactory {
    private static List<AbstractReport> reportGenerators = new ArrayList<>();

    public synchronized static List<AbstractReport> getReportGenerators() {
        if (CollectionUtils.isNotEmpty(reportGenerators)) {
            return reportGenerators;
        }

        Reflections reflections = new Reflections(ReportGeneratorFactory.class);
        Set<Class<? extends AbstractReport>> subTypes = reflections.getSubTypesOf(AbstractReport.class);
        List<AbstractReport> result = new ArrayList<>();
        subTypes.forEach(s -> {
            try {
                result.add(s.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
        reportGenerators = result;
        return reportGenerators;
    }
}

