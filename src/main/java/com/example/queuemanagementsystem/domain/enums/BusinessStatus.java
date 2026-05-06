package com.example.queuemanagementsystem.domain.enums;

public enum BusinessStatus {
    /** Egasi yangi yaratdi, hali to'liq sozlanmagan */
    DRAFT,
    /** Moderatsiya kutilmoqda */
    PENDING_REVIEW,
    /** Faol, obuna to'liq */
    ACTIVE,
    /** 14 kunlik bepul sinov davri */
    TRIAL,
    /** Sinov muddati tugadi, obuna yo'q – operatsiyalar bloklangan */
    EXPIRED,
    /** Admin tomonidan to'xtatilgan */
    SUSPENDED
}
