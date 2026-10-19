package com.jorgedrod.travelbooking.mapper;

import com.jorgedrod.travelbooking.dto.BookingRequest;
import com.jorgedrod.travelbooking.dto.BookingResponse;
import com.jorgedrod.travelbooking.entity.Booking;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BookingMapper {

    public Booking toEntity(BookingRequest bookingRequest) {
        Booking booking = new Booking();

        booking.setStartDate(bookingRequest.getStartDate());
        booking.setEndDate(bookingRequest.getEndDate());
        booking.setLocation(bookingRequest.getLocation());

        return booking;
    }

    public BookingResponse toResponse(Booking booking) {
        BookingResponse bookingResponse = new BookingResponse();

        bookingResponse.setId(booking.getId());
        bookingResponse.setStartDate(booking.getStartDate());
        bookingResponse.setEndDate(booking.getEndDate());
        bookingResponse.setLocation(booking.getLocation());

        return bookingResponse;
    }
}
