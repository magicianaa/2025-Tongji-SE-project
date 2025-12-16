package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.CheckInRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入住记录 Mapper
 */
@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {
}
