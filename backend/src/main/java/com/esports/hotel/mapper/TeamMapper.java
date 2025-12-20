package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.Team;
import org.apache.ibatis.annotations.Mapper;

/**
 * 战队Mapper接口
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {
}
