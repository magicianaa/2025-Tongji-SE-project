package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.Guest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 住客 Mapper
 */
@Mapper
public interface GuestMapper extends BaseMapper<Guest> {
}
