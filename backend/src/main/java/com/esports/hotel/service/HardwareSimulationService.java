package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.config.HardwareSimulatorProperties;
import com.esports.hotel.dto.HardwareTelemetryDTO;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 硬件模拟服务
 * 
 * 核心功能：
 * 1. 为每个房间生成随机的硬件遥测数据（正态分布）
 * 2. 判断健康等级（GREEN/YELLOW/RED）
 * 3. 检测异常并自动生成报警和维修工单
 * 4. 通过 WebSocket 实时推送数据到前端
 * 5. 定期归档数据到日志表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HardwareSimulationService {

    private final HardwareSimulatorProperties config;
    private final RoomMapper roomMapper;
    private final HardwareStatusMapper hardwareStatusMapper;
    private final DeviceLogMapper deviceLogMapper;
    private final AlertLogMapper alertLogMapper;
    private final MaintenanceTicketMapper maintenanceTicketMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private final Random random = new Random();
    
    // 存储每个房间的连续异常计数（用于判断是否触发报警）
    private final Map<Long, Integer> abnormalCountMap = new ConcurrentHashMap<>();

    /**
     * 初始化：为所有房间创建初始硬件状态记录
     */
    @PostConstruct
    public void initializeHardwareStatus() {
        if (!config.getEnabled()) {
            log.info("硬件模拟器已禁用");
            return;
        }

        List<Room> rooms = roomMapper.selectList(null);
        for (Room room : rooms) {
            LambdaQueryWrapper<HardwareStatus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HardwareStatus::getRoomId, room.getRoomId());
            
            if (hardwareStatusMapper.selectCount(wrapper) == 0) {
                HardwareStatus status = new HardwareStatus();
                status.setRoomId(room.getRoomId());
                status.setCpuTemp(50.0f);
                status.setGpuTemp(50.0f);
                status.setNetworkLatency(20);
                status.setPeripheralStatus("{\"keyboard\":true,\"mouse\":true,\"headset\":true}");
                status.setHealthLevel("GREEN");
                status.setLastUpdate(LocalDateTime.now());
                hardwareStatusMapper.insert(status);
            }
        }
        log.info("硬件模拟器初始化完成，共 {} 个房间", rooms.size());
    }

    /**
     * 定时任务：每隔N秒生成一次数据
     */
    @Scheduled(fixedDelayString = "${hardware.simulator.interval:5}000")
    @Transactional(rollbackFor = Exception.class)
    public void generateTelemetryData() {
        if (!config.getEnabled()) {
            return;
        }

        List<Room> rooms = roomMapper.selectList(null);
        List<HardwareTelemetryDTO> telemetryList = new ArrayList<>();

        for (Room room : rooms) {
            HardwareTelemetryDTO telemetry = generateSingleRoomData(room);
            telemetryList.add(telemetry);
            
            // 更新数据库
            updateHardwareStatus(telemetry);
            
            // 检测异常并触发报警
            checkAndHandleAlert(telemetry);
        }

        // 通过 WebSocket 广播所有房间数据
        messagingTemplate.convertAndSend("/topic/hardware", telemetryList);
        
        log.debug("硬件数据生成完成，共 {} 个房间", rooms.size());
    }

    /**
     * 为单个房间生成随机数据
     */
    private HardwareTelemetryDTO generateSingleRoomData(Room room) {
        HardwareTelemetryDTO telemetry = new HardwareTelemetryDTO();
        telemetry.setRoomId(room.getRoomId());
        telemetry.setRoomNo(room.getRoomNo());
        telemetry.setTimestamp(LocalDateTime.now());

        // 1. 生成 CPU 温度（正态分布，均值70(现在是50)°C，标准差10）
        float cpuTemp = generateNormalDistribution(50.0f, 10.0f, config.getTempMin(), config.getTempMax());
        
        // 2. 生成 GPU 温度（正态分布，均值75(现在是55)°C，标准差12）
        float gpuTemp = generateNormalDistribution(55.0f, 12.0f, config.getTempMin(), config.getTempMax());
        
        // 3. 模拟故障：按配置的概率生成过热数据
        if (random.nextFloat() < config.getFailureRate()) {
            cpuTemp = config.getTempMax() + random.nextFloat() * 5; // 95-100°C
            gpuTemp = config.getTempMax() + random.nextFloat() * 5;
            log.warn("模拟故障触发: 房间 {} 温度异常 CPU={}°C, GPU={}°C", room.getRoomNo(), cpuTemp, gpuTemp);
        }

        telemetry.setCpuTemp(cpuTemp);
        telemetry.setGpuTemp(gpuTemp);

        // 4. 生成网络延迟（均匀分布）
        int latency = config.getLatencyMin() + random.nextInt(config.getLatencyMax() - config.getLatencyMin());
        telemetry.setNetworkLatency(latency);

        // 5. 外设状态（偶尔模拟断开）
        HardwareTelemetryDTO.PeripheralStatus peripheralStatus = new HardwareTelemetryDTO.PeripheralStatus();
        peripheralStatus.setKeyboard(random.nextFloat() > 0.02); // 2%概率断开
        peripheralStatus.setMouse(random.nextFloat() > 0.02);
        peripheralStatus.setHeadset(random.nextFloat() > 0.05);
        telemetry.setPeripheralStatus(peripheralStatus);

        // 6. 计算健康等级
        String healthLevel = calculateHealthLevel(cpuTemp, gpuTemp, latency);
        telemetry.setHealthLevel(healthLevel);

        return telemetry;
    }

    /**
     * 正态分布随机数生成（Box-Muller 变换）
     */
    private float generateNormalDistribution(float mean, float stdDev, float min, float max) {
        float value = (float) (mean + stdDev * random.nextGaussian());
        return Math.max(min, Math.min(max, value)); // 限制范围
    }

    /**
     * 计算健康等级
     */
    private String calculateHealthLevel(float cpuTemp, float gpuTemp, int latency) {
        float maxTemp = Math.max(cpuTemp, gpuTemp);
        
        if (maxTemp >= config.getTempAlert() || latency > 150) {
            return "RED";    // 严重：温度>=95°C 或延迟>150ms
        } else if (maxTemp >= 85 || latency > 100) {
            return "YELLOW"; // 预警：温度>=85°C 或延迟>100ms
        } else {
            return "GREEN";  // 正常
        }
    }

    /**
     * 更新硬件状态表（实时数据，UPDATE操作）
     */
    private void updateHardwareStatus(HardwareTelemetryDTO telemetry) {
        LambdaQueryWrapper<HardwareStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HardwareStatus::getRoomId, telemetry.getRoomId());
        HardwareStatus status = hardwareStatusMapper.selectOne(wrapper);

        if (status == null) {
            status = new HardwareStatus();
            status.setRoomId(telemetry.getRoomId());
        }

        status.setCpuTemp(telemetry.getCpuTemp());
        status.setGpuTemp(telemetry.getGpuTemp());
        status.setNetworkLatency(telemetry.getNetworkLatency());
        
        try {
            status.setPeripheralStatus(objectMapper.writeValueAsString(telemetry.getPeripheralStatus()));
        } catch (JsonProcessingException e) {
            log.error("外设状态序列化失败", e);
        }
        
        status.setHealthLevel(telemetry.getHealthLevel());
        status.setLastUpdate(LocalDateTime.now());

        if (status.getStatusId() == null) {
            hardwareStatusMapper.insert(status);
        } else {
            hardwareStatusMapper.updateById(status);
        }
    }

    /**
     * 检测异常并处理报警逻辑
     * 规则：连续3次RED状态触发报警
     */
    private void checkAndHandleAlert(HardwareTelemetryDTO telemetry) {
        Long roomId = telemetry.getRoomId();
        String healthLevel = telemetry.getHealthLevel();

        if ("RED".equals(healthLevel)) {
            int count = abnormalCountMap.getOrDefault(roomId, 0) + 1;
            abnormalCountMap.put(roomId, count);

            // 连续3次异常才触发报警（防止误报）
            if (count >= 3) {
                triggerAlert(telemetry);
                abnormalCountMap.put(roomId, 0); // 重置计数
            }
        } else {
            abnormalCountMap.put(roomId, 0); // 恢复正常，重置计数
        }
    }

    /**
     * 触发报警：记录日志 + 生成维修工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void triggerAlert(HardwareTelemetryDTO telemetry) {
        // 1. 记录报警日志
        AlertLog alertLog = new AlertLog();
        alertLog.setRoomId(telemetry.getRoomId());
        alertLog.setAlertType("OVERHEAT");
        alertLog.setAlertLevel("CRITICAL");
        alertLog.setTriggerValue(String.format("CPU: %.1f°C, GPU: %.1f°C", 
                telemetry.getCpuTemp(), telemetry.getGpuTemp()));
        alertLog.setIsHandled(false);
        alertLogMapper.insert(alertLog);

        // 2. 自动生成维修工单
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setRoomId(telemetry.getRoomId());
        ticket.setReporterId(null); // 系统自动生成
        ticket.setRequestType("REPAIR");
        ticket.setDescription(String.format("系统自动检测：房间 %s 温度过高（CPU: %.1f°C, GPU: %.1f°C），请尽快检查散热系统", 
                telemetry.getRoomNo(), telemetry.getCpuTemp(), telemetry.getGpuTemp()));
        ticket.setPriority("URGENT");
        ticket.setStatus("OPEN");
        ticket.setCost(BigDecimal.ZERO);
        maintenanceTicketMapper.insert(ticket);

        // 3. 更新房态为"维修中"（可选，根据业务需求）
        Room room = roomMapper.selectById(telemetry.getRoomId());
        if ("VACANT".equals(room.getStatus())) {
            room.setStatus("MAINTENANCE");
            roomMapper.updateById(room);
        }

        log.warn("🚨 触发报警！房间: {}, 工单ID: {}, CPU: {}°C, GPU: {}°C", 
                telemetry.getRoomNo(), ticket.getTicketId(), telemetry.getCpuTemp(), telemetry.getGpuTemp());

        // 4. 通过 WebSocket 推送报警通知到管理端
        Map<String, Object> alertNotification = new HashMap<>();
        alertNotification.put("type", "ALERT");
        alertNotification.put("roomNo", telemetry.getRoomNo());
        alertNotification.put("roomId", telemetry.getRoomId());
        alertNotification.put("message", ticket.getDescription());
        alertNotification.put("ticketId", ticket.getTicketId());
        alertNotification.put("timestamp", LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/alerts", alertNotification);
    }

    /**
     * 定时归档日志（每分钟执行一次，保存到 tb_device_log）
     */
    @Scheduled(fixedRate = 60000) // 60秒
    @Transactional(rollbackFor = Exception.class)
    public void archiveDeviceLogs() {
        if (!config.getEnabled()) {
            return;
        }

        List<HardwareStatus> statusList = hardwareStatusMapper.selectList(null);
        for (HardwareStatus status : statusList) {
            DeviceLog log = new DeviceLog();
            log.setRoomId(status.getRoomId());
            log.setCpuTemp(status.getCpuTemp());
            log.setGpuTemp(status.getGpuTemp());
            log.setNetworkLatency(status.getNetworkLatency());
            deviceLogMapper.insert(log);
        }
        
        log.debug("设备日志归档完成，共 {} 条记录", statusList.size());
    }

    /**
     * 手动触发指定房间的故障模拟（用于演示）
     */
    public void triggerManualFailure(Long roomId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            return;
        }

        HardwareTelemetryDTO telemetry = new HardwareTelemetryDTO();
        telemetry.setRoomId(roomId);
        telemetry.setRoomNo(room.getRoomNo());
        telemetry.setCpuTemp(98.0f);
        telemetry.setGpuTemp(99.0f);
        telemetry.setNetworkLatency(200);
        telemetry.setHealthLevel("RED");
        telemetry.setTimestamp(LocalDateTime.now());

        updateHardwareStatus(telemetry);
        triggerAlert(telemetry);

        log.info("手动触发故障模拟：房间 {}", room.getRoomNo());
    }

    /**
     * 获取所有房间的当前硬件状态
     */
    public List<HardwareTelemetryDTO> getAllRoomStatus() {
        List<HardwareStatus> statusList = hardwareStatusMapper.selectList(null);
        List<HardwareTelemetryDTO> result = new ArrayList<>();

        for (HardwareStatus status : statusList) {
            Room room = roomMapper.selectById(status.getRoomId());
            if (room == null) continue;

            HardwareTelemetryDTO dto = new HardwareTelemetryDTO();
            dto.setRoomId(status.getRoomId());
            dto.setRoomNo(room.getRoomNo());
            dto.setCpuTemp(status.getCpuTemp());
            dto.setGpuTemp(status.getGpuTemp());
            dto.setNetworkLatency(status.getNetworkLatency());
            dto.setHealthLevel(status.getHealthLevel());
            dto.setTimestamp(status.getLastUpdate());

            try {
                HardwareTelemetryDTO.PeripheralStatus peripheralStatus = 
                        objectMapper.readValue(status.getPeripheralStatus(), HardwareTelemetryDTO.PeripheralStatus.class);
                dto.setPeripheralStatus(peripheralStatus);
            } catch (JsonProcessingException e) {
                log.error("外设状态反序列化失败", e);
            }

            result.add(dto);
        }

        return result;
    }
}
