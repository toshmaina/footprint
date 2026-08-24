package com.skyworld.util.infra;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.skyworld.util.formatting.TemplateEngine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;


public class PDFService {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                    .withZone(ZoneOffset.UTC);

    private static final String LOGO_BASE64;

    static {
        try (InputStream in =
                     PDFService.class.getResourceAsStream("/images/skyworld_logo_small.png")) {

            if (in == null) {
                throw new IllegalStateException("Logo not found");
            }

            LOGO_BASE64 = Base64.getEncoder()
                    .encodeToString(in.readAllBytes());

        } catch (Exception e) {
            throw new RuntimeException("Failed to load logo", e);
        }
    }

    public static void validateXhtml(String html) {
        try {
            DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(html)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Generated XHTML is invalid", e);
        }
    }

    public static String toXhtml(String html) {
        Document doc = Jsoup.parse(html);

        doc.select("[style*='word-break'], [style*='overflow-wrap'], [style*='el-wrap']")
                .forEach(el -> {
                    String currentStyle = el.attr("style");
                    currentStyle = currentStyle.replaceAll("word-break\\s*:\\s*[^;]+;?", "")
                            .replaceAll("overflow-wrap\\s*:\\s*[^;]+;?", "")
                            .replaceAll("el-wrap\\s*:\\s*[^;]+;?", "");

                    currentStyle += " word-wrap: break-word;";
                    el.attr("style", currentStyle);
                });

        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml)
                .charset(java.nio.charset.StandardCharsets.UTF_8);

        return doc.html();
    }

    private String renderTemplate(String name, Map<String, Object> model) {

        Map<String, Object> templateModel = new java.util.LinkedHashMap<>(model);

        templateModel.put("logoBase64", LOGO_BASE64);
        templateModel.put("organizationName", "Sky-Core");

        return TemplateEngine.render(name, templateModel);
    }

    public byte[] htmlToPdf(String html) throws Exception {
        html = toXhtml(html);
        validateXhtml(html);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        }
    }

    private String getStringOrDefault(Map<String, Object> m, String key, String fallback) {
        Object v = m.get(key);
        return (v != null && !v.toString().isBlank()) ? v.toString() : fallback;
    }

    public byte[] generatePdf(String template, Map<String, Object> model) throws Exception {
        return htmlToPdf(renderTemplate(template, model));
    }
}