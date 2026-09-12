package com.ofentse.pulse.notification.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationTemplateRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationTemplateRepo notificationTemplateRepo;

    private NotificationTemplate template;

    @BeforeEach
    void setup() {
        template = new NotificationTemplate();

        template.setName("TEST_TEMPLATE");
        template.setChannel("EMAIL");
        template.setSubject("Test subject");
        template.setBody("Hello {{name}}");
    }

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("Returns template when template exists")
        void findByName_ReturnsTemplate_WhenTemplateExists() {

            entityManager.persist(template);
            entityManager.flush();

            Optional<NotificationTemplate> result =
                    notificationTemplateRepo.findByName("TEST_TEMPLATE");

            assertTrue(result.isPresent());
            assertEquals("TEST_TEMPLATE", result.get().getName());
            assertEquals("EMAIL", result.get().getChannel());
            assertEquals("Test subject", result.get().getSubject());
            assertEquals("Hello {{name}}", result.get().getBody());
        }

        @Test
        @DisplayName("Returns empty when template does not exist")
        void findByName_ReturnsEmpty_WhenTemplateDoesNotExist() {

            Optional<NotificationTemplate> result =
                    notificationTemplateRepo.findByName("MISSING_TEMPLATE");

            assertTrue(result.isEmpty());
        }
    }

}