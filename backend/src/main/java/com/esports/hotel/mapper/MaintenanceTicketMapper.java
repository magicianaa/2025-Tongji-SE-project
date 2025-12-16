package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.MaintenanceTicket;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维修工单 Mapper
 */
@Mapper
public interface MaintenanceTicketMapper extends BaseMapper<MaintenanceTicket> {
}
