package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.SupportMessage;
import com.example.queuemanagementsystem.domain.SupportTicket;
import com.example.queuemanagementsystem.domain.enums.SupportMessageSender;
import com.example.queuemanagementsystem.domain.enums.SupportTicketPriority;
import com.example.queuemanagementsystem.domain.enums.SupportTicketStatus;
import com.example.queuemanagementsystem.domain.enums.SupportSource;
import com.example.queuemanagementsystem.dto.SupportMessageDto;
import com.example.queuemanagementsystem.dto.SupportTicketDto;
import com.example.queuemanagementsystem.dto.SupportTicketUpdateRequest;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.SupportMessageRepository;
import com.example.queuemanagementsystem.repository.SupportTicketRepository;
import com.example.queuemanagementsystem.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketService {
    private final SupportTicketRepository tickets;
    private final SupportMessageRepository messages;
    private final AppUserRepository users;
    private final CurrentUserService currentUser;

    public SupportTicket recordIncoming(Long chatId, String name, String content, String mediaType, Long telegramMessageId, SupportSource source) {
        AppUser user = users.findByTelegramChatId(chatId).orElse(null);
        SupportSource ticketSource = source == null ? SupportSource.GENERAL : source;
        SupportTicket ticket = tickets.findFirstByTelegramChatIdAndSourceAndStatusInOrderByUpdatedAtDesc(chatId, ticketSource,
                        List.of(SupportTicketStatus.NEW, SupportTicketStatus.IN_PROGRESS, SupportTicketStatus.WAITING_USER))
                .or(() -> tickets.findFirstByTelegramChatIdAndSourceOrderByUpdatedAtDesc(chatId, ticketSource)
                        .filter(last -> last.getStatus() == SupportTicketStatus.RESOLVED)
                        .filter(last -> last.getUpdatedAt().isAfter(Instant.now().minus(24, ChronoUnit.HOURS))))
                .orElseGet(() -> newTicket(chatId, user, name, content, ticketSource));
        if (ticket.getStatus() == SupportTicketStatus.WAITING_USER || ticket.getStatus() == SupportTicketStatus.RESOLVED)
            ticket.setStatus(SupportTicketStatus.IN_PROGRESS);
        addMessage(ticket, SupportMessageSender.USER, content, mediaType, telegramMessageId);
        return tickets.save(ticket);
    }

    public SupportTicket addOperatorReply(UUID ticketId, String content) {
        SupportTicket ticket = require(ticketId);
        if (ticket.getStatus() == SupportTicketStatus.CLOSED)
            throw new IllegalStateException("Yopilgan murojaatga javob berib bo'lmaydi");
        ticket.setStatus(SupportTicketStatus.WAITING_USER);
        addMessage(ticket, SupportMessageSender.OPERATOR, content, null, null);
        return tickets.save(ticket);
    }

    public SupportTicket update(UUID ticketId, SupportTicketUpdateRequest request) {
        SupportTicket ticket = require(ticketId);
        if (request.status() != null && !request.status().isBlank())
            ticket.setStatus(SupportTicketStatus.valueOf(request.status().trim().toUpperCase()));
        if (request.priority() != null && !request.priority().isBlank())
            ticket.setPriority(SupportTicketPriority.valueOf(request.priority().trim().toUpperCase()));
        return tickets.save(ticket);
    }

    public boolean resolveForTelegramChat(Long chatId, SupportSource source) {
        return tickets.findFirstByTelegramChatIdAndSourceAndStatusInOrderByUpdatedAtDesc(chatId, source == null ? SupportSource.GENERAL : source,
                        List.of(SupportTicketStatus.NEW, SupportTicketStatus.IN_PROGRESS, SupportTicketStatus.WAITING_USER))
                .map(ticket -> {
                    ticket.setStatus(SupportTicketStatus.RESOLVED);
                    tickets.save(ticket);
                    return true;
                }).orElse(false);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketDto> adminList(String status, String q, Pageable pageable) {
        SupportTicketStatus parsed = status == null || status.isBlank() ? null : SupportTicketStatus.valueOf(status.toUpperCase());
        return tickets.search(parsed, q == null ? "" : q.trim().toLowerCase(), pageable).map(this::summary);
    }

    @Transactional(readOnly = true)
    public List<SupportTicketDto> operatorTickets(SupportTicketStatus status) {
        return tickets.search(status, "", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt")))
                .getContent().stream().map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public SupportTicketDto adminGet(UUID id) {
        return detail(require(id));
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketDto> mine(Pageable pageable) {
        return tickets.findByRequester_Id(currentUser.getCurrentUserId(), pageable).map(this::summary);
    }

    @Transactional(readOnly = true)
    public SupportTicketDto mineGet(UUID id) {

        SupportTicket t = require(id);
        if (t.getRequester() == null || !t.getRequester().getId().equals(currentUser.getCurrentUserId()))
            throw new org.springframework.security.access.AccessDeniedException("Ruxsat yo'q");
        return detail(t);
    }

    public SupportTicket require(UUID id) {
        return tickets.findById(id).orElseThrow(() -> new IllegalArgumentException("Murojaat topilmadi"));
    }

    /** Operator kartasidagi qisqa ticket raqamini to‘liq UUIDga xavfsiz moslaydi. */
    @Transactional(readOnly = true)
    public SupportTicket requireByShortId(String shortId) {
        if (shortId == null || !shortId.matches("[0-9a-fA-F]{8}"))
            throw new IllegalArgumentException("Ticket ID noto‘g‘ri");
        List<SupportTicket> found = tickets.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(ticket -> ticket.getId().toString().regionMatches(true, 0, shortId, 0, shortId.length()))
                .limit(2).toList();
        if (found.size() != 1) throw new IllegalArgumentException("Ticket topilmadi");
        return found.get(0);
    }

    private SupportTicket newTicket(Long chatId, AppUser user, String name, String content, SupportSource source) {
        SupportTicket t = new SupportTicket();
        t.setTelegramChatId(chatId);
        t.setRequester(user);
        t.setRequesterName(name == null || name.isBlank() ? "Telegram foydalanuvchi" : name);
        t.setSubject(shorten(content));
        t.setSource(source);
        return tickets.save(t);
    }

    private void addMessage(SupportTicket t, SupportMessageSender sender, String content, String media, Long telegramId) {
        // A new message must move the ticket to the top of operator lists even when its status is unchanged.
        t.setUpdatedAt(Instant.now());
        SupportMessage m = new SupportMessage();
        m.setTicket(t);
        m.setSender(sender);
        m.setContent(content == null || content.isBlank() ? "Fayl yoki media yuborildi" : content);
        m.setMediaType(media);
        m.setTelegramMessageId(telegramId);
        messages.save(m);
    }

    private String shorten(String value) {
        String v = value == null || value.isBlank() ? "Telegram murojaati" : value.trim();
        return v.length() > 200 ? v.substring(0, 197) + "..." : v;
    }

    private SupportTicketDto summary(SupportTicket t) {
        return new SupportTicketDto(t.getId(), t.getRequesterName(), t.getRequester() == null ? null : t.getRequester().getUsername(), t.getSubject(), t.getSource().name(), t.getStatus().name(), t.getPriority().name(), t.getCreatedAt(), t.getUpdatedAt(), List.of());
    }

    private SupportTicketDto detail(SupportTicket t) {
        return new SupportTicketDto(t.getId(), t.getRequesterName(), t.getRequester() == null ? null : t.getRequester().getUsername(), t.getSubject(), t.getSource().name(), t.getStatus().name(), t.getPriority().name(), t.getCreatedAt(), t.getUpdatedAt(), messages.findByTicket_IdOrderByCreatedAtAsc(t.getId()).stream().map(m -> new SupportMessageDto(m.getId(), m.getSender().name(), m.getContent(), m.getMediaType(), m.getCreatedAt())).toList());
    }
}
