package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.Staff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工信息Mapper
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
}
