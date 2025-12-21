package com.esports.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.dto.*;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import com.esports.hotel.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final RoomMapper roomMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final PosOrderMapper posOrderMapper;
    private final MaintenanceTicketMapper maintenanceTicketMapper;
    private final AlertLogMapper alertLogMapper;
    private final GuestMapper guestMapper;
    private final PointsRedemptionMapper pointsRedemptionMapper;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 1. 房间统计
        Long totalRooms = roomMapper.selectCount(null);
        Long occupiedRooms = roomMapper.selectCount(
                new LambdaQueryWrapper<Room>().eq(Room::getStatus, "OCCUPIED")
        );
        stats.setTotalRooms(totalRooms.intValue());
        stats.setOccupiedRooms(occupiedRooms.intValue());

        // 2. 计算入住率
        if (totalRooms > 0) {
            BigDecimal occupancyRate = BigDecimal.valueOf(occupiedRooms)
                    .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            stats.setOccupancyRate(occupancyRate);
        } else {
            stats.setOccupancyRate(BigDecimal.ZERO);
        }

        // 3. 计算RevPAR（本月总客房收入 / 可用房间数）
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<CheckInRecord> monthlyCheckIns = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .ge(CheckInRecord::getActualCheckin, monthStart)
                        .eq(CheckInRecord::getPaymentStatus, "PAID")
        );
        
        BigDecimal totalRoomRevenue = monthlyCheckIns.stream()
                .map(CheckInRecord::getRoomFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalRooms > 0) {
            stats.setRevPAR(totalRoomRevenue.divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP));
        } else {
            stats.setRevPAR(BigDecimal.ZERO);
        }

        // 4. 今日入住/退房统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();

        Long todayCheckIns = checkInRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckInRecord>()
                        .ge(CheckInRecord::getActualCheckin, todayStart)
                        .lt(CheckInRecord::getActualCheckin, todayEnd)
        );
        stats.setTodayCheckIns(todayCheckIns.intValue());

        Long todayCheckOuts = checkInRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckInRecord>()
                        .ge(CheckInRecord::getActualCheckout, todayStart)
                        .lt(CheckInRecord::getActualCheckout, todayEnd)
        );
        stats.setTodayCheckOuts(todayCheckOuts.intValue());

        // 5. 待处理报警数
        Long pendingAlerts = alertLogMapper.selectCount(
                new LambdaQueryWrapper<AlertLog>()
                        .eq(AlertLog::getIsHandled, false)
        );
        stats.setPendingAlerts(pendingAlerts.intValue());

        // 6. 待处理维修工单数
        Long pendingTickets = maintenanceTicketMapper.selectCount(
                new LambdaQueryWrapper<MaintenanceTicket>()
                        .in(MaintenanceTicket::getStatus, Arrays.asList("OPEN", "IN_PROGRESS"))
        );
        stats.setPendingMaintenanceTickets(pendingTickets.intValue());

        // 7. 本月总收入（房费 + POS）
        BigDecimal monthlyPosRevenue = posOrderMapper.selectList(
                new LambdaQueryWrapper<PosOrder>()
                        .ge(PosOrder::getCreateTime, monthStart)
                        .eq(PosOrder::getStatus, "DELIVERED")
        ).stream()
                .map(PosOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setMonthlyRevenue(totalRoomRevenue.add(monthlyPosRevenue));

        // 8. 本月完成订单数
        Long monthlyOrders = posOrderMapper.selectCount(
                new LambdaQueryWrapper<PosOrder>()
                        .ge(PosOrder::getCreateTime, monthStart)
                        .eq(PosOrder::getStatus, "DELIVERED")
        );
        stats.setMonthlyOrders(monthlyOrders.intValue());

        // 9. 活跃会员数（近30天有入住记录）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<CheckInRecord> recentCheckIns = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .ge(CheckInRecord::getActualCheckin, thirtyDaysAgo)
        );
        Set<Long> activeGuestIds = recentCheckIns.stream()
                .map(CheckInRecord::getGuestId)
                .collect(Collectors.toSet());
        stats.setActiveMembers(activeGuestIds.size());

        return stats;
    }

    @Override
    public FinancialReportDTO getDailyReport(LocalDate date) {
        FinancialReportDTO report = new FinancialReportDTO();
        report.setReportDate(date);
        report.setReportType("DAILY");

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        // 1. 客房收入（当日退房并已结算）
        List<CheckInRecord> dailyCheckOuts = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .ge(CheckInRecord::getActualCheckout, dayStart)
                        .lt(CheckInRecord::getActualCheckout, dayEnd)
                        .eq(CheckInRecord::getPaymentStatus, "PAID")
        );

        BigDecimal roomRevenue = dailyCheckOuts.stream()
                .map(CheckInRecord::getRoomFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setRoomRevenue(roomRevenue);
        report.setCheckInCount(dailyCheckOuts.size());

        if (!dailyCheckOuts.isEmpty()) {
            report.setAvgRoomPrice(roomRevenue.divide(
                    BigDecimal.valueOf(dailyCheckOuts.size()), 2, RoundingMode.HALF_UP));
        } else {
            report.setAvgRoomPrice(BigDecimal.ZERO);
        }

        // 2. POS收入
        List<PosOrder> dailyPosOrders = posOrderMapper.selectList(
                new LambdaQueryWrapper<PosOrder>()
                        .ge(PosOrder::getCreateTime, dayStart)
                        .lt(PosOrder::getCreateTime, dayEnd)
                        .eq(PosOrder::getStatus, "DELIVERED")
        );

        BigDecimal posRevenue = dailyPosOrders.stream()
                .map(PosOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setPosRevenue(posRevenue);
        report.setPosOrderCount(dailyPosOrders.size());

        if (!dailyPosOrders.isEmpty()) {
            report.setAvgPosConsumption(posRevenue.divide(
                    BigDecimal.valueOf(dailyPosOrders.size()), 2, RoundingMode.HALF_UP));
        } else {
            report.setAvgPosConsumption(BigDecimal.ZERO);
        }

        // 3. 积分兑换收入（虚拟收入，用于统计）
        List<PointsRedemption> dailyRedemptions = pointsRedemptionMapper.selectList(
                new LambdaQueryWrapper<PointsRedemption>()
                        .ge(PointsRedemption::getRedemptionTime, dayStart)
                        .lt(PointsRedemption::getRedemptionTime, dayEnd)
                        .eq(PointsRedemption::getStatus, "COMPLETED")
        );

        BigDecimal pointsRevenue = dailyRedemptions.stream()
                .map(r -> BigDecimal.valueOf(r.getPointsCost()).multiply(BigDecimal.valueOf(0.01)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setPointsRevenue(pointsRevenue);

        // 4. 总收入
        report.setTotalRevenue(roomRevenue.add(posRevenue).add(pointsRevenue));

        // 5. 入住率和RevPAR
        Long totalRooms = roomMapper.selectCount(null);
        Long occupiedRooms = roomMapper.selectCount(
                new LambdaQueryWrapper<Room>().eq(Room::getStatus, "OCCUPIED")
        );

        if (totalRooms > 0) {
            BigDecimal occupancyRate = BigDecimal.valueOf(occupiedRooms)
                    .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            report.setOccupancyRate(occupancyRate);
            report.setRevPAR(roomRevenue.divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP));
        } else {
            report.setOccupancyRate(BigDecimal.ZERO);
            report.setRevPAR(BigDecimal.ZERO);
        }

        return report;
    }

    @Override
    public FinancialReportDTO getMonthlyReport(Integer year, Integer month) {
        FinancialReportDTO report = new FinancialReportDTO();
        YearMonth yearMonth = YearMonth.of(year, month);
        report.setReportDate(yearMonth.atDay(1));
        report.setReportType("MONTHLY");

        LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay();

        // 1. 客房收入
        List<CheckInRecord> monthlyCheckOuts = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .ge(CheckInRecord::getActualCheckin, monthStart)
                        .lt(CheckInRecord::getActualCheckin, monthEnd)
                        .eq(CheckInRecord::getPaymentStatus, "PAID")
        );

        BigDecimal roomRevenue = monthlyCheckOuts.stream()
                .map(CheckInRecord::getRoomFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setRoomRevenue(roomRevenue);
        report.setCheckInCount(monthlyCheckOuts.size());

        if (!monthlyCheckOuts.isEmpty()) {
            report.setAvgRoomPrice(roomRevenue.divide(
                    BigDecimal.valueOf(monthlyCheckOuts.size()), 2, RoundingMode.HALF_UP));
        } else {
            report.setAvgRoomPrice(BigDecimal.ZERO);
        }

        // 2. POS收入
        List<PosOrder> monthlyPosOrders = posOrderMapper.selectList(
                new LambdaQueryWrapper<PosOrder>()
                        .ge(PosOrder::getCreateTime, monthStart)
                        .lt(PosOrder::getCreateTime, monthEnd)
                        .eq(PosOrder::getStatus, "DELIVERED")
        );

        BigDecimal posRevenue = monthlyPosOrders.stream()
                .map(PosOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setPosRevenue(posRevenue);
        report.setPosOrderCount(monthlyPosOrders.size());

        if (!monthlyPosOrders.isEmpty()) {
            report.setAvgPosConsumption(posRevenue.divide(
                    BigDecimal.valueOf(monthlyPosOrders.size()), 2, RoundingMode.HALF_UP));
        } else {
            report.setAvgPosConsumption(BigDecimal.ZERO);
        }

        // 3. 积分兑换收入
        List<PointsRedemption> monthlyRedemptions = pointsRedemptionMapper.selectList(
                new LambdaQueryWrapper<PointsRedemption>()
                        .ge(PointsRedemption::getRedemptionTime, monthStart)
                        .lt(PointsRedemption::getRedemptionTime, monthEnd)
                        .eq(PointsRedemption::getStatus, "COMPLETED")
        );

        BigDecimal pointsRevenue = monthlyRedemptions.stream()
                .map(r -> BigDecimal.valueOf(r.getPointsCost()).multiply(BigDecimal.valueOf(0.01)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setPointsRevenue(pointsRevenue);

        // 4. 总收入
        report.setTotalRevenue(roomRevenue.add(posRevenue).add(pointsRevenue));

        // 5. 平均入住率和RevPAR
        Long totalRooms = roomMapper.selectCount(null);
        if (totalRooms > 0) {
            // 计算月平均入住率（简化版：使用当前入住率）
            Long occupiedRooms = roomMapper.selectCount(
                    new LambdaQueryWrapper<Room>().eq(Room::getStatus, "OCCUPIED")
            );
            BigDecimal occupancyRate = BigDecimal.valueOf(occupiedRooms)
                    .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            report.setOccupancyRate(occupancyRate);
            report.setRevPAR(roomRevenue.divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP));
        } else {
            report.setOccupancyRate(BigDecimal.ZERO);
            report.setRevPAR(BigDecimal.ZERO);
        }

        return report;
    }

    @Override
    public HardwareAnalysisDTO getHardwareAnalysis(Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }

        HardwareAnalysisDTO analysis = new HardwareAnalysisDTO();
        analysis.setAnalysisDays(days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        // 1. 查询时间范围内的所有维修工单
        List<MaintenanceTicket> tickets = maintenanceTicketMapper.selectList(
                new LambdaQueryWrapper<MaintenanceTicket>()
                        .ge(MaintenanceTicket::getCreateTime, startDate)
                        .eq(MaintenanceTicket::getRequestType, "REPAIR")
        );

        analysis.setTotalMaintenanceTickets(tickets.size());

        if (tickets.isEmpty()) {
            analysis.setAnalysisItems(new ArrayList<>());
            analysis.setTopFailureDevices(new ArrayList<>());
            analysis.setPurchaseRecommendations(new ArrayList<>());
            return analysis;
        }

        // 2. 分析设备故障统计（从description字段中提取设备类型）
        Map<String, List<MaintenanceTicket>> deviceGroupMap = new HashMap<>();
        
        for (MaintenanceTicket ticket : tickets) {
            String description = ticket.getDescription();
            if (description == null) continue;

            String deviceType = extractDeviceType(description);
            deviceGroupMap.computeIfAbsent(deviceType, k -> new ArrayList<>()).add(ticket);
        }

        // 3. 生成分析项
        List<HardwareAnalysisItemDTO> analysisItems = new ArrayList<>();
        Long totalRooms = roomMapper.selectCount(null);

        for (Map.Entry<String, List<MaintenanceTicket>> entry : deviceGroupMap.entrySet()) {
            String deviceType = entry.getKey();
            List<MaintenanceTicket> deviceTickets = entry.getValue();

            HardwareAnalysisItemDTO item = new HardwareAnalysisItemDTO();
            item.setDeviceType(deviceType);
            item.setBrandModel(extractBrandModel(deviceTickets.get(0).getDescription()));
            item.setFailureCount(deviceTickets.size());

            // 统计涉及房间数
            Set<Long> affectedRooms = deviceTickets.stream()
                    .map(MaintenanceTicket::getRoomId)
                    .collect(Collectors.toSet());
            item.setAffectedRoomCount(affectedRooms.size());

            // 计算故障率（假设每个房间都有该设备）
            if (totalRooms > 0) {
                double failureRate = (double) deviceTickets.size() / totalRooms;
                item.setFailureRate(failureRate);
            } else {
                item.setFailureRate(0.0);
            }

            // 计算平均修复时间
            double avgRepairTime = deviceTickets.stream()
                    .filter(t -> t.getResolveTime() != null && t.getCreateTime() != null)
                    .mapToDouble(t -> java.time.Duration.between(
                            t.getCreateTime(), t.getResolveTime()).toHours())
                    .average()
                    .orElse(0.0);
            item.setAvgRepairTime(avgRepairTime);

            // 采购建议逻辑
            if (deviceTickets.size() >= 5) {
                item.setRecommendedPurchaseQty((int) Math.ceil(deviceTickets.size() * 0.3));
                item.setRecommendationReason("高频故障，建议备货" + item.getRecommendedPurchaseQty() + "件");
            } else if (deviceTickets.size() >= 3) {
                item.setRecommendedPurchaseQty(2);
                item.setRecommendationReason("中频故障，建议少量备货");
            } else {
                item.setRecommendedPurchaseQty(0);
                item.setRecommendationReason("故障率正常");
            }

            analysisItems.add(item);
        }

        // 按故障次数排序
        analysisItems.sort((a, b) -> b.getFailureCount().compareTo(a.getFailureCount()));
        analysis.setAnalysisItems(analysisItems);

        // 4. TOP3高频故障设备
        List<HardwareAnalysisItemDTO> topFailures = analysisItems.stream()
                .limit(3)
                .collect(Collectors.toList());
        analysis.setTopFailureDevices(topFailures);

        // 5. 采购建议清单（筛选需要采购的）
        List<HardwareAnalysisItemDTO> recommendations = analysisItems.stream()
                .filter(item -> item.getRecommendedPurchaseQty() > 0)
                .collect(Collectors.toList());
        analysis.setPurchaseRecommendations(recommendations);

        return analysis;
    }

    @Override
    public byte[] exportFinancialReportExcel(String reportType, LocalDate date) {
        // 简化实现：返回CSV格式数据（添加UTF-8 BOM以支持Excel正确识别中文）
        StringBuilder csv = new StringBuilder();
        
        // 添加UTF-8 BOM标记，确保Excel正确识别中文编码
        csv.append("\uFEFF");
        
        FinancialReportDTO report;
        if ("MONTHLY".equals(reportType)) {
            report = getMonthlyReport(date.getYear(), date.getMonthValue());
            csv.append("月度财务报表\n");
        } else {
            report = getDailyReport(date);
            csv.append("日度财务报表\n");
        }

        csv.append("报表日期,").append(report.getReportDate()).append("\n");
        csv.append("客房收入,").append(report.getRoomRevenue()).append("\n");
        csv.append("POS收入,").append(report.getPosRevenue()).append("\n");
        csv.append("积分收入,").append(report.getPointsRevenue()).append("\n");
        csv.append("总收入,").append(report.getTotalRevenue()).append("\n");
        csv.append("入住订单数,").append(report.getCheckInCount()).append("\n");
        csv.append("POS订单数,").append(report.getPosOrderCount()).append("\n");
        csv.append("平均客单价,").append(report.getAvgRoomPrice()).append("\n");
        csv.append("平均POS消费,").append(report.getAvgPosConsumption()).append("\n");
        csv.append("入住率(%),").append(report.getOccupancyRate()).append("\n");
        csv.append("RevPAR,").append(report.getRevPAR()).append("\n");

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 从描述中提取设备类型
     */
    private String extractDeviceType(String description) {
        if (description == null) return "未知设备";
        
        String desc = description.toLowerCase();
        if (desc.contains("鼠标") || desc.contains("mouse")) return "鼠标";
        if (desc.contains("键盘") || desc.contains("keyboard")) return "键盘";
        if (desc.contains("显示器") || desc.contains("monitor") || desc.contains("屏幕")) return "显示器";
        if (desc.contains("耳机") || desc.contains("headset")) return "耳机";
        if (desc.contains("椅子") || desc.contains("chair")) return "电竞椅";
        if (desc.contains("主机") || desc.contains("电脑") || desc.contains("pc")) return "主机";
        
        return "其他设备";
    }

    /**
     * 从描述中提取品牌型号
     */
    private String extractBrandModel(String description) {
        if (description == null) return "未知品牌";
        
        // 简化实现：提取描述中的第一个词作为品牌
        String[] words = description.split("\\s+");
        if (words.length > 0) {
            return words[0];
        }
        
        return "未知品牌";
    }
}
