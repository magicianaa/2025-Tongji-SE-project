package com.esports.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.esports.hotel.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 预订Mapper
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
    
    /**
     * 查询当前有效的预订记录
     */
    @Select("SELECT * FROM tb_booking " +
            "WHERE room_id = #{roomId} " +
            "AND status IN ('PENDING', 'CONFIRMED') " +
            "AND DATE(planned_checkin) <= CURDATE() " +
            "AND DATE(planned_checkout) >= CURDATE() " +
            "ORDER BY planned_checkin ASC " +
            "LIMIT 1")
    Booking selectActiveBookingWithGuestName(Long roomId, LocalDate today);
    
    /**
     * 查询房间的当前有效预订（用于房态列表显示）
     */
    @Select("SELECT * FROM tb_booking " +
            "WHERE room_id = #{roomId} " +
            "AND status IN ('PENDING', 'CONFIRMED') " +
            "AND planned_checkin <= NOW() " +
            "AND planned_checkout >= NOW() " +
            "ORDER BY planned_checkin ASC " +
            "LIMIT 1")
    Booking selectCurrentBookingForRoom(Long roomId);
}
