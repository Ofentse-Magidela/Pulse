package com.ofentse.pulse.notification.template;

import com.ofentse.pulse.notification.exception.TemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepo notificationTemplateRepo;

    @InjectMocks
    private NotificationTemplateService service;

    private NotificationTemplate template;
    private Map<String, String> variables;

    @BeforeEach
    void setup() {
        template = new NotificationTemplate();
        template.setName("TEST_TEMPLATE");
        template.setSubject("Test subject");
        template.setBody("Hello {{name}}");

        variables = Map.of(
                "name", "Test User"
        );
    }

    @Nested
    @DisplayName("GetTemplate")
    class GetTemplate {

        @Test
        @DisplayName("Returns template when template exists")
        void getTemplate_ReturnsTemplate_WhenTemplateExists() {

            when(notificationTemplateRepo.findByName("TEST_TEMPLATE"))
                    .thenReturn(Optional.of(template));

            NotificationTemplate result = service.getTemplate("TEST_TEMPLATE");

            assertEquals("TEST_TEMPLATE", result.getName());
            assertEquals("Test subject", result.getSubject());
            assertEquals("Hello {{name}}", result.getBody());

            verify(notificationTemplateRepo).findByName("TEST_TEMPLATE");
        }

        @Test
        @DisplayName("Throws exception when template does not exist")
        void getTemplate_ThrowsException_WhenTemplateDoesNotExist() {

            when(notificationTemplateRepo.findByName("TEST_TEMPLATE"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    TemplateNotFoundException.class,
                    () -> service.getTemplate("TEST_TEMPLATE")
            );

            verify(notificationTemplateRepo).findByName("TEST_TEMPLATE");
        }
    }

    @Nested
    @DisplayName("Render")
    class Render {

        @Test
        @DisplayName("Replaces template variables")
        void render_ReplacesVariables_WhenVariablesAreProvided() {

            String result = service.render(
                    "Hello {{name}}",
                    variables
            );

            assertEquals("Hello Test User", result);
        }
    }
}