package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
public interface ChartService extends IService<Chart> {

    BiResponse genChart(MultipartFile multipartFile, String name, String goal, String chartType);
}
