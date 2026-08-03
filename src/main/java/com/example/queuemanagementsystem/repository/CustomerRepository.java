package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Page<Customer> findByBusiness_Id(UUID businessId, Pageable pageable);

    Optional<Customer> findByBusiness_IdAndId(UUID businessId, UUID id);

    Optional<Customer> findByBusiness_IdAndPhone(UUID businessId, String phone);

    /** Ism yoki telefon bo'yicha qidiruv (bitta biznes ichida). */
    @Query("select c from Customer c where c.business.id = :businessId and (" +
            "lower(c.fullName) like lower(concat('%', :q, '%')) or " +
            "c.phone like concat('%', :q, '%'))")
    Page<Customer> search(@Param("businessId") UUID businessId, @Param("q") String q, Pageable pageable);
}
