package com.ofentse.pulse.notification.template;

import com.ofentse.pulse.notification.exception.TemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepo notificationTemplateRepo;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    private NotificationTemplateService service;
    private NotificationTemplate template;
    private Map<String, String> variables;

    private static final String TEMPLATE_NAME = "TEST_TEMPLATE";
    private static final String CACHE_KEY = "pulse:template:" + TEMPLATE_NAME;
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @BeforeEach
    void setup() {
        service = new NotificationTemplateService(
                notificationTemplateRepo,
                redisTemplate,
                objectMapper,
                CACHE_TTL
        );

        template = new NotificationTemplate();
        template.setName(TEMPLATE_NAME);
        template.setSubject("Test subject");
        template.setBody("Hello {{name}}");

        variables = Map.of("name", "Test User");
    }

    @Nested
    @DisplayName("GetTemplate")
    class GetTemplate {

        @Test
        @DisplayName("Returns cached template when cache contains valid JSON")
        void getTemplate_ReturnsCachedTemplate_WhenCacheHit() {

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_KEY)).thenReturn("cached-json");
            when(objectMapper.readValue("cached-json", NotificationTemplate.class))
                    .thenReturn(template);

            NotificationTemplate result = service.getTemplate(TEMPLATE_NAME);

            assertEquals(TEMPLATE_NAME, result.getName());
            verify(notificationTemplateRepo, never()).findByName(TEMPLATE_NAME);
            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("Fetches and caches template when cache misses")
        void getTemplate_FetchesFromDatabase_WhenCacheMiss() {

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_KEY)).thenReturn(null);
            when(notificationTemplateRepo.findByName(TEMPLATE_NAME))
                    .thenReturn(Optional.of(template));
            when(objectMapper.writeValueAsString(template)).thenReturn("template-json");

            NotificationTemplate result = service.getTemplate(TEMPLATE_NAME);

            assertEquals(TEMPLATE_NAME, result.getName());

            verify(notificationTemplateRepo).findByName(TEMPLATE_NAME);
            verify(valueOperations).set(CACHE_KEY, "template-json", CACHE_TTL);
        }

        @Test
        @DisplayName("Throws exception when template does not exist")
        void getTemplate_ThrowsException_WhenTemplateDoesNotExist() {

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_KEY)).thenReturn(null);
            when(notificationTemplateRepo.findByName(TEMPLATE_NAME))
                    .thenReturn(Optional.empty());

            assertThrows(
                    TemplateNotFoundException.class,
                    () -> service.getTemplate(TEMPLATE_NAME)
            );

            verify(notificationTemplateRepo).findByName(TEMPLATE_NAME);
            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("Falls back to database when Redis lookup fails")
        void getTemplate_FallsBackToDatabase_WhenRedisLookupFails() {

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_KEY))
                    .thenThrow(new DataAccessResourceFailureException("Redis unavailable"));

            when(notificationTemplateRepo.findByName(TEMPLATE_NAME))
                    .thenReturn(Optional.of(template));
            when(objectMapper.writeValueAsString(template)).thenReturn("template-json");

            NotificationTemplate result = service.getTemplate(TEMPLATE_NAME);

            assertEquals(TEMPLATE_NAME, result.getName());
            verify(notificationTemplateRepo).findByName(TEMPLATE_NAME);
        }

        @Test
        @DisplayName("Removes invalid cached JSON and reloads template from database")
        void getTemplate_RemovesInvalidCache_WhenCachedJsonIsCorrupted() {

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_KEY)).thenReturn("invalid-json");
            when(objectMapper.readValue("invalid-json", NotificationTemplate.class))
                    .thenThrow(mock(JacksonException.class));

            when(notificationTemplateRepo.findByName(TEMPLATE_NAME))
                    .thenReturn(Optional.of(template));
            when(objectMapper.writeValueAsString(template)).thenReturn("valid-json");

            NotificationTemplate result = service.getTemplate(TEMPLATE_NAME);

            assertEquals(TEMPLATE_NAME, result.getName());

            verify(redisTemplate).delete(CACHE_KEY);
            verify(notificationTemplateRepo).findByName(TEMPLATE_NAME);
            verify(valueOperations).set(CACHE_KEY, "valid-json", CACHE_TTL);
        }
    }

    @Nested
    @DisplayName("Render")
    class Render {

        @Test
        @DisplayName("Replaces template variables")
        void render_ReplacesVariables_WhenVariablesAreProvided() {

            String result = service.render("Hello {{name}}", variables);

            assertEquals("Hello Test User", result);
        }
    }

}