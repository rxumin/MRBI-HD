package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

    @Resource
    private AiManager aiManager;

    @Resource
    private UserService userService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 异步
     * @param multipartFile
     * @param name
     * @param goal
     * @param chartType
     * @param request
     * @return
     */
    @Override
    public BiResponse genChart(MultipartFile multipartFile, String name, String goal, String chartType, HttpServletRequest request) {
        long biModelId = 1659171950288818178L;
        User loginUser = userService.getLoginUser(request);
        // 分析需求：
        // 分析网站用户的增长情况
        // 原始数据：
        // 日期,用户数
        // 1号,10
        // 2号,20
        // 3号,30

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 将前端传来的数据，先插入到数据库，将状态改为wait
        Long chartID = FirstSave(name, goal, csvData, chartType, loginUser.getId());

        // 调用线程池
        CompletableFuture.runAsync(() ->{
            // 将状态改为running
            handleChartStatusRunning(chartID);

            // 调用AI
            String result = aiManager.doChat(biModelId, userInput.toString());
            String[] splits = result.split("【【【【【");
            if (splits.length < 3) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();

            // 将状态改为成功
            handleChartStatusSucceed(chartID,genChart,genResult);

        },threadPoolExecutor);


        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chartID);
        return biResponse;
    }


    /**
     * 统一管理图标状态
     * @param chartId
     * @param genChart
     * @param genResult
     */
    private void handleChartStatusSucceed(Long chartId,String genChart,String genResult) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setGenChart(genChart);
        updateChart.setGenResult(genResult);
        updateChart.setStatus("succeed");
        boolean updateResult = this.updateById(updateChart);
        if(!updateResult){
            log.error(chartId+"更新最succeed失败");
        }
    }

    private void handleChartStatusRunning(Long chartId) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("running");
        boolean updateResult = this.updateById(updateChart);
        if(!updateResult){
            log.error(chartId+"更新图标running失败");
        }
    }

    private Long FirstSave(String name, String goal, String chartType, String csvData,Long userId) {
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(userId);
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        return chart.getId();

    }
}




