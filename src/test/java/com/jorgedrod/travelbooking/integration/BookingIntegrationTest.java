package com.jorgedrod.travelbooking.integration;

import com.jorgedrod.travelbooking.entity.Booking;
import com.jorgedrod.travelbooking.entity.Customer;
import com.jorgedrod.travelbooking.repository.BookingRepository;
import com.jorgedrod.travelbooking.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void applicationContextLoads() {
    }

    @Test
    void createCustomer_validRequest_returnsSuccess()  throws Exception {

        String customerJson = """
                {
                    "firstName": "Jorge",
                    "lastName": "Rodriguez",
                    "email": "jorge.integration@example.com",
                    "phone": "555-123-4567"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.firstName").value("Jorge"))
                .andExpect(jsonPath("$.email")
                        .value("jorge.integration@example.com"));

        List<Customer> customers = customerRepository.findAll();

        assertEquals(1, customers.size());
        assertEquals("Jorge", customers.get(0).getFirstName());
        assertEquals("jorge.integration@example.com", customers.get(0).getEmail());
    }

    @Test
    void createBooking_validRequest_savesBookingToDatabase()  throws Exception {

        Customer customer = new Customer();
        customer.setFirstName("Jorge");
        customer.setLastName("Rodriguez");
        customer.setEmail("jorge.integration@example.com");
        customer.setPhone("555-123-4567");

        Customer savedCustomer = customerRepository.save(customer);

        String bookingJson = """
                {
                    "startDate": "2027-06-01",
                    "endDate": "2027-06-10",
                    "location": "Rome"
                }
        """;

        mockMvc.perform(post("/api/customers/{customerId}/bookings", savedCustomer.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bookingJson))
                    .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.location").value("Rome"))
                .andExpect(jsonPath("$.startDate").value("2027-06-01"))
                .andExpect(jsonPath("$.endDate").value("2027-06-10"));


        List<Booking> bookings = bookingRepository.findAll();

        Booking savedBooking = bookings.get(0);

        assertEquals("Rome", savedBooking.getLocation());
        assertEquals(LocalDate.of(2027, 6, 1), savedBooking.getStartDate());
        assertEquals(savedCustomer.getId(), savedBooking.getCustomer().getId());
    }

    @Test
    void createBooking_invalidDates_doesNotSaveBooking()  throws Exception {

        Customer customer = new Customer();
        customer.setFirstName("Jorge");
        customer.setLastName("Rodriguez");
        customer.setEmail("jorge.integration@example.com");
        customer.setPhone("555-123-4567");

        Customer savedCustomer = customerRepository.save(customer);

        String bookingJson = """
                {
                    "startDate": "2027-06-10",
                    "endDate": "2027-06-01",
                    "location": "Rome"
                }
        """;

        mockMvc.perform(post("/api/customers/{customerId}/bookings", savedCustomer.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bookingJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("End date cannot be before start date."));

        assertEquals(0, bookingRepository.count());

    }
}
