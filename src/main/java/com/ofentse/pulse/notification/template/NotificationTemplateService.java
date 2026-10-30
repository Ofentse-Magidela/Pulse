package com.ofentse.pulse.notification.template;

import com.ofentse.pulse.notification.exception.TemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

@Service
public class NotificationTemplateService {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateService.class);
    private static final String CACHE_KEY_PREFIX = "pulse:template:";

    private final NotificationTemplateRepo notificationTemplateRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration templateCacheTtl;

    public NotificationTemplateService(NotificationTemplateRepo notificationTemplateRepo,
                                       RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper,
                                       @Value("${pulse.cache.template.ttl:30m}") Duration templateCacheTtl) {
        this.notificationTemplateRepo = notificationTemplateRepo;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.templateCacheTtl = templateCacheTtl;
    }

    public NotificationTemplate getTemplate(String name) {

        String key = CACHE_KEY_PREFIX + name;
        String cachedJson = null;

        try {
            cachedJson = redisTemplate.opsForValue().get(key);
        } catch(DataAccessException e) {
            log.warn("Redis cache lookup failed for template: {}", name, e);
        }

        if (cachedJson != null) {
            try {
                log.debug("Template cache hit: {}", name);
                return objectMapper.readValue(cachedJson, NotificationTemplate.class);
            } catch (JacksonException e) {
                log.warn("Cached template with name {} contains invalid JSON", name, e);
            }
        }

        log.debug("Template cache miss: {}", name);

        NotificationTemplate template = notificationTemplateRepo.findByName(name)
                .orElseThrow(() -> new TemplateNotFoundException(
                        "Template with name: " + name + " not found."
                ));

        try {
            String jsonTemplateBody = objectMapper.writeValueAsString(template);

            redisTemplate.opsForValue().set(
                    key,
                    jsonTemplateBody,
                    templateCacheTtl
            );
        }catch (DataAccessException | JacksonException ex) {
            log.warn("Failed to cache the notification template: {}", name, ex);
        }

        return template;
    }

    public String render(String template, Map<String, String> variables) {

        String renderedTemplate = template;
        for(Map.Entry<String, String> variable : variables.entrySet()) {

            String placeholder = "{{" + variable.getKey() + "}}";
            renderedTemplate = renderedTemplate.replace(placeholder, variable.getValue());
        }

        return renderedTemplate;
    }
}
