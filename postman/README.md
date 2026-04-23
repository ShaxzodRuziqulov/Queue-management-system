# Postman

## Import

1. Postman → **Import** → **File** → tanlang: `QueueManagementSystem.postman_collection.json`
2. Server ishga tushgan bo‘lsin (`http://localhost:9092` — `application.yml` dagi port).
3. **Auth → Login admin** ni birinchi yuboring (standart `admin` / `admin`, yoki `API_ADMIN_USER` / `API_ADMIN_PASSWORD`).
4. Muvaffaqiyatli javobda **Tests** skripti `accessToken` ni collection o‘zgaruvchisi **`token`** ga yozadi — qolgan so‘rovlar avtomatik **Bearer** oladi.

## O‘zgaruvchilar

| O‘zgaruvchi   | Tavsif |
|---------------|--------|
| `baseUrl`     | Masalan `http://localhost:9092` |
| `token`       | Login dan keyin to‘ldiriladi |
| `userId`, `businessId`, … | Namuna UUID; javoblardan haqiqiy `id` ni nusxalab qo‘ying |

## Eslatma

- Bron yaratishda `offeredServiceId` shu `businessId` ostidagi xizmat bo‘lishi kerak; `endAt` > `startAt`.
- Bir `bookingId` uchun bitta sharh.
