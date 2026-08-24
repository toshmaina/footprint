package com.skyworld.util.formatting;

import com.skyworld.util.logging.Log;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.StringWriter;
import java.util.Map;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.formatting)
 * Created by: oloo
 * On: 8/12/26. 5:42 PM
 * Description:
 **/

public class TemplateEngine {

    private static final Configuration CFG;

    static {
        CFG = new Configuration(Configuration.VERSION_2_3_33);
        CFG.setClassForTemplateLoading(TemplateEngine.class, "/templates");
        CFG.setDefaultEncoding("UTF-8");
        CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        CFG.setLogTemplateExceptions(false);
        CFG.setTemplateUpdateDelayMilliseconds(5_000);
    }

    private TemplateEngine() {
    }

    public static String render(String templatePath, Map<String, Object> model) {
        try {
            Template template = CFG.getTemplate(templatePath);
            StringWriter out = new StringWriter();
            template.process(model, out);
            return out.toString();
        } catch (Exception e) {
            Log.error(TemplateEngine.class, "render",
                    "Failed to render template: " + templatePath, e);
            throw new RuntimeException("Template rendering failed: " + templatePath, e);
        }
    }
}