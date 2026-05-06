package com.example.queuemanagementsystem.scheduler;

import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Har kecha soat 02:00 da muddati tugagan TRIAL bizneslarni EXPIRED qiladi.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrialExpirationScheduler {

    private final BusinessRepository businessRepository;

    /**
     * Cron: har kecha soat 02:00 (server vaqti).
     * Samarali bulk UPDATE — har bir qatorni alohida yuklash o'rniga
     * bitta SQL so'rovi bilan yangilaydi.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireTrials() {
        Instant now = Instant.now();
        int count = businessRepository.expireTrials(
                BusinessStatus.TRIAL,
                BusinessStatus.EXPIRED,
                now
        );
        if (count > 0) {
            log.info("Trial muddati tugadi: {} ta biznes EXPIRED holatiga o'tkazildi.", count);
        }
    }
}
