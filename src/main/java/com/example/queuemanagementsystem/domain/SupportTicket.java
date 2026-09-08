package com.example.queuemanagementsystem.domain;

import com.example.queuemanagementsystem.domain.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
public class SupportTicket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "requester_id")
    private AppUser requester;

    @Column(nullable = false)
    private Long telegramChatId;

    @Column(length = 160)
    private String requesterName;

    @Column(nullable = false, length = 200)
    private String subject;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private SupportSource source = SupportSource.GENERAL;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private SupportTicketStatus status = SupportTicketStatus.NEW;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private SupportTicketPriority priority = SupportTicketPriority.NORMAL;
}
