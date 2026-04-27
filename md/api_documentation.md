# Queue Management System - API Documentation

Ushbu hujjat Frontend (yoki Mobil) dasturchilar uchun backend API'lari bilan qanday ishlash bo'yicha to'liq qo'llanma hisoblanadi.

> [!NOTE]
> Barcha so'rovlar JSON formatida yuborilishi va qabul qilinishi kerak (`Content-Type: application/json`).

## ⚙️ Asosiy ma'lumotlar
- **Base URL:** `http://136.113.208.136/api/v1`
- **Authentication:** JWT (Bearer Token)
- **Token yuborish tartibi:** Har bir himoyalangan so'rovning Header qismida quyidagicha yuboriladi:
  `Authorization: Bearer <sizning_tokeningiz>`

---

## 🔐 1. Avtorizatsiya (Auth)

### Tizimga kirish (Login)
- **Endpoint:** `POST /auth/login`
- **Body:**
```json
{
  "login": "admin",
  "password": "admin"
}
```
- **Javob (Response 200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInSeconds": 86400,
  "userId": "uuid-formatidagi-id",
  "login": "admin",
  "businessOwner": true
}
```

### Ro'yxatdan o'tish (Register)
- **Endpoint:** `POST /auth/register`
- **Body:**
```json
{
  "login": "johndoe",
  "password": "strongpassword123",
  "displayName": "John Doe",
  "email": "john@example.com",
  "phone": "+998901234567"
}
```

---

## 📅 2. Navbatlar (Bookings)

Barcha so'rovlar Bearer Token talab qiladi.

### Barcha navbatlarni olish
- **Endpoint:** `GET /bookings`
- **Query parametrlar (ixtiyoriy):** `?customerId=uuid` yoki `?businessId=uuid`
- **Javob:**
```json
[
  {
    "id": "uuid",
    "customerId": "uuid",
    "businessId": "uuid",
    "offeredServiceId": "uuid",
    "staffId": "uuid",
    "startAt": "2026-04-27T10:00:00Z",
    "endAt": "2026-04-27T10:30:00Z",
    "status": "PENDING",
    "customerNote": "Sochimni qisqaroq oling"
  }
]
```

### Navbatga yozilish (Create Booking)
- **Endpoint:** `POST /bookings`
- **Body:**
```json
{
  "customerId": "uuid",
  "businessId": "uuid",
  "offeredServiceId": "uuid",
  "staffId": "uuid",           // ixtiyoriy
  "startAt": "2026-04-27T10:00:00Z",
  "endAt": "2026-04-27T10:30:00Z",
  "status": "PENDING",
  "customerNote": "Izoh"
}
```

### Navbatni tahrirlash (Update)
- **Endpoint:** `PUT /bookings/{id}`
- **Body:**
```json
{
  "startAt": "2026-04-27T11:00:00Z",
  "endAt": "2026-04-27T11:30:00Z",
  "status": "CONFIRMED",
  "customerNote": "Vaqtni o'zgartirdim"
}
```

### Navbatni o'chirish / Bekor qilish
- **Endpoint:** `DELETE /bookings/{id}`
- **Javob:** `204 No Content`

---

## 🏢 3. Bizneslar (Businesses)

### Bizneslar ro'yxatini olish
- **Endpoint:** `GET /businesses`
- **Javob:**
```json
[
  {
    "id": "uuid",
    "ownerId": "uuid",
    "name": "Super Barbershop",
    "description": "Eng yaxshi sartaroshxona",
    "phone": "+998991234567",
    "address": "Toshkent shahar, Chilonzor"
  }
]
```

### Yangi biznes qo'shish
- **Endpoint:** `POST /businesses`
- **Body:**
```json
{
  "name": "Super Barbershop",
  "description": "Zo'r joy",
  "phone": "+998991234567",
  "address": "Toshkent shahar",
  "category": "BARBERSHOP",
  "ownerId": "uuid"
}
```

*(PUT va DELETE so'rovlari `/businesses/{id}` orqali xuddi shunday ishlaydi)*

---

## 💇‍♂️ 4. Xizmatlar (Offered Services)

### Xizmat qo'shish
- **Endpoint:** `POST /offered-services`
- **Body:**
```json
{
  "businessId": "uuid",
  "name": "Soch kesish",
  "description": "Klassik soch kesish",
  "price": 50000.00,
  "durationMinutes": 30,
  "active": true
}
```

---

## 👥 5. Xodimlar (Staff Members)

### Xodim qo'shish
- **Endpoint:** `POST /staff-members`
- **Body:**
```json
{
  "businessId": "uuid",
  "userId": "uuid", // tizimdagi user bilan bog'lash (ixtiyoriy)
  "name": "Ali Valiyev",
  "position": "Sartarosh",
  "active": true
}
```

---

## ⭐ 6. Fikrlar va Baholar (Reviews)

### Fikr qoldirish
- **Endpoint:** `POST /reviews`
- **Body:**
```json
{
  "businessId": "uuid",
  "customerId": "uuid",
  "rating": 5,
  "comment": "Xizmat juda yoqdi!"
}
```

> [!TIP]
> **Tavsiya:** Dastlab API'larni Postman orqali tekshirib ko'rish uchun loyihaning ichidagi `postman/QueueManagementSystem.postman_collection.json` faylini import qiling. Barcha tayyor so'rovlar o'sha yerda jamlangan!
