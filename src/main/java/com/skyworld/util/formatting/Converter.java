package com.skyworld.util.formatting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.formatting)
 * Created by: oloo
 * On: 8/12/26. 5:29 PM
 * Description:
 **/

public class Converter {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"
    );
    private static final int MAX_EMAIL_LENGTH = 254;

    private Converter() {
    }

    private static ObjectMapper jsonMapper() {
        return MapperHolder.INSTANCE.jsonMapper;
    }

    private static XmlMapper xmlMapper() {
        return MapperHolder.INSTANCE.xmlMapper;
    }

    public static String toJson(Object obj) {
        try {
            return jsonMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ConverterException("JSON serialization failed", e);
        }
    }

    public static String toXml(Object obj) {
        try {
            return xmlMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ConverterException("XML serialization failed", e);
        }
    }

    public static String toXhtml(String html) {
        Document doc = Jsoup.parse(html);

        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml)
                .charset(java.nio.charset.StandardCharsets.UTF_8);

        return doc.html();
    }


    public static ObjectMapper getObjectMapper(String contentType) {
        if (contentType == null) {
            return xmlMapper();
        }

        if ("application/json".equals(contentType)) {
            return jsonMapper();
        }
        return xmlMapper();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static String normalizeEmail(String email) {
        return email != null ? email.toLowerCase().trim() : null;
    }

    private enum MapperHolder {
        INSTANCE;

        final ObjectMapper jsonMapper;
        final XmlMapper xmlMapper;

        MapperHolder() {
            jsonMapper = new ObjectMapper();
            jsonMapper.registerModule(new JavaTimeModule());
            jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            jsonMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
            jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            jsonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

            JacksonXmlModule xmlModule = new JacksonXmlModule();
            xmlModule.setDefaultUseWrapper(false);
            xmlMapper = new XmlMapper(xmlModule);
            xmlMapper.registerModule(new JavaTimeModule());
            xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            xmlMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            xmlMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        }
    }


    public static class ConverterException extends RuntimeException {
        public ConverterException(String message) {
            super(message);
        }

        public ConverterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
