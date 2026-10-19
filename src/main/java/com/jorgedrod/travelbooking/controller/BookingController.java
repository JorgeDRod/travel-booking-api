package com.jorgedrod.travelbooking.controller;

import com.jorgedrod.travelbooking.entity.Booking;
import com.jorgedrod.travelbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jorgedrod.travelbooking.mapper.BookingMapper;
import com.jorgedrod.travelbooking.dto.BookingRequest;
import com.jorgedrod.travelbooking.dto.BookingResponse;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    public BookingController(BookingService bookingService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable Long customerId,
            @Valid @RequestBody BookingRequest  bookingRequest) {
        Booking booking = bookingMapper.toEntity(bookingRequest);

        Booking createdBooking = bookingService.createBooking(customerId, booking);

        if (createdBooking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingMapper.toResponse(createdBooking));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getBookingsByCustomerId(@PathVariable Long customerId) {

        List<Booking> bookings = bookingService.findAllByCustomerId(customerId);
        if (bookings == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookings.stream().map(bookingMapper::toResponse).toList());
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingsByIdAndCustomerId(@PathVariable Long customerId, @PathVariable Long bookingId) {

        Booking booking = bookingService.getBookingById(customerId, bookingId);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long customerId, @PathVariable Long bookingId, @Valid @RequestBody BookingRequest bookingRequest) {

        Booking booking = bookingMapper.toEntity(bookingRequest);

        Booking updatedBookingResult = bookingService.updateBooking(customerId, bookingId, booking);
        if (updatedBookingResult == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookingMapper.toResponse(updatedBookingResult));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long customerId, @PathVariable Long bookingId) {
        boolean deleted = bookingService.deleteBooking(customerId, bookingId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
