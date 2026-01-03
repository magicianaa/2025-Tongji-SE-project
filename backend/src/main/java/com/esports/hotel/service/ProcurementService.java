package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.dto.ProcurementRequest;
import com.esports.hotel.entity.Procurement;
import com.esports.hotel.entity.Product;
import com.esports.hotel.entity.User;
import com.esports.hotel.mapper.ProcurementMapper;
import com.esports.hotel.mapper.ProductMapper;
import com.esports.hotel.mapper.UserMapper;
import com.esports.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 进货服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcurementService {

    private final ProcurementMapper procurementMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 创建进货记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void createProcurement(ProcurementRequest request, String token) {
        // 1. 验证管理员身份
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证用户是否为管理员
        if (!"ADMIN".equals(user.getUserType())) {
            throw new BusinessException("只有管理员才能进行进货操作");
        }

        // 2. 查询商品
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 3. 计算总成本
        BigDecimal totalCost = request.getUnitCost().multiply(BigDecimal.valueOf(request.getQuantity()));

        // 4. 创建进货记录
        Procurement procurement = new Procurement();
        procurement.setProductId(request.getProductId());
        procurement.setQuantity(request.getQuantity());
        procurement.setUnitCost(request.getUnitCost());
        procurement.setTotalCost(totalCost);
        procurement.setSupplier(request.getSupplier());
        procurement.setProcurementTime(LocalDateTime.now());
        procurement.setOperatorId(userId);  // 直接使用 userId 作为操作员ID
        procurement.setNotes(request.getNotes());
        procurementMapper.insert(procurement);

        // 5. 更新商品库存
        product.setStockQuantity(product.getStockQuantity() + request.getQuantity());
        productMapper.updateById(product);

        log.info("进货记录创建成功: procurementId={}, productId={}, productName={}, quantity={}, totalCost={}, 更新后库存={}", 
                procurement.getProcurementId(), product.getProductId(), product.getProductName(), 
                request.getQuantity(), totalCost, product.getStockQuantity());
    }

    /**
     * 获取进货记录列表
     */
    public List<Procurement> getProcurementList() {
        LambdaQueryWrapper<Procurement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Procurement::getProcurementTime);
        return procurementMapper.selectList(wrapper);
    }

    /**
     * 分页获取进货记录列表
     */
    public Page<Procurement> getProcurementListPaged(int pageNum, int pageSize) {
        Page<Procurement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Procurement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Procurement::getProcurementTime);
        return procurementMapper.selectPage(page, wrapper);
    }
}
