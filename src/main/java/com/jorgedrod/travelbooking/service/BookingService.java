package com.jorgedrod.travelbooking.service;

import com.jorgedrod.travelbooking.entity.Booking;
import com.jorgedrod.travelbooking.entity.Customer;
import com.jorgedrod.travelbooking.exception.InvalidBookingDateException;
import com.jorgedrod.travelbooking.repository.BookingRepository;
import com.jorgedrod.travelbooking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    private void validateBookingDates(Booking booking) {

        if (booking.getEndDate().isBefore(booking.getStartDate())) {
            throw new InvalidBookingDateException(
                    "End date cannot be before start date."
            );
        }
    }

    public BookingService(BookingRepository bookingRepository, CustomerRepository customerRepository) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
    }

    public Booking createBooking(Long customerId, Booking booking){

        Customer existingCustomer = customerRepository.findById(customerId).orElse(null);

        if(existingCustomer == null){
            return null;
        }

        booking.setCustomer(existingCustomer);

        validateBookingDates(booking);

        return bookingRepository.save(booking);
    }

    public List<Booking> findAllByCustomerId(Long customerId){

        if (!customerRepository.existsById(customerId)) {
            return null;
        }
        return bookingRepository.findAllByCustomerId(customerId);
    }

    public Booking getBookingById(Long customerId, Long bookingId){
        return bookingRepository.findByIdAndCustomerId(bookingId, customerId).orElse(null);
    }

    public Booking updateBooking(Long customerId, Long bookingId, Booking updatedBooking){

        Booking existingBooking = getBookingById(customerId, bookingId);

        if (existingBooking == null){
            return null;
        }
        existingBooking.setStartDate(updatedBooking.getStartDate());
        existingBooking.setEndDate(updatedBooking.getEndDate());
        existingBooking.setLocation(updatedBooking.getLocation());

        validateBookingDates(existingBooking);

        return bookingRepository.save(existingBooking);
    }

    public boolean deleteBooking(Long customerId, Long bookingId){

        Booking existingBooking = getBookingById(customerId, bookingId);

        if (existingBooking == null){
            return false;
        }
        bookingRepository.delete(existingBooking);
        return true;
    }
}
