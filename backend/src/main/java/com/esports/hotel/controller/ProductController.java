package com.esports.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.ProductDTO;
import com.esports.hotel.entity.Product;
import com.esports.hotel.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * 获取商品列表（分页）
     */
    @GetMapping
    public Result<Page<ProductDTO>> getProducts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String keyword) {
        Page<ProductDTO> page = productService.getProducts(pageNum, pageSize, productType, keyword);
        return Result.success(page);
    }
    
    /**
     * 获取所有可用商品（不分页，用于下单）
     */
    @GetMapping("/available")
    public Result<List<ProductDTO>> getAvailableProducts() {
        List<ProductDTO> products = productService.getAvailableProducts();
        return Result.success(products);
    }
    
    /**
     * 根据ID获取商品
     */
    @GetMapping("/{productId}")
    public Result<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDTO product = productService.getProductById(productId);
        return Result.success(product);
    }
    
    /**
     * 添加商品
     */
    @PostMapping
    public Result<Long> addProduct(@RequestBody Product product) {
        Long productId = productService.addProduct(product);
        return Result.success(productId);
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/{productId}")
    public Result<Void> updateProduct(@PathVariable Long productId, @RequestBody Product product) {
        productService.updateProduct(productId, product);
        return Result.success();
    }
    
    /**
     * 删除商品（物理删除）
     */
    @DeleteMapping("/{productId}")
    public Result<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return Result.success();
    }
    
    /**
     * 下架商品（软删除）
     */
    @PutMapping("/{productId}/disable")
    public Result<Void> disableProduct(@PathVariable Long productId) {
        productService.disableProduct(productId);
        return Result.success();
    }
    
    /**
     * 更新库存
     */
    @PutMapping("/{productId}/stock")
    public Result<Void> updateStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        productService.updateStock(productId, quantity);
        return Result.success();
    }
}
