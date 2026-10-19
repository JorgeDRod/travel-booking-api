package com.jorgedrod.travelbooking.repository;

import com.jorgedrod.travelbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByCustomerId(Long customerId);

    Optional<Booking> findByIdAndCustomerId(Long bookingId, Long customerId);
}
