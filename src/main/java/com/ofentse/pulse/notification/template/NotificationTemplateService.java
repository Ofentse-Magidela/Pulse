package com.ofentse.pulse.notification.template;

import com.ofentse.pulse.notification.exception.TemplateNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationTemplateService {

    private final NotificationTemplateRepo notificationTemplateRepo;

    public NotificationTemplateService(NotificationTemplateRepo notificationTemplateRepo) {
        this.notificationTemplateRepo = notificationTemplateRepo;
    }

    public NotificationTemplate getTemplate(String name) {
        return notificationTemplateRepo.findByName(name)
                .orElseThrow(() -> new TemplateNotFoundException(
                        "Template with name: " + name + " not found."
                ));
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
