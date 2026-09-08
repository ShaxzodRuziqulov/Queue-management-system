package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.SupportTicket;
import com.example.queuemanagementsystem.domain.TelegramLinkToken;
import com.example.queuemanagementsystem.dto.TelegramLinkDto;
import com.example.queuemanagementsystem.dto.SupportTicketDto;
import com.example.queuemanagementsystem.dto.SupportTicketUpdateRequest;
import com.example.queuemanagementsystem.domain.enums.SupportTicketStatus;
import com.example.queuemanagementsystem.domain.enums.SupportSource;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.TelegramLinkTokenRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Telegram Bot API bilan polling orqali ishlaydigan support kanali. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramSupportService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AppUserRepository userRepository;
    private final TelegramLinkTokenRepository linkTokenRepository;
    private final CurrentUserService currentUserService;
    private final SupportTicketService ticketService;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;
    private final RestClient telegramClient = RestClient.create("https://api.telegram.org");

    @Value("${telegram.bot.token:}") private String botToken;
    @Value("${telegram.bot.enabled:true}") private boolean enabled;
    @Value("${telegram.bot.username:}") private String botUsername;
    @Value("${telegram.support.chat-ids:}") private String supportChatIdsValue;
    @Value("${TELEGRAM_WEB_URL:https://onetime-management.netlify.app/help}") private String webUrl;
    private long updateOffset = 0;

    @Transactional
    public TelegramLinkDto createLink(String sourceValue) {
        if (!isConfigured()) throw new IllegalStateException("Telegram bot hali sozlanmagan");
        UUID userId = currentUserService.getCurrentUserId();
        AppUser user = userRepository.findById(userId).orElseThrow();
        linkTokenRepository.deleteByUser_Id(userId);
        TelegramLinkToken link = new TelegramLinkToken();
        link.setUser(user);
        link.setToken(randomToken());
        link.setSource(parseSource(sourceValue));
        link.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        linkTokenRepository.save(link);
        return new TelegramLinkDto("https://t.me/" + botUsername.replace("@", "") + "?start=" + link.getToken(), user.getTelegramChatId() != null);
    }

    @Scheduled(fixedDelayString = "${telegram.bot.poll-delay-ms:1500}")
    public void poll() {
        if (!enabled || !isConfigured()) return;
        try {
            JsonNode response = new com.fasterxml.jackson.databind.ObjectMapper().readTree(telegramClient.get().uri("/bot" + botToken + "/getUpdates?timeout=0&offset=" + updateOffset)
                    .retrieve().body(String.class));
            if (response == null || !response.path("ok").asBoolean()) return;
            for (JsonNode update : response.path("result")) {
                try {
                    if (update.has("callback_query")) handleCallback(update.path("callback_query"));
                    else handleMessage(update.path("message"));
                } catch (org.springframework.web.client.HttpClientErrorException ex) {
                    if (ex.getStatusCode().value() == 429) throw ex;
                    log.warn("Telegram rejected delivery (HTTP {})", ex.getStatusCode().value());
                }
                updateOffset = Math.max(updateOffset, update.path("update_id").asLong() + 1);
            }
        } catch (Exception ex) {
            log.warn("Telegram update processing failed ({})", ex.getClass().getSimpleName());
        }
    }

    private void handleMessage(JsonNode message) {
        if (message.isMissingNode() || message.isEmpty()) return;
        long chatId = message.path("chat").path("id").asLong();
        if (chatId == 0) return;
        String text = message.path("text").asText("").trim();
        if (!"private".equals(message.path("chat").path("type").asText())) return;
        if (text.equals("/id")) {
            sendMessage(chatId, "Telegram chat ID: " + chatId);
            return;
        }
        if (isSupportChat(chatId)) {
            if (handleOperatorReply(message, chatId)) return;
            if (handleOperatorCommand(text, chatId)) return;
            if (text.equals("/start") || text.equals("/help")) {
                showOperatorMenu(chatId);
                return;
            }
            if (text.equals("📥 Yangi murojaatlar")) { showTickets(chatId, SupportTicketStatus.NEW, "Yangi murojaatlar"); return; }
            if (text.equals("🔄 Jarayondagi murojaatlar")) { showTickets(chatId, SupportTicketStatus.IN_PROGRESS, "Jarayondagi murojaatlar"); showTickets(chatId, SupportTicketStatus.WAITING_USER, "User javobi kutilayotganlar"); return; }
            if (text.equals("✅ Hal qilinganlar")) { showTickets(chatId, SupportTicketStatus.RESOLVED, "Hal qilingan murojaatlar"); return; }
            if (text.equals("ℹ️ Yo‘riqnoma")) { sendMessage(chatId, "Ticket kartasidagi tugmalar bilan holatni o‘zgartiring. Userga javob yozish uchun ticket xabariga Reply bosing. /status va /reply buyruqlari zaxira usul sifatida ham ishlaydi."); return; }
            return;
        }
        if (text.startsWith("/start")) {
            String[] parts = text.split("\\s+", 2);
            if (parts.length == 2) transactionTemplate.executeWithoutResult(status -> linkUser(parts[1], chatId));
            else showMenu(chatId);
            return;
        }
        if (text.equals("/help") || text.equals("🏠 Bosh menyu")) { showMenu(chatId); return; }
        if (text.equals("✉️ Murojaat yozish")) {
            sendMessage(chatId, "Muammo yoki savolingizni shu chatga yozing. Rasm, ovozli xabar yoki fayl ham yuborishingiz mumkin. Javob shu chatga keladi.");
            return;
        }
        if (text.equals("✅ Muammo hal bo‘ldi")) {
            if (ticketService.resolveForTelegramChat(chatId, supportSourceForChat(chatId))) {
                sendMessage(chatId, "✅ Rahmat! Murojaatingiz hal qilingan deb belgilandi.");
            } else {
                sendMessage(chatId, "Yopiladigan faol murojaat topilmadi.");
            }
            return;
        }
        if (text.equals("🔗 Hisobni bog‘lash")) {
            call("sendMessage", Map.of("chat_id", chatId, "text", "OnTime hisobingizga kiring, Yordam sahifasidagi Telegram botni ochish tugmasini bosing, keyin Start ni bosing.",
                    "reply_markup", Map.of("inline_keyboard", List.of(List.of(Map.of("text", "OnTime hisobiga kirish", "url", webUrl))))));
            return;
        }
        userRepository.findByTelegramChatId(chatId).ifPresentOrElse(
                user -> { if (user.isActive()) forwardSupportMessage(user, message); },
                () -> forwardGuestMessage(chatId, message));
    }

    @Transactional
    protected void linkUser(String token, long chatId) {
        linkTokenRepository.findByToken(token).ifPresentOrElse(link -> {
            if (link.getExpiresAt().isBefore(Instant.now())) {
                linkTokenRepository.delete(link);
                sendMessage(chatId, "Bu havola muddati tugagan. Tizimdan yangisini oling.");
                return;
            }
            AppUser user = link.getUser();
            if (!user.isActive() || userRepository.findByTelegramChatId(chatId)
                    .filter(existing -> !existing.getId().equals(user.getId())).isPresent()) {
                sendMessage(chatId, "Bu Telegram hisobini bog‘lab bo‘lmadi. Boshqa hisobga bog‘langan yoki profilingiz faol emas.");
                return;
            }
            user.setTelegramChatId(chatId);
            user.setTelegramSupportSource(link.getSource());
            userRepository.save(user);
            linkTokenRepository.delete(link);
            sendMessage(chatId, "✅ Hisobingiz muvaffaqiyatli bog‘landi.");
            showMenu(chatId);
        }, () -> sendMessage(chatId, "Havola yaroqsiz yoki avval ishlatilgan."));
    }

    private void forwardSupportMessage(AppUser user, JsonNode message) {
        SupportTicket ticket = ticketService.recordIncoming(user.getTelegramChatId(), displayName(user), messageText(message), mediaType(message), message.path("message_id").asLong(), supportSourceForUser(user));
        forwardToOperators(ticket, user.getTelegramChatId(), message);
    }

    private void forwardGuestMessage(long chatId, JsonNode message) {
        SupportTicket ticket = ticketService.recordIncoming(chatId, displayName(message.path("from")), messageText(message), mediaType(message), message.path("message_id").asLong(), SupportSource.GENERAL);
        forwardToOperators(ticket, chatId, message);
    }

    private void forwardToOperators(SupportTicket ticket, long chatId, JsonNode message) {
        List<Long> supportChats = supportChatIds();
        if (supportChats.isEmpty()) {
            log.warn("Support message received but TELEGRAM_SUPPORT_CHAT_IDS is not configured");
            sendMessage(chatId, "Support hali sozlanmagan. Xabar saqlandi, keyinroq javob beramiz.");
            return;
        }
        for (Long supportChatId : supportChats) {
            call("forwardMessage", Map.of("chat_id", supportChatId, "from_chat_id", chatId, "message_id", message.path("message_id").asLong()));
            sendTicketCard(supportChatId, ticket);
        }
        sendMessage(chatId, "✅ Xabaringiz qabul qilindi. Operator javobi shu chatga keladi.");
    }

    private void sendMessage(long chatId, String text) { call("sendMessage", Map.of("chat_id", chatId, "text", text)); }
    public void sendOperatorReply(SupportTicket ticket, String content) {
        sendUserAnswerActions(ticket, "📩 Support javobi\n\n" + content + "\n\nBu javob yordam berdimi?");
    }
    private boolean handleOperatorReply(JsonNode message, long operatorChatId) {
        JsonNode replied = message.path("reply_to_message");
        String reference = replied.path("text").asText("");
        try {
            SupportTicket ticket;
            if (reference.startsWith("📩 Murojaat #")) {
                String shortId = reference.substring("📩 Murojaat #".length()).split("\\n", 2)[0].trim();
                ticket = ticketService.requireByShortId(shortId);
            } else if (reference.startsWith("Murojaat #")) {
                String idText = reference.substring("Murojaat #".length()).split("\\n", 2)[0].trim();
                ticket = ticketService.require(UUID.fromString(idText));
            } else return false;
            String content = messageText(message);
            if (content.isBlank()) content = "Operator media yoki fayl yubordi";
            ticketService.addOperatorReply(ticket.getId(), content);
            if (mediaType(message) == null) sendOperatorReply(ticket, content);
            else {
                call("copyMessage", Map.of("chat_id", ticket.getTelegramChatId(), "from_chat_id", operatorChatId, "message_id", message.path("message_id").asLong()));
                sendUserAnswerActions(ticket, "Bu javob yordam berdimi?");
            }
            sendMessage(operatorChatId, "✅ Javob ticketga saqlandi va foydalanuvchiga yuborildi.");
            return true;
        } catch (IllegalArgumentException ignored) { return false; }
    }
    private void handleCallback(JsonNode callback) {
        JsonNode card = callback.path("message");
        long chatId = card.path("chat").path("id").asLong();
        String callbackId = callback.path("id").asText("");
        String data = callback.path("data").asText("");
        if (data.startsWith("ticket-user:")) {
            handleUserCallback(callback, chatId, callbackId, data);
            return;
        }
        if (!isSupportChat(chatId)) return;
        if (data.startsWith("ticket-priority:")) {
            handlePriorityCallback(callback, chatId, callbackId, data);
            return;
        }
        if (!data.startsWith("ticket-status:")) return;
        try {
            String[] parts = data.split(":", 3);
            if (parts.length != 3) throw new IllegalArgumentException();
            SupportTicketStatus status = SupportTicketStatus.valueOf(parts[2]);
            SupportTicket ticket = ticketService.update(UUID.fromString(parts[1]), new SupportTicketUpdateRequest(status.name(), null));
            if (status == SupportTicketStatus.RESOLVED || status == SupportTicketStatus.CLOSED)
                sendMessage(ticket.getTelegramChatId(), "✅ Murojaatingiz hal qilindi. Yana savol bo‘lsa, shu chatga yozishingiz mumkin.");
            call("answerCallbackQuery", Map.of("callback_query_id", callbackId, "text", "Holat: " + statusLabel(status)));
            call("editMessageText", Map.of("chat_id", chatId, "message_id", card.path("message_id").asLong(),
                    "text", ticketCardText(ticket), "reply_markup", ticketKeyboard(ticket.getId(), ticket.getStatus())));
        } catch (IllegalArgumentException ex) {
            if (!callbackId.isBlank()) call("answerCallbackQuery", Map.of("callback_query_id", callbackId, "text", "Ticket yoki holat noto‘g‘ri", "show_alert", true));
        }
    }
    private void handlePriorityCallback(JsonNode callback, long operatorChatId, String callbackId, String data) {
        try {
            String[] parts = data.split(":", 3);
            if (parts.length != 3) throw new IllegalArgumentException();
            SupportTicket ticket = ticketService.update(UUID.fromString(parts[1]), new SupportTicketUpdateRequest(null, parts[2]));
            call("answerCallbackQuery", Map.of("callback_query_id", callbackId, "text", "Muhimlik: " + priorityLabel(ticket.getPriority().name())));
            JsonNode card = callback.path("message");
            call("editMessageText", Map.of("chat_id", operatorChatId, "message_id", card.path("message_id").asLong(),
                    "text", ticketCardText(ticket), "reply_markup", ticketKeyboard(ticket.getId(), ticket.getStatus())));
        } catch (IllegalArgumentException ex) {
            if (!callbackId.isBlank()) call("answerCallbackQuery", Map.of("callback_query_id", callbackId, "text", "Ticket yoki muhimlik noto‘g‘ri", "show_alert", true));
        }
    }
    private void handleUserCallback(JsonNode callback, long chatId, String callbackId, String data) {
        try {
            String[] parts = data.split(":", 3);
            if (parts.length != 3) throw new IllegalArgumentException();
            SupportTicket ticket = ticketService.require(UUID.fromString(parts[1]));
            if (ticket.getTelegramChatId() != chatId) throw new IllegalArgumentException();
            SupportTicketStatus next = "RESOLVED".equals(parts[2]) ? SupportTicketStatus.RESOLVED : SupportTicketStatus.IN_PROGRESS;
            ticket = ticketService.update(ticket.getId(), new SupportTicketUpdateRequest(next.name(), null));
            String result = next == SupportTicketStatus.RESOLVED
                    ? "✅ Murojaat yopildi. Yana savol tug‘ilsa, shu chatga yozing."
                    : "✉️ Murojaat qayta ochildi. Operatorga yuborildi.";
            call("answerCallbackQuery", Map.of("callback_query_id", callbackId, "text", result));
            JsonNode message = callback.path("message");
            call("editMessageText", Map.of("chat_id", chatId, "message_id", message.path("message_id").asLong(), "text", result));
            if (next == SupportTicketStatus.IN_PROGRESS) {
                for (Long supportChatId : supportChatIds()) {
                    sendMessage(supportChatId, "🔔 User qo‘shimcha yordam so‘radi.");
                    sendTicketCard(supportChatId, ticket);
                }
            }
        } catch (IllegalArgumentException ex) {
            if (!callbackId.isBlank()) call("answerCallbackQuery", Map.of("callback_query_id", callbackId, "text", "Murojaat topilmadi", "show_alert", true));
        }
    }
    private boolean handleOperatorCommand(String text, long operatorChatId) {
        if (text.startsWith("/status ")) {
            String[] parts = text.split("\\s+", 3);
            if (parts.length < 3) { sendMessage(operatorChatId, "Format: /status TICKET_UUID RESOLVED"); return true; }
            try {
                SupportTicket ticket = ticketService.update(UUID.fromString(parts[1]), new SupportTicketUpdateRequest(parts[2], null));
                if (ticket.getStatus() == SupportTicketStatus.RESOLVED || ticket.getStatus() == SupportTicketStatus.CLOSED)
                    sendMessage(ticket.getTelegramChatId(), "✅ Murojaatingiz hal qilindi. Yana savol bo‘lsa, shu chatga yozishingiz mumkin.");
                sendMessage(operatorChatId, "✅ Murojaat holati: " + statusLabel(ticket.getStatus()));
            } catch (IllegalArgumentException ex) { sendMessage(operatorChatId, "Ticket ID yoki status noto‘g‘ri."); }
            return true;
        }
        if (!text.startsWith("/reply ")) return false;
        String[] parts = text.split("\\s+", 3);
        if (parts.length < 3) { sendMessage(operatorChatId, "Format: /reply TICKET_UUID javob matni"); return true; }
        try {
            SupportTicket ticket = ticketService.addOperatorReply(UUID.fromString(parts[1]), parts[2]);
            sendOperatorReply(ticket, parts[2]);
            sendMessage(operatorChatId, "✅ Javob ticketga saqlandi va foydalanuvchiga yuborildi.");
        } catch (IllegalArgumentException ex) {
            sendMessage(operatorChatId, "Ticket ID noto‘g‘ri yoki ticket topilmadi.");
        }
        return true;
    }
    private void showOperatorMenu(long chatId) {
        call("sendMessage", Map.of("chat_id", chatId,
                "text",
                "Operator paneli. Tugma bilan ticketlarni ko‘ring. Ticket xabariga Reply bosib userga javob yozing.",
                "reply_markup", Map.of("keyboard", List.of(List.of(Map.of("text", "📥 Yangi murojaatlar")),
                        List.of(Map.of("text", "🔄 Jarayondagi murojaatlar")),
                        List.of(Map.of("text", "✅ Hal qilinganlar"), Map.of("text", "ℹ️ Yo‘riqnoma"))), "resize_keyboard", true)));
    }
    private void showTickets(long chatId, SupportTicketStatus status, String title) {
        List<SupportTicketDto> items = ticketService.operatorTickets(status);
        if (items.isEmpty()) { sendMessage(chatId, title + ": hozircha yo‘q."); return; }
        sendMessage(chatId, title + " (oxirgi " + items.size() + " ta):");
        for (SupportTicketDto item : items) sendTicketCard(chatId, item);
    }
    private void sendTicketCard(long chatId, SupportTicket ticket) {
        call("sendMessage", Map.of("chat_id", chatId, "text", ticketCardText(ticket), "reply_markup", ticketKeyboard(ticket.getId(), ticket.getStatus())));
    }
    private void sendTicketCard(long chatId, SupportTicketDto ticket) {
        SupportTicketStatus status = SupportTicketStatus.valueOf(ticket.status());
        call("sendMessage", Map.of("chat_id", chatId, "text", ticketCardText(ticket.id(), ticket.requesterName(), ticket.subject(), SupportSource.valueOf(ticket.source()), status, ticket.priority()), "reply_markup", ticketKeyboard(ticket.id(), status)));
    }
    private void sendUserAnswerActions(SupportTicket ticket, String text) {
        String prefix = "ticket-user:" + ticket.getId() + ":";
        call("sendMessage", Map.of("chat_id", ticket.getTelegramChatId(), "text", text,
                "reply_markup", Map.of("inline_keyboard", List.of(List.of(
                        Map.of("text", "✅ Muammo hal bo‘ldi", "callback_data", prefix + "RESOLVED"),
                        Map.of("text", "✉️ Hali yordam kerak", "callback_data", prefix + "IN_PROGRESS"))))));
    }
    private String ticketCardText(SupportTicket ticket) { return ticketCardText(ticket.getId(), ticket.getRequesterName(), ticket.getSubject(), ticket.getSource(), ticket.getStatus(), ticket.getPriority().name()); }
    private String ticketCardText(UUID id, String name, String subject, SupportSource source, SupportTicketStatus status, String priority) {
        String replyHint = status == SupportTicketStatus.RESOLVED || status == SupportTicketStatus.CLOSED ? "" : "\nJavob berish uchun shu xabarga Reply bosing.";
        return "📩 Murojaat #" + shortTicketId(id) + "\n👤 " + name + "\n📍 " + sourceLabel(source) + "\n📌 " + statusLabel(status) + " · " + priorityLabel(priority) + "\n\n💬 Xabar:\n" + subject + replyHint;
    }
    private Map<String, Object> ticketKeyboard(UUID id, SupportTicketStatus status) {
        String prefix = "ticket-status:" + id + ":";
        List<List<Map<String, String>>> rows = new ArrayList<>(switch (status) {
            case NEW -> List.of(List.of(Map.of("text", "▶️ Jarayonga olish", "callback_data", prefix + "IN_PROGRESS"), Map.of("text", "✅ Hal qilish", "callback_data", prefix + "RESOLVED")));
            case IN_PROGRESS -> List.of(List.of(Map.of("text", "⏳ User javobini kutish", "callback_data", prefix + "WAITING_USER"), Map.of("text", "✅ Hal qilish", "callback_data", prefix + "RESOLVED")));
            case WAITING_USER -> List.of(List.of(Map.of("text", "▶️ Jarayonga qaytarish", "callback_data", prefix + "IN_PROGRESS"), Map.of("text", "✅ Hal qilish", "callback_data", prefix + "RESOLVED")));
            case RESOLVED, CLOSED -> List.of(List.of(Map.of("text", "🔄 Qayta ochish", "callback_data", prefix + "IN_PROGRESS")));
        });
        String priorityPrefix = "ticket-priority:" + id + ":";
        rows.add(List.of(Map.of("text", "⬇️ Past", "callback_data", priorityPrefix + "LOW"), Map.of("text", "• Oddiy", "callback_data", priorityPrefix + "NORMAL")));
        rows.add(List.of(Map.of("text", "⬆️ Yuqori", "callback_data", priorityPrefix + "HIGH"), Map.of("text", "⚡ Shoshilinch", "callback_data", priorityPrefix + "URGENT")));
        return Map.of("inline_keyboard", rows);
    }
    private void showMenu(long chatId) {
        call("sendMessage", Map.of("chat_id", chatId,
                "text", "Assalomu alaykum! OnTime yordam botidasiz. Murojaat yozish tugmasini bosing yoki savolingizni shu chatga yozing.",
                "reply_markup", Map.of("keyboard", List.of(List.of(Map.of("text", "✉️ Murojaat yozish")),
                        List.of(Map.of("text", "✅ Muammo hal bo‘ldi")),
                        List.of(Map.of("text", "🔗 Hisobni bog‘lash"), Map.of("text", "🏠 Bosh menyu"))), "resize_keyboard", true)));
    }

    private void call(String method, Map<String, Object> body) {
        telegramClient.post().uri("/bot" + botToken + "/" + method).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();
    }
    private boolean isConfigured() { return !botToken.isBlank() && !botUsername.isBlank(); }
    private boolean isSupportChat(long chatId) { return supportChatIds().contains(chatId); }
    private List<Long> supportChatIds() { return java.util.Arrays.stream(supportChatIdsValue.split(",")).map(String::trim).filter(s -> s.matches("[0-9]{1,18}")).map(Long::parseLong).filter(id -> id > 0).toList(); }
    private String randomToken() { byte[] bytes = new byte[24]; RANDOM.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private String displayName(AppUser u) { return (u.getFirstName() + " " + (u.getLastName() == null ? "" : u.getLastName())).trim() + " (@" + u.getUsername() + ")"; }
    private String displayName(JsonNode from) { String full = (from.path("first_name").asText("") + " " + from.path("last_name").asText("")).trim(); String username = from.path("username").asText(""); return username.isBlank() ? (full.isBlank() ? "Telegram foydalanuvchi" : full) : full + " (@" + username + ")"; }
    private String messageText(JsonNode message) { String text = message.path("text").asText(""); return text.isBlank() ? message.path("caption").asText("") : text; }
    private String mediaType(JsonNode m) { if (m.has("photo")) return "PHOTO"; if (m.has("document")) return "DOCUMENT"; if (m.has("video")) return "VIDEO"; if (m.has("voice")) return "VOICE"; return null; }
    private String statusLabel(SupportTicketStatus status) { return switch (status) { case NEW -> "Yangi"; case IN_PROGRESS -> "Jarayonda"; case WAITING_USER -> "User javobi kutilmoqda"; case RESOLVED -> "Hal qilindi"; case CLOSED -> "Yopildi"; }; }
    private String priorityLabel(String priority) { return switch (priority) { case "LOW" -> "Past"; case "HIGH" -> "Yuqori"; case "URGENT" -> "Shoshilinch"; default -> "Oddiy"; }; }
    private String sourceLabel(SupportSource source) { return switch (source) { case USER_APP -> "📱 User ilovasi"; case BUSINESS_PANEL -> "🏢 Biznes paneli"; case GENERAL -> "Bot orqali"; }; }
    private SupportSource parseSource(String value) { try { return SupportSource.valueOf(value == null ? "GENERAL" : value.trim().toUpperCase()); } catch (IllegalArgumentException ignored) { return SupportSource.GENERAL; } }
    private SupportSource supportSourceForUser(AppUser user) { return user.getTelegramSupportSource() == null ? SupportSource.GENERAL : user.getTelegramSupportSource(); }
    private SupportSource supportSourceForChat(long chatId) { return userRepository.findByTelegramChatId(chatId).map(this::supportSourceForUser).orElse(SupportSource.GENERAL); }
    private String shortTicketId(UUID id) { return id.toString().substring(0, 8).toUpperCase(); }
}
