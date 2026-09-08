package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.repository.*;
import com.example.queuemanagementsystem.security.CurrentUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TelegramSupportServiceTest {
    @Test void startShowsMenuAndGuestCanSendWithoutLink() throws Exception {
        var service = new TelegramSupportService(mock(AppUserRepository.class), mock(TelegramLinkTokenRepository.class),
                mock(CurrentUserService.class), mock(SupportTicketService.class), mock(TransactionTemplate.class));
        var client = mock(RestClient.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(service, "telegramClient", client);
        ReflectionTestUtils.setField(service, "supportChatIdsValue", "555");
        ReflectionTestUtils.setField(service, "botToken", "999:test");
        var mapper = new ObjectMapper();
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(service, "handleMessage",
                mapper.readTree("{\"chat\":{\"id\":123,\"type\":\"private\"},\"text\":\"/start\"}")));
        verify(client, times(1)).post();
    }

    @Test void untrustedReplyCannotSendToArbitraryUser() throws Exception {
        var service = new TelegramSupportService(mock(AppUserRepository.class), mock(TelegramLinkTokenRepository.class),
                mock(CurrentUserService.class), mock(SupportTicketService.class), mock(TransactionTemplate.class));
        var client = mock(RestClient.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(service, "telegramClient", client);
        ReflectionTestUtils.setField(service, "supportChatIdsValue", "555");
        ReflectionTestUtils.setField(service, "botToken", "999:test");
        ReflectionTestUtils.invokeMethod(service, "handleMessage", new ObjectMapper().readTree(
                "{\"chat\":{\"id\":555,\"type\":\"private\"},\"text\":\"answer\",\"reply_to_message\":{\"from\":{\"id\":111},\"text\":\"Murojaat #123\"}}"));
        verifyNoInteractions(client);
    }
}
