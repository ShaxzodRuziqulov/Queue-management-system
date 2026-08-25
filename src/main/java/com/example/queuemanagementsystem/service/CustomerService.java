package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.Customer;
import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.dto.CustomerCreateRequest;
import com.example.queuemanagementsystem.dto.CustomerDto;
import com.example.queuemanagementsystem.dto.CustomerUpdateRequest;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import com.example.queuemanagementsystem.mapper.CustomerMapper;
import com.example.queuemanagementsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final BusinessService businessService;

    @Transactional(readOnly = true)
    public Page<CustomerDto> findAll(UUID businessId, String search, Pageable pageable) {
        businessService.requireManagerOrAdmin(businessId);
        Page<Customer> page = StringUtils.hasText(search)
                ? repository.search(businessId, search.trim().toLowerCase(Locale.ROOT), pageable)
                : repository.findByBusiness_Id(businessId, pageable);
        return page.map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public CustomerDto get(UUID businessId, UUID customerId) {
        businessService.requireManagerOrAdmin(businessId);
        return mapper.toDto(requireCustomer(businessId, customerId));
    }

    public CustomerDto create(UUID businessId, CustomerCreateRequest request) {
        businessService.requireManagerOrAdmin(businessId);
        Business business = businessService.requireActiveAccess(businessId);
        if (StringUtils.hasText(request.getPhone())) {
            String phone = request.getPhone().trim();
            if (repository.findByBusiness_IdAndPhone(businessId, phone).isPresent()) {
                throw new IllegalArgumentException("Bu telefon raqamli mijoz allaqachon mavjud");
            }
        }
        Customer entity = mapper.toEntity(request);
        entity.setBusiness(business);
        normalizeName(entity);
        normalizePhone(entity);
        return mapper.toDto(repository.save(entity));
    }

    public CustomerDto update(UUID businessId, UUID customerId, CustomerUpdateRequest request) {
        businessService.requireManagerOrAdmin(businessId);
        Customer entity = requireCustomer(businessId, customerId);
        if (request.getFirstName() != null && !StringUtils.hasText(request.getFirstName())) {
            throw new IllegalArgumentException("Ism kiritilishi shart");
        }
        // Telefon o'zgarayotgan bo'lsa — boshqa mijozda band emasligini tekshiramiz
        if (StringUtils.hasText(request.getPhone())) {
            String phone = request.getPhone().trim();
            repository.findByBusiness_IdAndPhone(businessId, phone).ifPresent(existing -> {
                if (!existing.getId().equals(customerId)) {
                    throw new IllegalArgumentException("Bu telefon raqamli mijoz allaqachon mavjud");
                }
            });
        }
        mapper.update(entity, request);
        normalizeName(entity);
        normalizePhone(entity);
        return mapper.toDto(entity);
    }

    public void delete(UUID businessId, UUID customerId) {
        businessService.requireManagerOrAdmin(businessId);
        Customer entity = requireCustomer(businessId, customerId);
        repository.delete(entity);
    }

    /**
     * Booking yaratilganda biznes mijozlar bazasini to'ldiradi.
     * Avval account bo'yicha, keyin telefon bo'yicha qidiradi. Topilsa shu yozuvga ulaydi,
     * topilmasa yangi Customer yaratadi. Telefon keyinroq ro'yxatdan o'tgan accountni
     * eski customer tarixi bilan bog'lash uchun asosiy dedup kalit.
     */
    public Customer upsertFromBooking(
            Business business,
            String firstName,
            String lastName,
            String middleName,
            String phone,
            AppUser appUser
    ) {
        if (appUser == null && !StringUtils.hasText(firstName) && !StringUtils.hasText(phone)) {
            return null;
        }
        String normalizedPhone = StringUtils.hasText(phone) ? phone.trim() : null;
        Customer customer = appUser == null
                ? null
                : repository.findByBusiness_IdAndAppUser_Id(business.getId(), appUser.getId()).orElse(null);
        if (customer == null && StringUtils.hasText(normalizedPhone)) {
            customer = repository.findByBusiness_IdAndPhone(business.getId(), normalizedPhone).orElse(null);
        }
        if (customer == null) {
            customer = new Customer();
            customer.setBusiness(business);
        }
        if (appUser != null && customer.getAppUser() == null) {
            customer.setAppUser(appUser);
        }
        if (StringUtils.hasText(normalizedPhone) && !StringUtils.hasText(customer.getPhone())) {
            customer.setPhone(normalizedPhone);
        }
        if (StringUtils.hasText(firstName)) {
            customer.setFirstName(firstName.trim());
        } else if (!StringUtils.hasText(customer.getFirstName())) {
            customer.setFirstName(appUser != null && StringUtils.hasText(appUser.getFirstName())
                    ? appUser.getFirstName().trim()
                    : "Noma'lum mijoz");
        }
        if (StringUtils.hasText(lastName)) {
            customer.setLastName(lastName.trim());
        } else if (!StringUtils.hasText(customer.getLastName()) && appUser != null && StringUtils.hasText(appUser.getLastName())) {
            customer.setLastName(appUser.getLastName().trim());
        }
        if (StringUtils.hasText(middleName)) {
            customer.setMiddleName(middleName.trim());
        }
        customer.setVisitCount(customer.getVisitCount() + 1);
        customer.setLastVisitAt(Instant.now());
        normalizeName(customer);
        normalizePhone(customer);
        return repository.save(customer);
    }

    public Customer requireCustomer(UUID businessId, UUID customerId) {
        return repository.findByBusiness_IdAndId(businessId, customerId)
                .orElseGet(() -> {
                    throw new ResourceNotFoundException("Mijoz topilmadi: " + customerId);
                });
    }

    private void normalizePhone(Customer entity) {
        if (entity.getPhone() != null) {
            String trimmed = entity.getPhone().trim();
            entity.setPhone(trimmed.isEmpty() ? null : trimmed);
        }
    }

    private void normalizeName(Customer entity) {
        if (entity.getFirstName() != null) {
            entity.setFirstName(entity.getFirstName().trim());
        }
        if (!StringUtils.hasText(entity.getFirstName())) {
            throw new IllegalArgumentException("Ism kiritilishi shart");
        }
        if (entity.getLastName() != null) {
            String trimmed = entity.getLastName().trim();
            entity.setLastName(trimmed.isEmpty() ? null : trimmed);
        }
        if (entity.getMiddleName() != null) {
            String trimmed = entity.getMiddleName().trim();
            entity.setMiddleName(trimmed.isEmpty() ? null : trimmed);
        }
    }

}
