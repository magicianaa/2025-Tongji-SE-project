package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.dto.ProductDTO;
import com.esports.hotel.entity.Product;
import com.esports.hotel.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductMapper productMapper;
    
    /**
     * 获取商品列表（分页）
     */
    public Page<ProductDTO> getProducts(int pageNum, int pageSize, String productType, String keyword) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        if (productType != null && !productType.isEmpty()) {
            wrapper.eq(Product::getProductType, productType);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getProductName, keyword)
                             .or().like(Product::getCategory, keyword));
        }
        
        wrapper.orderByDesc(Product::getProductId);
        
        Page<Product> productPage = productMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<ProductDTO> dtoPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        
        List<ProductDTO> dtoList = productPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
    
    /**
     * 获取所有可用商品（不分页，用于下单）
     */
    public List<ProductDTO> getAvailableProducts() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsAvailable, true)
               .gt(Product::getStockQuantity, 0)
               .orderByAsc(Product::getProductType)
               .orderByAsc(Product::getProductName);
        
        List<Product> products = productMapper.selectList(wrapper);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取商品
     */
    public ProductDTO getProductById(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return convertToDTO(product);
    }
    
    /**
     * 添加商品
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addProduct(Product product) {
        // 设置默认值
        if (product.getIsAvailable() == null) {
            product.setIsAvailable(true);
        }
        if (product.getStockThreshold() == null) {
            product.setStockThreshold(5);
        }
        if (product.getRentalUnit() == null) {
            product.setRentalUnit("NONE");
        }
        
        productMapper.insert(product);
        log.info("添加商品成功: productId={}, name={}", product.getProductId(), product.getProductName());
        return product.getProductId();
    }
    
    /**
     * 更新商品
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long productId, Product product) {
        Product existing = productMapper.selectById(productId);
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        
        product.setProductId(productId);
        productMapper.updateById(product);
        log.info("更新商品成功: productId={}, name={}", productId, product.getProductName());
    }
    
    /**
     * 删除商品（物理删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 物理删除
        productMapper.deleteById(productId);
        log.info("删除商品成功: productId={}, name={}", productId, product.getProductName());
    }
    
    /**
     * 下架商品（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        product.setIsAvailable(false);
        productMapper.updateById(product);
        log.info("下架商品成功: productId={}, name={}", productId, product.getProductName());
    }
    
    /**
     * 更新库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new BusinessException("库存不足");
        }
        
        product.setStockQuantity(newStock);
        productMapper.updateById(product);
        log.info("更新库存: productId={}, oldStock={}, change={}, newStock={}", 
                productId, product.getStockQuantity() - quantity, quantity, newStock);
    }
    
    /**
     * 转换为DTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        
        // 判断是否低库存
        dto.setIsLowStock(product.getStockQuantity() != null && 
                         product.getStockQuantity() <= product.getStockThreshold());
        
        return dto;
    }
}
