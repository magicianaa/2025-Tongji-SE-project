package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.SupplyUsage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物资消耗记录Mapper
 */
@Mapper
public interface SupplyUsageMapper extends BaseMapper<SupplyUsage> {
}
