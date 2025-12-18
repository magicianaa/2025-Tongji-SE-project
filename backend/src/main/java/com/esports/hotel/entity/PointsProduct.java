package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 积分商城商品表实体
 */
@Data
@TableName("tb_points_product")
public class PointsProduct implements Serializable {

    @TableId(value = "points_product_id", type = IdType.AUTO)
    private Long pointsProductId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品类型：PHYSICAL（实物）
     */
    private String productType;

    /**
     * 所需积分
     */
    private Integer pointsRequired;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品图片URL
     */
    private String imageUrl;

    /**
     * 是否上架
     */
    private Boolean isAvailable;

    /**
     * 关联POS商品ID（实物类）
     */
    private Long relatedProductId;
}
