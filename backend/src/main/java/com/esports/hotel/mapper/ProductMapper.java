package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
