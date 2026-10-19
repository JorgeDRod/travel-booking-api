package com.jorgedrod.travelbooking.controller;

import com.jorgedrod.travelbooking.entity.Booking;
import com.jorgedrod.travelbooking.exception.InvalidBookingDateException;
import com.jorgedrod.travelbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import com.jorgedrod.travelbooking.exception.GlobalExceptionHandler;
import com.jorgedrod.travelbooking.mapper.BookingMapper;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.MediaType;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        BookingMapper bookingMapper = new BookingMapper();

        BookingController bookingController =
                new BookingController(bookingService, bookingMapper);


        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getBooking_existingBooking_returnsOK() throws Exception{

        Booking  booking = new Booking();
        booking.setLocation("Rome");
        booking.setStartDate(LocalDate.of(2027,6,1));
        booking.setEndDate(LocalDate.of(2027,6,10));

        when(bookingService.getBookingById(2L, 5L))
                .thenReturn(booking);

        mockMvc.perform(get("/api/customers/2/bookings/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Rome"));
    }

    @Test
    void getBooking_bookingDoesNotExist_returnsNotFound() throws Exception{

        when(bookingService.getBookingById(2L, 999L))
                .thenReturn(null);

        mockMvc.perform(get("/api/customers/2/bookings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBookings_bookingExists_returnsOK() throws Exception{

        Booking booking = new Booking();
        booking.setLocation("Rome");
        Booking booking2 = new Booking();
        booking2.setLocation("Paris");

        when(bookingService.findAllByCustomerId(2L)).thenReturn(List.of(booking, booking2));

        mockMvc.perform(get("/api/customers/2/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location").value("Rome"))
                .andExpect(jsonPath("$[1].location").value("Paris"));
    }

    @Test
    void getAllBookings_customerHasNoBookings_returnsEmptyList() throws Exception{

        when(bookingService.findAllByCustomerId(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/customers/2/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllBookings_customerDoesNotExist_returnsNotFound() throws Exception{

        when(bookingService.findAllByCustomerId(2L)).thenReturn(null);

        mockMvc.perform(get("/api/customers/2/bookings"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_validBooking_returnsCreated() throws Exception{
        Booking booking = new Booking();

        booking.setStartDate(LocalDate.of(2027,6,1));
        booking.setEndDate(LocalDate.of(2027,6,10));
        booking.setLocation("Rome");

        when(bookingService.createBooking(eq(2L), any(Booking.class)))
                .thenReturn(booking);

        String bookingJson = """
                {
                    "startDate": "2027-06-01",
                    "endDate": "2027-06-10",
                    "location": "Rome"
                }
                """;

        mockMvc.perform(post("/api/customers/2/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.location").value("Rome"));

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingService).createBooking(eq(2L), bookingCaptor.capture());

        Booking capturedBooking = bookingCaptor.getValue();

        assertEquals(LocalDate.of(2027, 6, 1), capturedBooking.getStartDate());
        assertEquals(LocalDate.of(2027, 6, 10), capturedBooking.getEndDate());
        assertEquals("Rome", capturedBooking.getLocation());
    }

    @Test
    void createBooking_customerDoesNotExist_returnsNotFound() throws Exception{

        when(bookingService.createBooking(eq(999L), any(Booking.class)))
                .thenReturn(null);

        String bookingJson = """
                {
                    "startDate": "2027-06-01",
                    "endDate": "2027-06-10",
                    "location": "Rome"
                }
                """;

        mockMvc.perform(post("/api/customers/999/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_blankLocation_returnsBadRequest() throws Exception{

        String bookingJson = """
                {
                    "startDate": "2027-06-01",
                    "endDate": "2027-06-10",
                    "location": ""
                    }
        """;

        mockMvc.perform(post("/api/customers/2/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("location: Location is required"));

        verify(bookingService, never()).createBooking(anyLong(), any(Booking.class));
    }

    @Test
    void createBooking_endDateBeforeStartDate_returnsBadRequest() throws Exception{

        String bookingJson = """
                {
                    "startDate": "2027-06-10",
                    "endDate": "2027-06-01",
                    "location": "Rome"
                }
                """;

        when(bookingService.createBooking(eq(2L), any(Booking.class)))
                .thenThrow(new InvalidBookingDateException("End date cannot be before start date."));

        mockMvc.perform(post("/api/customers/2/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("End date cannot be before start date."));
    }

    @Test
    void updateBooking_existingBooking_returnsOk() throws Exception{
        Booking booking = new Booking();
        booking.setStartDate(LocalDate.of(2027,7,1));
        booking.setEndDate(LocalDate.of(2027,7,10));
        booking.setLocation("Paris");

        String bookingJson = """
                {
                    "startDate": "2027-07-01",
                    "endDate": "2027-07-10",
                    "location": "Paris"
                    }
        """;

        when(bookingService.updateBooking(eq(2L), eq(5L), any(Booking.class)))
                .thenReturn(booking);

        mockMvc.perform(put("/api/customers/2/bookings/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Paris"));
    }

    @Test
    void updateBooking_bookingDoesNotExist_returnsNotFound() throws Exception{

        String bookingJson = """
                {
                    "startDate": "2027-07-01",
                    "endDate": "2027-07-10",
                    "location": "Paris"
                    }
        """;

        when(bookingService.updateBooking(eq(2L), eq(999L), any(Booking.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/customers/2/bookings/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBooking_existingBooking_returnsNoContent() throws Exception{

        when(bookingService.deleteBooking(eq(2L), eq(5L)))
                .thenReturn(true);

        mockMvc.perform(delete("/api/customers/2/bookings/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBooking_bookingDoesNotExist_returnsNotFound() throws Exception{

        when(bookingService.deleteBooking(eq(2L), eq(999L)))
                .thenReturn(false);

        mockMvc.perform(delete("/api/customers/2/bookings/999"))
                .andExpect(status().isNotFound());
    }
}
