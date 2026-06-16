package com.codewithsam.mailautomator.util;

import com.codewithsam.mailautomator.exception.TemplateRenderException;

import java.util.Map;

public final class TemplateRenderer {

    private TemplateRenderer() {}

    /**
     * Replaces all {@code {{key}}} placeholders in the template with values from the map.
     * Missing keys are replaced with an empty string.
     */
    public static String render(String template, Map<String, String> variables) {
        if (template == null) {
            throw new TemplateRenderException("Template content cannot be null");
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
