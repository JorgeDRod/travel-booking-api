package com.jorgedrod.travelbooking.service;

import com.jorgedrod.travelbooking.entity.Booking;
import com.jorgedrod.travelbooking.entity.Customer;
import com.jorgedrod.travelbooking.exception.InvalidBookingDateException;
import com.jorgedrod.travelbooking.repository.BookingRepository;
import com.jorgedrod.travelbooking.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_customerDoesNotExist_returnsNull() {

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());

        Booking booking = new Booking();
        Booking result = bookingService.createBooking(999L, booking);

        assertNull(result);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_validCustomer_savesBooking() {

        Customer customer = new Customer();
        Booking booking = new Booking();

        booking.setStartDate(LocalDate.of(2027, 6, 1));
        booking.setEndDate(LocalDate.of(2027, 6, 10));
        booking.setLocation("Rome");

        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));

        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking result = bookingService.createBooking(2L, booking);

        assertNotNull(result);
        assertSame(booking, result);
        assertSame(customer, result.getCustomer());
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBooking_endDateBeforeStartDate_throwsException() {

        Customer customer = new Customer();

        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));

        Booking booking = new Booking();

        booking.setStartDate(LocalDate.of(2027, 6, 10));
        booking.setEndDate(LocalDate.of(2027, 6, 1));
        booking.setLocation("Rome");

        assertThrows(InvalidBookingDateException.class, () -> {
            bookingService.createBooking(2L, booking);
        });
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_existingBooking_updatesAndSaves() {

        Booking existingBooking = new Booking();

        existingBooking.setStartDate(LocalDate.of(2027, 6, 1));
        existingBooking.setEndDate(LocalDate.of(2027, 6, 10));
        existingBooking.setLocation("Rome");

        Booking updatedBooking = new Booking();

        updatedBooking.setStartDate(LocalDate.of(2027, 7, 1));
        updatedBooking.setEndDate(LocalDate.of(2027, 7, 15));
        updatedBooking.setLocation("Paris");

        when(bookingRepository.findByIdAndCustomerId(5L, 2L)).thenReturn(Optional.of(existingBooking));

        when(bookingRepository.save(existingBooking)).thenReturn(existingBooking);

        Booking result = bookingService.updateBooking(2L, 5L, updatedBooking);

        assertNotNull(result);
        assertEquals("Paris", result.getLocation());
        assertEquals(LocalDate.of(2027, 7, 1), result.getStartDate());
        assertEquals(LocalDate.of(2027, 7, 15), result.getEndDate());
        assertSame(existingBooking, result);
        verify(bookingRepository).save(existingBooking);
    }

    @Test
    void updateBooking_bookingDoesNotExist_returnsNull() {

        when(bookingRepository.findByIdAndCustomerId(999L, 2L)).thenReturn(Optional.empty());

        Booking updatedBooking = new Booking();

        Booking result = bookingService.updateBooking(2L, 999L, updatedBooking);

        assertNull(result);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_endDateBeforeStartDate_throwsException() {

        Booking existingBooking = new Booking();

        existingBooking.setStartDate(LocalDate.of(2027, 6, 1));
        existingBooking.setEndDate(LocalDate.of(2027, 6, 10));
        existingBooking.setLocation("Rome");

        when(bookingRepository.findByIdAndCustomerId(5L, 2L)).thenReturn(Optional.of(existingBooking));

        Booking updatedBooking = new Booking();

        updatedBooking.setStartDate(LocalDate.of(2027, 7, 15));
        updatedBooking.setEndDate(LocalDate.of(2027, 7, 1));
        updatedBooking.setLocation("Paris");

        assertThrows(InvalidBookingDateException.class, () -> {
            bookingService.updateBooking(2L, 5L, updatedBooking);
        });
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void deleteBooking_existingBooking_deletesAndReturnsTrue() {
        Booking existingBooking = new Booking();

        when(bookingRepository.findByIdAndCustomerId(5L, 2L)).thenReturn(Optional.of(existingBooking));

        boolean result = bookingService.deleteBooking(2L, 5L);

        assertTrue(result);
        verify(bookingRepository).delete(existingBooking);
    }

    @Test
    void deleteBooking_bookingDoesNotExist_returnsFalse() {

        when(bookingRepository.findByIdAndCustomerId(999L, 2L)).thenReturn(Optional.empty());
        boolean result = bookingService.deleteBooking(2L, 999L);
        assertFalse(result);
        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    @Test
    void getBookingById_existingBooking_returnsBooking() {
        Booking existingBooking = new Booking();
        existingBooking.setStartDate(LocalDate.of(2027, 6, 1));
        existingBooking.setEndDate(LocalDate.of(2027, 6, 10));
        existingBooking.setLocation("Rome");

        when(bookingRepository.findByIdAndCustomerId(5L, 2L)).thenReturn(Optional.of(existingBooking));

        Booking getBooking = bookingService.getBookingById(2L, 5L);

        assertSame(existingBooking, getBooking);
    }

    @Test
    void getBookingById_bookingDoesNotExist_returnsNull() {

        when(bookingRepository.findByIdAndCustomerId(999L, 2L)).thenReturn(Optional.empty());

        Booking getBooking = bookingService.getBookingById(2L, 999L);

        assertNull(getBooking);
    }

    @Test
    void findAllByCustomerId_customerHasBookings_returnsBookings() {

        when(customerRepository.existsById(2L)).thenReturn(true);

        Booking booking1 = new Booking();
        booking1.setLocation("Rome");
        Booking booking2 = new Booking();
        booking2.setLocation("Paris");

        when(bookingRepository.findAllByCustomerId(2L)).thenReturn(List.of(booking1, booking2));
        List<Booking> bookings = bookingService.findAllByCustomerId(2L);

        assertEquals(2, bookings.size());
        assertSame(booking1, bookings.get(0));
        assertSame(booking2, bookings.get(1));
    }

    @Test
    void findAllByCustomerId_customerHasNoBookings_returnsEmptyList() {

        when(customerRepository.existsById(2L)).thenReturn(true);

        when(bookingRepository.findAllByCustomerId(2L)).thenReturn(List.of());

        List<Booking> bookings = bookingService.findAllByCustomerId(2L);

        assertNotNull(bookings);
        assertTrue(bookings.isEmpty());
    }

    @Test
    void findAllByCustomerId_customerDoesNotExist_returnsNull() {

        when(customerRepository.existsById(999L)).thenReturn(false);
        List<Booking> bookings = bookingService.findAllByCustomerId(999L);
        assertNull(bookings);
        verify(bookingRepository, never()).findAllByCustomerId(999L);
    }
}
