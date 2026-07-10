# Postman

## Import

1. Postman -> **Import** -> **File** -> `QueueManagementSystem.postman_collection.json` ni tanlang.
2. Server ishga tushgan bo'lsin: `http://localhost:9092`.
3. Avval **Auth -> Login (admin)** ni yuboring.
4. Muvaffaqiyatli javobdan keyin test skripti `accessToken` ni collection variable `token` ga yozadi.

## Variables

| O'zgaruvchi | Tavsif |
|---|---|
| `baseUrl` | API manzili, odatda `http://localhost:9092` |
| `token` | Login dan keyin avtomatik to'ldiriladi |
| `userId`, `businessId`, `serviceId`, `staffId`, `bookingId`, `reviewId` | CRUD javoblaridan olingan real `id` lar |
| `avatarFile` | Avatar upload uchun lokal fayl yo'li |
| `serviceImageFile` | Service image upload uchun lokal fayl yo'li |

## Qo'shilgan endpointlar

- `Users -> Upload user avatar`
- `Services -> Upload service image`
- `Services -> Delete service image`
- `Staff Portal -> My staff profile / bookings / stats`
- `Reviews -> businessId`, `staffId`, `staff avg rating`

## Eslatma

- `Upload` requestlaridan oldin `avatarFile` va `serviceImageFile` qiymatini o'zingizdagi real fayl yo'liga almashtiring.
- Booking yaratishda `offeredServiceId` shu `businessId` ichidagi xizmat bo'lishi kerak.
- `endAt` qiymati `startAt` dan katta bo'lishi kerak.
- Bir `bookingId` uchun faqat bitta review yaratiladi.
