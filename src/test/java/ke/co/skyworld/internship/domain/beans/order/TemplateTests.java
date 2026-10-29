package ke.co.skyworld.internship.domain.beans.order;

import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.util.formatting.TemplateEngine;
import ke.co.skyworld.internship.util.logging.Log;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TemplateTests {

    private static String getOutputPath() {
        String output = "tmp/templates";
        try {
            Files.createDirectories(Paths.get(output));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    @Test
    public void testOTPTemplate() {
        String name = "James";
        String otp = "12345";

        Map<String, Object> model = Map.of(
                "name", name,
                "otpCode", otp,
                "otpExpiryMinutes", Constants.getOtpTtlMinutes()
        );

        String output = TemplateEngine.render("/mail/verification-otp.ftl", model);


        // Create a temporary HTML file in /tmp
        Path tempFile;
        try {
            tempFile = Files.createTempFile(
                    Path.of(getOutputPath()),
                    "template-output-",
                    ".html");

            // Write the rendered HTML to the temporary file
            Files.writeString(
                    tempFile,
                    output,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Log.info(
                TemplateEngine.class,
                "render",
                "Rendered template saved to: " + tempFile
        );
    }
}
