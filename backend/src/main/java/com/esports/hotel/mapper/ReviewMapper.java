package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价Mapper接口
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    // MyBatis-Plus提供了基础的CRUD方法
    // 如需要自定义SQL，可在此处添加方法并在XML中实现
}
