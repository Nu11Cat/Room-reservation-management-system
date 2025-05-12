package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.Booking;
import cn.nullcat.sckj.pojo.PageBean;

import java.time.LocalDate;

public interface BookingsService {
    PageBean getBookings(Integer page, Integer pageSize, Integer roomId, Integer userId, Integer status, LocalDate begin, LocalDate end);

    Booking getById(Integer id);

    void addBooking(Booking booking);

    void updateBooking(Booking booking);

    void cancelBooking(Integer id);
}
