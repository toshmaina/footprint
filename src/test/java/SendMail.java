import ke.co.skyworld.internship.util.infra.EmailService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class SendMail {

    EmailService emailService = new EmailService();
    Path PATH_TO_WELCOME  = Path.of("/home/jamesmaina/Projects/SKYWORLD_PROJECT/tatua-website-starter/welcome-email.html");
    Path PATH_TO_TRIAL_EXPIRY  = Path.of("/home/jamesmaina/Projects/SKYWORLD_PROJECT/tatua-website-starter/trial-expiration.html");
    Path PATH_TO_NEWSLETTER  = Path.of("/home/jamesmaina/Projects/SKYWORLD_PROJECT/tatua-website-starter/newsletter-email.html");

    String MY_EMAIL = "jamesmaina2003xx@gmail.com";
    String WILLIAM_EMAIL = "ochomoswill@gmail.com";
    @Test
    public void sendToWilliam() throws Exception {
        emailService.doSend(WILLIAM_EMAIL,
                "Welcome Mail Template",
                Files.readString(PATH_TO_WELCOME)
        );
        emailService.doSend(WILLIAM_EMAIL,
                "Trial Expiry Template",
                Files.readString(PATH_TO_TRIAL_EXPIRY)
        );
        emailService.doSend(WILLIAM_EMAIL,
                "Newsletter Email Template",
                Files.readString(PATH_TO_NEWSLETTER)
        );
    }

}
