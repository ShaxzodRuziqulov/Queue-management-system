package com.example.queuemanagementsystem;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Booking;
import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.BusinessHours;
import com.example.queuemanagementsystem.domain.Customer;
import com.example.queuemanagementsystem.domain.OfferedService;
import com.example.queuemanagementsystem.domain.Review;
import com.example.queuemanagementsystem.domain.StaffMember;
import com.example.queuemanagementsystem.domain.enums.Weekday;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.BookingRepository;
import com.example.queuemanagementsystem.repository.BusinessHoursRepository;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.repository.CustomerRepository;
import com.example.queuemanagementsystem.repository.OfferedServiceRepository;
import com.example.queuemanagementsystem.repository.ReviewRepository;
import com.example.queuemanagementsystem.repository.StaffMemberRepository;
import com.example.queuemanagementsystem.service.AppUserService;
import com.example.queuemanagementsystem.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class QueueManagementSystemApplicationTests {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BusinessHoursRepository businessHoursRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void businessAggregateSortsDoNotAppendVirtualPropertiesToQuery() {
        assertDoesNotThrow(() -> businessService.findPublic(
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "rating"))
        ));

        assertDoesNotThrow(() -> businessService.findPublic(
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "reviews"))
        ));

        assertDoesNotThrow(() -> businessService.findAll(
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "rating"))
        ));
    }

    @Test
    void deletingUserDeletesOwnedBusinessGraph() {
        AppUser owner = new AppUser();
        owner.setFirstName("Delete");
        owner.setUsername("delete-owner-" + System.nanoTime());
        owner.setActive(true);
        owner = appUserRepository.saveAndFlush(owner);

        Business business = new Business();
        business.setOwner(owner);
        business.setName("Delete Test Business");
        business = businessRepository.saveAndFlush(business);

        BusinessHours hours = new BusinessHours();
        hours.setBusiness(business);
        hours.setWeekday(Weekday.MONDAY);
        hours.setOpensAt(LocalTime.of(9, 0));
        hours.setClosesAt(LocalTime.of(18, 0));
        hours = businessHoursRepository.saveAndFlush(hours);

        OfferedService offeredService = new OfferedService();
        offeredService.setBusiness(business);
        offeredService.setName("Haircut");
        offeredService.setDurationMinutes(30);
        offeredService.setActive(true);
        offeredService = offeredServiceRepository.saveAndFlush(offeredService);

        StaffMember staff = new StaffMember();
        staff.setBusiness(business);
        staff.setFirstName("Staff");
        staff.setActive(true);
        staff.getOfferedServices().add(offeredService);
        staff = staffMemberRepository.saveAndFlush(staff);

        Customer customer = new Customer();
        customer.setBusiness(business);
        customer.setAppUser(owner);
        customer.setFirstName("Customer");
        customer.setActive(true);
        customer = customerRepository.saveAndFlush(customer);

        Booking booking = new Booking();
        booking.setBusiness(business);
        booking.setCustomer(customer);
        booking.setCustomerAccount(owner);
        booking.setOfferedService(offeredService);
        booking.setStaff(staff);
        booking.setStartAt(Instant.now());
        booking.setEndAt(Instant.now().plusSeconds(1800));
        booking = bookingRepository.saveAndFlush(booking);

        Review review = new Review();
        review.setBooking(booking);
        review.setStaff(staff);
        review.setStars(5);
        review = reviewRepository.saveAndFlush(review);

        AppUser finalOwner = owner;
        assertDoesNotThrow(() -> appUserService.delete(finalOwner.getId()));

        assertFalse(appUserRepository.existsById(owner.getId()));
        assertFalse(businessRepository.existsById(business.getId()));
        assertFalse(businessHoursRepository.existsById(hours.getId()));
        assertFalse(offeredServiceRepository.existsById(offeredService.getId()));
        assertFalse(staffMemberRepository.existsById(staff.getId()));
        assertFalse(customerRepository.existsById(customer.getId()));
        assertFalse(bookingRepository.existsById(booking.getId()));
        assertFalse(reviewRepository.existsById(review.getId()));
    }

}
