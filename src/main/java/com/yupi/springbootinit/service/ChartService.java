package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface ChartService extends IService<Chart> {

    long toAI(MultipartFile multipartFile, String name, String goal, String chartType, User loginUser);


    BiResponse genChart(MultipartFile multipartFile, String name, String goal, String chartType, HttpServletRequest request);
}
