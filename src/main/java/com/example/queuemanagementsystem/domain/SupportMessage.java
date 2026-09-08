package com.example.queuemanagementsystem.domain;

import com.example.queuemanagementsystem.domain.enums.SupportMessageSender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "support_messages") @Getter @Setter
public class SupportMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private SupportMessageSender sender;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(length = 30) private String mediaType;
    private Long telegramMessageId;
}
