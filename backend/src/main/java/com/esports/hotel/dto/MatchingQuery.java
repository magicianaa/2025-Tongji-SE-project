package com.esports.hotel.dto;

import lombok.Data;

/**
 * 匹配推荐查询条件DTO
 */
@Data
public class MatchingQuery {
    
    private String gameType;
    
    private String rank;
    
    private String position;
    
    // 分页参数
    private Integer page = 1;
    
    private Integer size = 10;
}
