package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 战队成员Mapper接口
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
}
