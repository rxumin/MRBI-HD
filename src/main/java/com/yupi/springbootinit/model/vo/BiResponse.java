package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * Bi 的返回结果
 */
@Data
public class BiResponse {

    /**
     * e-charts代码
     */
    private String genChart;

    /**
     * 分析结果
     */
    private String genResult;

    private Long chartId;
}
