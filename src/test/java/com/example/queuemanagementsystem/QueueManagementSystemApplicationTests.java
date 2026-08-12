package com.example.queuemanagementsystem;

import com.example.queuemanagementsystem.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class QueueManagementSystemApplicationTests {

    @Autowired
    private BusinessService businessService;

    @Test
    void contextLoads() {
    }

    @Test
    void businessAggregateSortsDoNotAppendVirtualPropertiesToQuery() {
        assertDoesNotThrow(() -> businessService.findPublic(
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "rating"))
        ));

        assertDoesNotThrow(() -> businessService.findPublic(
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "reviews"))
        ));

        assertDoesNotThrow(() -> businessService.findAll(
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "rating"))
        ));
    }

}
