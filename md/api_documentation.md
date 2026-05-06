# Queue Management System — API Documentation

Ushbu hujjat Frontend va Mobil dasturchilar uchun backend API'lari bilan ishlash bo'yicha to'liq qo'llanma.

> [!NOTE]
> Barcha so'rovlar JSON formatida yuborilishi va qabul qilinishi kerak (`Content-Type: application/json`).

## ⚙️ Asosiy ma'lumotlar

- **Base URL:** `http://136.113.208.136/api/v1`
- **Authentication:** JWT (Bearer Token)
- **Token yuborish tartibi:** Himoyalangan har bir so'rovning Header qismida:
  `Authorization: Bearer <sizning_tokeningiz>`

---

## 🏗️ Arxitektura — 2 ta Frontend, 1 ta Backend

```
             [ Spring Boot API ]
                     |
       ┌─────────────┴─────────────┐
       │                           │
[ Client Frontend ]       [ Business Frontend ]
  (Mijozlar uchun)          (Biznes egalari uchun)

  - Ro'yxatdan o'tish          - Dashboard
  - Salonlar qidirish          - Navbatlar boshqaruvi
  - Bron qilish                - Xodimlar boshqaruvi
  - Bronlarim                  - Xizmatlar boshqaruvi
```

Login javobidagi maydonlarga qarab frontendni yo'naltiring:

| Maydon | Qiymat | Yo'naltirish |
|--------|--------|--------------|
| `admin` | `true` | Admin Panel |
| `businessOwner` | `true` | Business Dashboard |
| ikkalasi ham | `false` | Client Panel |

---

## 🔐 Rollar

| Rol | Kim oladi | Maqsad |
|-----|-----------|--------|
| `ROLE_USER` | Barcha ro'yxatdan o'tganlar | Bronlash, profil ko'rish |
| `ROLE_BUSINESS_OWNER` | Biznes ochgan foydalanuvchi | Dashboard, biznes boshqaruvi |
| `ROLE_MANAGER` | Tayinlangan xodim *(kelajakda)* | Qisman boshqaruv |
| `ROLE_ADMIN` | Tizim administratori | Barcha foydalanuvchi va bizneslarni boshqarish |

> [!IMPORTANT]
> `ROLE_BUSINESS_OWNER` DB da saqlanmaydi — har login qilganda dinamik hisoblanadi.
> Biznes ochgandan so'ng foydalanuvchi **qayta login** qilishi kerak, shundagina yangi token `businessOwner: true` bo'ladi.

---

## 💳 Trial va Obuna tizimi

### Biznes holatlari (BusinessStatus)

| Status | Ma'nosi |
|--------|---------|
| `TRIAL` | 14 kunlik bepul sinov davri (yangi biznes ochilganda avtomatik) |
| `ACTIVE` | Obuna to'liq faol |
| `EXPIRED` | Sinov muddati tugagan, obuna yo'q — operatsiyalar bloklangan |
| `SUSPENDED` | Admin tomonidan to'xtatilgan |
| `DRAFT` | Hali to'liq sozlanmagan |
| `PENDING_REVIEW` | Moderatsiya kutilmoqda |

### Qanday ishlaydi?

```
Biznes ochiladi → status: TRIAL, trialEndDate: bugundan +14 kun
       ↓
   14 kun davomida to'liq ishlaydi
       ↓
   Har kecha 02:00 — scheduler avtomatik tekshiradi
       ↓
   Muddat o'tdi → status: EXPIRED
       ↓
   Bron, xizmat, xodim qo'shish → HTTP 402
       ↓
   Obuna sotib olindi (admin status o'zgartiradi) → status: ACTIVE
       ↓
   Yana to'liq ishlaydi ✅
```

### EXPIRED bo'lganda bloklangan operatsiyalar

| Operatsiya | Holat |
|------------|-------|
| Yangi bron (Booking) | ❌ HTTP 402 |
| Xizmat qo'shish | ❌ HTTP 402 |
| Xodim qo'shish | ❌ HTTP 402 |
| Dashboard ko'rish | ✅ Ruxsat bor |
| Profil ko'rish | ✅ Ruxsat bor |

---

## 🔐 1. Avtorizatsiya (Auth)

Barcha auth so'rovlari token talab qilmaydi.

### Login

**`POST /auth/login`**

```json
// Request
{
  "login": "admin",
  "password": "admin"
}

// Response 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInSeconds": 86400,
  "userId": "uuid",
  "login": "admin",
  "businessOwner": false,
  "admin": true,
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

### Ro'yxatdan o'tish (Register)

**`POST /auth/register`**

```json
// Request
{
  "login": "johndoe",
  "password": "strongpassword123",
  "displayName": "John Doe",
  "email": "john@example.com",
  "phone": "+998901234567"
}

// Response 201 Created — Login javobidek LoginResponse qaytaradi
```

> [!NOTE]
> Ro'yxatdan o'tganda `ROLE_USER` beriladi va token avtomatik qaytariladi.

### Yangi biznes ochgan foydalanuvchi uchun oqim

```
1. POST /auth/register   → token (businessOwner: false)
2. POST /businesses      → biznes yaratiladi
3. POST /auth/login      → qayta login → token (businessOwner: true) ✅
```

---

## 👤 2. Foydalanuvchilar (Users)

> [!WARNING]
> `GET /users` (ro'yxat) va `POST /users` (yaratish) faqat **ROLE_ADMIN** uchun.
> `DELETE /users/{id}` faqat **ROLE_ADMIN** uchun.
> `GET /users/{id}` va `PUT /users/{id}` — faqat o'z profili yoki admin.

### Barcha foydalanuvchilar ro'yxati (Admin)

**`GET /users`**

### Foydalanuvchini olish

**`GET /users/{id}`**

### Foydalanuvchi yaratish (Admin)

**`POST /users`**

```json
// Request
{
  "login": "testuser",
  "password": "password12",
  "displayName": "Test User",
  "email": "user@example.com",
  "phone": "+998901112233",
  "avatarUrl": null,
  "active": true
}
```

### Foydalanuvchini yangilash

**`PUT /users/{id}`**

```json
// Request (barcha maydonlar ixtiyoriy)
{
  "password": "newpassword12",
  "displayName": "Yangilangan Ism",
  "email": "new@example.com",
  "phone": "+998909876543",
  "avatarUrl": "https://example.com/avatar.png",
  "active": true
}
```

### Foydalanuvchini o'chirish (Admin)

**`DELETE /users/{id}`** → `204 No Content`

---

## 🔑 3. Foydalanuvchi Identifikatorlari (User Identities)

Tashqi autentifikatsiya provayderlarini (Google, Telegram va h.k.) bog'lash.

Barcha so'rovlar token talab qiladi. Yo'l: `/users/{userId}/identities`

### Ro'yxat

**`GET /users/{userId}/identities`**

### Bitta olish

**`GET /users/{userId}/identities/{identityId}`**

### Yaratish

**`POST /users/{userId}/identities`**

```json
// Request
{
  "provider": "TELEGRAM",
  "providerSubject": "999888777",
  "providerEmail": "tg@example.com"
}
```

### Yangilash

**`PUT /users/{userId}/identities/{identityId}`**

```json
// Request
{
  "providerEmail": "newtg@example.com"
}
```

### O'chirish

**`DELETE /users/{userId}/identities/{identityId}`** → `204 No Content`

---

## 🏢 4. Bizneslar (Businesses)

### Barcha bizneslar

**`GET /businesses`**

**`GET /businesses?ownerId={uuid}`** — muayyan eganing bizneslarini olish

### Bitta biznesni olish

**`GET /businesses/{id}`**

```json
// Response 200 OK
{
  "id": "uuid",
  "ownerId": "uuid",
  "name": "Super Barbershop",
  "description": "Eng yaxshi sartaroshxona",
  "addressLine": "Chilonzor, 1-mavze",
  "city": "Toshkent",
  "latitude": 41.311081,
  "longitude": 69.240562,
  "contactPhone": "+998991234567",
  "status": "TRIAL",
  "trialEndDate": "2026-05-13T00:00:00Z",
  "subscriptionEndDate": null
}
```

### Biznes yaratish

**`POST /businesses`** — Har qanday autentifikatsiyalangan foydalanuvchi

```json
// Request
{
  "ownerId": "uuid",
  "name": "Super Barbershop",
  "description": "Zo'r joy",
  "addressLine": "Chilonzor, 1-mavze",
  "city": "Toshkent",
  "latitude": 41.311081,
  "longitude": 69.240562,
  "contactPhone": "+998991234567"
}
```

> [!NOTE]
> - `status` va `trialEndDate` avtomatik belgilanadi: `TRIAL`, bugundan +14 kun.
> - Admin bo'lmagan foydalanuvchi uchun `ownerId` e'tiborga olinmaydi — backend tokendan o'z ID'sini oladi.

### Biznesni yangilash

**`PUT /businesses/{id}`** — Faqat biznes egasi yoki admin (`403` aks holda)

```json
// Request (barcha maydonlar ixtiyoriy)
{
  "name": "Yangi nom",
  "description": "Yangilangan tavsif",
  "addressLine": "Yunusobod, 7-mavze",
  "city": "Toshkent",
  "latitude": 41.36,
  "longitude": 69.28,
  "contactPhone": "+998991112233"
}
```

### Biznesni o'chirish

**`DELETE /businesses/{id}`** — Faqat biznes egasi yoki admin → `204 No Content`

### Biznes statusini o'zgartirish (Admin only)

**`PUT /businesses/{id}/status`** — Faqat `ROLE_ADMIN` (`403` aks holda)

```json
// Request
{
  "status": "ACTIVE",
  "subscriptionEndDate": "2026-12-31T23:59:59Z"
}
```

> [!NOTE]
> `subscriptionEndDate` ixtiyoriy. Faqat to'xtatish uchun: `{ "status": "SUSPENDED" }`

---

## 🕐 5. Ish Soatlari (Business Hours)

Yo'l: `/businesses/{businessId}/hours`

### Ro'yxat

**`GET /businesses/{businessId}/hours`**

### Bitta olish

**`GET /businesses/{businessId}/hours/{hoursId}`**

### Yaratish

**`POST /businesses/{businessId}/hours`**

```json
// Request
{
  "weekday": "MONDAY",
  "closed": false,
  "opensAt": "09:00:00",
  "closesAt": "18:00:00"
}
```

> weekday qiymatlari: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

### Yangilash

**`PUT /businesses/{businessId}/hours/{hoursId}`**

```json
// Request (barcha maydonlar ixtiyoriy)
{
  "closed": false,
  "opensAt": "10:00:00",
  "closesAt": "20:00:00"
}
```

### O'chirish

**`DELETE /businesses/{businessId}/hours/{hoursId}`** → `204 No Content`

---

## 💇 6. Xizmatlar (Offered Services)

Yo'l: `/businesses/{businessId}/services`

> [!WARNING]
> Biznes `EXPIRED` yoki `SUSPENDED` holatida bo'lsa, xizmat qo'shish **HTTP 402** qaytaradi.

### Ro'yxat

**`GET /businesses/{businessId}/services`**

### Bitta olish

**`GET /businesses/{businessId}/services/{serviceId}`**

### Yaratish

**`POST /businesses/{businessId}/services`**

```json
// Request
{
  "name": "Soch kesish",
  "description": "Klassik soch kesish xizmati",
  "durationMinutes": 30,
  "basePrice": 50000.00,
  "active": true
}
```

### Yangilash

**`PUT /businesses/{businessId}/services/{serviceId}`**

```json
// Request (barcha maydonlar ixtiyoriy)
{
  "name": "Soch kesish Premium",
  "description": "Yangilangan tavsif",
  "durationMinutes": 45,
  "basePrice": 75000.00,
  "active": true
}
```

### O'chirish

**`DELETE /businesses/{businessId}/services/{serviceId}`** → `204 No Content`

---

## 👥 7. Xodimlar (Staff Members)

Yo'l: `/businesses/{businessId}/staff`

> [!WARNING]
> Biznes `EXPIRED` yoki `SUSPENDED` holatida bo'lsa, xodim qo'shish **HTTP 402** qaytaradi.

### Ro'yxat

**`GET /businesses/{businessId}/staff`**

### Bitta olish

**`GET /businesses/{businessId}/staff/{staffId}`**

### Yaratish

**`POST /businesses/{businessId}/staff`**

```json
// Request
{
  "displayName": "Ali Valiyev",
  "linkedUserId": null,
  "active": true
}
```

> `linkedUserId` — xodimni tizim foydalanuvchisiga bog'lash uchun (ixtiyoriy).

### Yangilash

**`PUT /businesses/{businessId}/staff/{staffId}`**

```json
// Request (barcha maydonlar ixtiyoriy)
{
  "displayName": "Ali Valiyev (Usta)",
  "linkedUserId": null,
  "active": true
}
```

### O'chirish

**`DELETE /businesses/{businessId}/staff/{staffId}`** → `204 No Content`

---

## 📅 8. Navbatlar (Bookings)

> [!WARNING]
> Biznes `EXPIRED` yoki `SUSPENDED` holatida bo'lsa, bron yaratish **HTTP 402** qaytaradi.

### Ro'yxat

**`GET /bookings`** — Barcha bronlar

**`GET /bookings?customerId={uuid}`** — Mijoz bo'yicha filtrlash

**`GET /bookings?businessId={uuid}`** — Biznes bo'yicha filtrlash

### Bitta olish

**`GET /bookings/{id}`**

```json
// Response 200 OK
{
  "id": "uuid",
  "customerId": "uuid",
  "businessId": "uuid",
  "offeredServiceId": "uuid",
  "staffId": "uuid",
  "startAt": "2026-06-01T09:00:00Z",
  "endAt": "2026-06-01T09:30:00Z",
  "status": "PENDING",
  "customerNote": "Sochimni qisqaroq oling"
}
```

### Yaratish

**`POST /bookings`**

```json
// Request
{
  "customerId": "uuid",
  "businessId": "uuid",
  "offeredServiceId": "uuid",
  "staffId": "uuid",
  "startAt": "2026-06-01T09:00:00Z",
  "endAt": "2026-06-01T09:30:00Z",
  "status": "PENDING",
  "customerNote": "Sochimni qisqaroq oling"
}
```

> `staffId` ixtiyoriy. `status` qiymatlari: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`

### Yangilash

**`PUT /bookings/{id}`**

```json
// Request (barcha maydonlar ixtiyoriy)
{
  "staffId": "uuid",
  "startAt": "2026-06-01T10:00:00Z",
  "endAt": "2026-06-01T10:30:00Z",
  "status": "CONFIRMED",
  "customerNote": "Vaqtni o'zgartirdim"
}
```

### O'chirish

**`DELETE /bookings/{id}`** → `204 No Content`

---

## ⭐ 9. Sharhlar (Reviews)

### Ro'yxat

**`GET /reviews`**

### Bitta olish

**`GET /reviews/{id}`**

### Yaratish

**`POST /reviews`**

```json
// Request
{
  "bookingId": "uuid",
  "stars": 5,
  "comment": "Xizmat juda yoqdi!"
}
```

> `stars`: `1` (eng past) — `5` (eng yaxshi). Sharh **bron asosida** yoziladi.

### Yangilash

**`PUT /reviews/{id}`**

```json
// Request (barcha maydonlar ixtiyoriy)
{
  "stars": 4,
  "comment": "Yaxshi, lekin biroz kechikdi"
}
```

### O'chirish

**`DELETE /reviews/{id}`** → `204 No Content`

---

## ❌ Xato kodlari

| HTTP kodi | Ma'nosi |
|-----------|---------|
| `400 Bad Request` | Noto'g'ri yoki yetishmayotgan ma'lumot |
| `401 Unauthorized` | Token yo'q yoki yaroqsiz |
| `402 Payment Required` | Trial muddati tugagan yoki obuna faol emas |
| `403 Forbidden` | Ruxsat yo'q (noto'g'ri rol yoki boshqa eganing resursi) |
| `404 Not Found` | Resurs topilmadi |
| `409 Conflict` | Ma'lumot allaqachon mavjud (masalan, band login) |

---

## 📋 Endpoint umumiy ro'yxati

| Method | Endpoint | Ruxsat | Izoh |
|--------|----------|--------|------|
| POST | `/auth/login` | Hamma | Login |
| POST | `/auth/register` | Hamma | Ro'yxatdan o'tish |
| GET | `/users` | Admin | Barcha foydalanuvchilar |
| GET | `/users/{id}` | O'zi / Admin | |
| POST | `/users` | Admin | Foydalanuvchi yaratish |
| PUT | `/users/{id}` | O'zi / Admin | |
| DELETE | `/users/{id}` | Admin | |
| GET | `/users/{userId}/identities` | Authenticated | |
| GET | `/users/{userId}/identities/{id}` | Authenticated | |
| POST | `/users/{userId}/identities` | Authenticated | |
| PUT | `/users/{userId}/identities/{id}` | Authenticated | |
| DELETE | `/users/{userId}/identities/{id}` | Authenticated | |
| GET | `/businesses` | Authenticated | `?ownerId=` filter bor |
| GET | `/businesses/{id}` | Authenticated | |
| POST | `/businesses` | Authenticated | Har kim ocha oladi |
| PUT | `/businesses/{id}` | Egasi / Admin | |
| DELETE | `/businesses/{id}` | Egasi / Admin | |
| PUT | `/businesses/{id}/status` | **Admin** | Status o'zgartirish |
| GET | `/businesses/{id}/hours` | Authenticated | |
| GET | `/businesses/{id}/hours/{hId}` | Authenticated | |
| POST | `/businesses/{id}/hours` | Authenticated | |
| PUT | `/businesses/{id}/hours/{hId}` | Authenticated | |
| DELETE | `/businesses/{id}/hours/{hId}` | Authenticated | |
| GET | `/businesses/{id}/services` | Authenticated | |
| GET | `/businesses/{id}/services/{sId}` | Authenticated | |
| POST | `/businesses/{id}/services` | Authenticated | EXPIRED → 402 |
| PUT | `/businesses/{id}/services/{sId}` | Authenticated | |
| DELETE | `/businesses/{id}/services/{sId}` | Authenticated | |
| GET | `/businesses/{id}/staff` | Authenticated | |
| GET | `/businesses/{id}/staff/{stId}` | Authenticated | |
| POST | `/businesses/{id}/staff` | Authenticated | EXPIRED → 402 |
| PUT | `/businesses/{id}/staff/{stId}` | Authenticated | |
| DELETE | `/businesses/{id}/staff/{stId}` | Authenticated | |
| GET | `/bookings` | Authenticated | `?customerId=` / `?businessId=` |
| GET | `/bookings/{id}` | Authenticated | |
| POST | `/bookings` | Authenticated | EXPIRED → 402 |
| PUT | `/bookings/{id}` | Authenticated | |
| DELETE | `/bookings/{id}` | Authenticated | |
| GET | `/reviews` | Authenticated | |
| GET | `/reviews/{id}` | Authenticated | |
| POST | `/reviews` | Authenticated | Bron asosida |
| PUT | `/reviews/{id}` | Authenticated | |
| DELETE | `/reviews/{id}` | Authenticated | |

> [!TIP]
> Postman collection: `postman/QueueManagementSystem.postman_collection.json` — barcha so'rovlar tayyor, token avtomatik saqlanadi.
