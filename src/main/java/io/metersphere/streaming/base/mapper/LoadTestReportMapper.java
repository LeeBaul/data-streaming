package io.metersphere.streaming.base.mapper;

import io.metersphere.streaming.base.domain.LoadTestReport;
import io.metersphere.streaming.base.domain.LoadTestReportExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoadTestReportMapper {
    long countByExample(LoadTestReportExample example);

    int deleteByExample(LoadTestReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoadTestReport record);

    int insertSelective(LoadTestReport record);

    List<LoadTestReport> selectByExampleWithBLOBs(LoadTestReportExample example);

    List<LoadTestReport> selectByExample(LoadTestReportExample example);

    LoadTestReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoadTestReport record, @Param("example") LoadTestReportExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReport record, @Param("example") LoadTestReportExample example);

    int updateByExample(@Param("record") LoadTestReport record, @Param("example") LoadTestReportExample example);

    int updateByPrimaryKeySelective(LoadTestReport record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReport record);

    int updateByPrimaryKey(LoadTestReport record);
}