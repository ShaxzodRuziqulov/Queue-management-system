package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.OfferedService;
import com.example.queuemanagementsystem.repository.BookingRepository;
import com.example.queuemanagementsystem.repository.BusinessHoursRepository;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.repository.CustomerRepository;
import com.example.queuemanagementsystem.repository.OfferedServiceRepository;
import com.example.queuemanagementsystem.repository.ReviewRepository;
import com.example.queuemanagementsystem.repository.StaffMemberRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessDeletionService {

    private final BusinessRepository businessRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final BusinessHoursRepository businessHoursRepository;
    private final CustomerRepository customerRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final FileStorageService fileStorageService;
    private final EntityManager entityManager;

    @Transactional
    public void delete(Business business) {
        deleteById(business.getId());
    }

    @Transactional
    public void deleteById(UUID businessId) {
        List<String> serviceImageUrls = offeredServiceRepository.findByBusiness_Id(businessId).stream()
                .map(OfferedService::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .toList();

        reviewRepository.deleteByBookingBusinessId(businessId);
        bookingRepository.deleteByBusinessId(businessId);
        staffMemberRepository.deleteServiceLinksByBusinessId(businessId);
        businessHoursRepository.deleteByBusinessId(businessId);
        customerRepository.deleteByBusinessId(businessId);
        staffMemberRepository.deleteByBusinessId(businessId);
        offeredServiceRepository.deleteByBusinessId(businessId);
        entityManager.flush();
        entityManager.clear();
        businessRepository.deleteByBusinessId(businessId);
        businessRepository.flush();

        serviceImageUrls.forEach(fileStorageService::delete);
    }
}
