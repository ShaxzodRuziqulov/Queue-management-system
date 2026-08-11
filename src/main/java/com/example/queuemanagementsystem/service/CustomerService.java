package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.Customer;
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
                ? repository.search(businessId, search.trim(), pageable)
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
        normalizePhone(entity);
        return mapper.toDto(repository.save(entity));
    }

    public CustomerDto update(UUID businessId, UUID customerId, CustomerUpdateRequest request) {
        businessService.requireManagerOrAdmin(businessId);
        Customer entity = requireCustomer(businessId, customerId);
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
        normalizePhone(entity);
        return mapper.toDto(entity);
    }

    public void delete(UUID businessId, UUID customerId) {
        businessService.requireManagerOrAdmin(businessId);
        Customer entity = requireCustomer(businessId, customerId);
        repository.delete(entity);
    }

    /**
     * Bron yaratilganda mijozlar bazasini avtomatik to'ldiradi.
     * Telefon bo'yicha topsa — tashrif sonini oshiradi va oxirgi tashrifni yangilaydi;
     * topmasa — yangi mijoz yaratadi. Telefon bo'lmasa — {@code null} qaytaradi
     * (ishonchli dedup bo'lmagani uchun yig'ilmaydi).
     */
    public Customer upsertFromBooking(Business business, String name, String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        String normalizedPhone = phone.trim();
        Customer customer = repository.findByBusiness_IdAndPhone(business.getId(), normalizedPhone)
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setBusiness(business);
                    c.setPhone(normalizedPhone);
                    c.setFullName(StringUtils.hasText(name) ? name.trim() : "Noma'lum mijoz");
                    return c;
                });
        customer.setVisitCount(customer.getVisitCount() + 1);
        customer.setLastVisitAt(Instant.now());
        return repository.save(customer);
    }

    private void normalizePhone(Customer entity) {
        if (entity.getPhone() != null) {
            String trimmed = entity.getPhone().trim();
            entity.setPhone(trimmed.isEmpty() ? null : trimmed);
        }
    }

    Customer requireCustomer(UUID businessId, UUID customerId) {
        return repository.findByBusiness_IdAndId(businessId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Mijoz topilmadi: " + customerId));
    }
}
