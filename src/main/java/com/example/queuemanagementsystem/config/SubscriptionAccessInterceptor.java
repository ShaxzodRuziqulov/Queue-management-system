package com.example.queuemanagementsystem.config;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.exception.BusinessAccessDeniedException;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only rejim: sinov/obuna muddati tugagan (EXPIRED/SUSPENDED yoki muddati o'tgan)
 * biznesga tegishli <b>yozuv</b> so'rovlarini (POST/PUT/PATCH/DELETE) bloklaydi.
 * O'qish (GET/HEAD/OPTIONS) har doim ochiq qoladi.
 *
 * <p>Faqat path'da {@code businessId} bo'lgan yo'llarni qamraydi
 * (masalan {@code /api/v1/businesses/{businessId}/customers}). businessId body'da
 * keladigan yo'llar (bron) service qatlamida alohida tekshiriladi.
 *
 * <p>Admin — cheklovdan ozod (muddati o'tgan bizneslarni ham boshqara oladi).
 */
@Component
@RequiredArgsConstructor
public class SubscriptionAccessInterceptor implements HandlerInterceptor {

    private static final Set<String> READ_METHODS = Set.of(
            HttpMethod.GET.name(), HttpMethod.HEAD.name(), HttpMethod.OPTIONS.name());

    private final BusinessRepository businessRepository;
    private final CurrentUserService currentUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // O'qish amallari — har doim ruxsat.
        if (READ_METHODS.contains(request.getMethod())) {
            return true;
        }
        // Path'dan businessId ni olamiz; bo'lmasa — bu gate bu so'rovga taalluqli emas.
        UUID businessId = extractBusinessId(request);
        if (businessId == null) {
            return true;
        }
        // Admin — cheklovdan ozod.
        if (currentUserService.isAdmin()) {
            return true;
        }
        Business business = businessRepository.findById(businessId).orElse(null);
        // Biznes topilmasa — bu yerda xato bermaymiz; controller/service 404 qaytaradi.
        if (business != null && !business.isAccessAllowed()) {
            throw new BusinessAccessDeniedException(
                    "Biznesning sinov muddati tugagan yoki obuna faol emas. " +
                    "Faqat ko'rish mumkin — davom etish uchun obuna sotib oling.");
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private UUID extractBusinessId(HttpServletRequest request) {
        Object attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attr instanceof Map<?, ?> vars)) {
            return null;
        }
        Object raw = ((Map<String, String>) vars).get("businessId");
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw.toString());
        } catch (IllegalArgumentException ex) {
            return null; // Noto'g'ri UUID — controller o'zi hal qiladi.
        }
    }
}
