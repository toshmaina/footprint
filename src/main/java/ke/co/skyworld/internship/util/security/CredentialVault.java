package ke.co.skyworld.internship.util.security;


import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.util.formatting.XmlUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.security)
 * Created by: oloo
 * On: 8/13/26. 6:45 PM
 * Description: Transparently encrypts credential values stored in conf.xml. Each
 * credential element carries its own "encrypted" attribute (e.g.
 * <password encrypted="true">...</password>)
 * <p>
 * On read: if encrypted="false" (or the attribute is absent), the plaintext is
 * encrypted with AES-256-GCM, the element's text is replaced with
 * base64(iv):base64(ciphertext), and encrypted="true" is set - all in one write via
 * XmlUtils.updateXMLTagWithAttribute. The plaintext just read is returned for this
 * call regardless. If encrypted="true", the stored value is decrypted and returned.
 * <p>
 * conf.xml must already contain an empty <security key_salt=""/> element - see
 * Constants.XML_PATH_TO_KEY_SALT.
 * <p>
 * The AES key is derived via PBKDF2WithHmacSHA256 from a passphrase supplied
 * out-of-band through the SKY_CORE_CONFIG_KEY environment variable. That passphrase
 * must never be stored in conf.xml or committed to source control.
 **/
public final class CredentialVault {

    private static final String MASTER_KEY_ENV_VAR = "SKYWORLD_INVENTORY_CONFIG_KEY";
    private static final String MASTER_KEY_SYSTEM_PROPERTY = "sky.inventory.config.key";
    private static final String ENCRYPTED_ATTRIBUTE = "encrypted";

    private static final String CIPHER_TRANSFORM = "AES/GCM/NoPadding";
    private static final String AES_ALGORITHM = "AES";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int PBKDF2_ITERATIONS = 210_000;
    private static final int AES_KEY_LENGTH_BITS = 256;
    private static final Object LOCK = new Object();
    private static volatile byte[] cachedKey;

    private CredentialVault() {
    }

    /**
     * Resolves the plaintext value at tagPath. Encrypts and persists it in place,
     * setting encrypted="true" on the same element, if it is currently plaintext.
     */
    public static String resolvePlaintext(String tagPath) {
        String raw = XmlUtils.readXMLTag(tagPath).replaceAll("\\s+", "");
        if (isMissingOrBlank(raw)) {
            return raw;
        }
        boolean alreadyEncrypted = "true".equalsIgnoreCase(XmlUtils.readXMLTag(tagPath + "/@" + ENCRYPTED_ATTRIBUTE));
        try {
            byte[] key = resolveKey();
            if (alreadyEncrypted) {
                return decrypt(raw, key);
            }
            String encrypted = encrypt(raw, key);
            if (!XmlUtils.updateXMLTagWithAttribute(tagPath, encrypted, ENCRYPTED_ATTRIBUTE, "true")) {
                throw new IllegalStateException("Failed to persist encrypted value for " + tagPath);
            }
            return raw;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Credential crypto failure for " + tagPath
                    + " - check " + MASTER_KEY_ENV_VAR + " is correct", e);
        }
    }

    private static boolean isMissingOrBlank(String value) {
        return value == null || value.isBlank() || value.equals(Constants.SSA_DELIMITER);
    }

    private static String encrypt(String plaintext, byte[] key) throws GeneralSecurityException {
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, AES_ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(ciphertext);
    }

    private static String decrypt(String encoded, byte[] key) throws GeneralSecurityException {
        int sep = encoded.indexOf(':');
        if (sep < 0) {
            throw new IllegalArgumentException("Malformed encrypted config value: " + encoded);
        }
        byte[] iv = Base64.getDecoder().decode(encoded.substring(0, sep));
        byte[] ciphertext = Base64.getDecoder().decode(encoded.substring(sep + 1));
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, AES_ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private static byte[] resolveKey() throws GeneralSecurityException {
        if (cachedKey != null) {
            return cachedKey;
        }
        synchronized (LOCK) {
            if (cachedKey != null) {
                return cachedKey;
            }
            String passphrase = resolvePassphrase();
            byte[] salt = resolveSalt();
            PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt,
                    PBKDF2_ITERATIONS, AES_KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            cachedKey = factory.generateSecret(spec).getEncoded();
            return cachedKey;
        }
    }

    private static String resolvePassphrase() {
        String fromEnv = System.getenv("SKYWORLD_INVENTORY_CONFIG_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProperty = System.getProperty("MASTER_KEY_SYSTEM_PROPERTY");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        throw new IllegalStateException(
                "No config encryption passphrase found. Set either the "
                        + MASTER_KEY_ENV_VAR + " environment variable or the "
                        + MASTER_KEY_SYSTEM_PROPERTY + " JVM system property. "
                        + "Without one, credentials cannot be read, and the application must not start.");
    }

    private static byte[] resolveSalt() {
        String existing = XmlUtils.readXMLTag(Constants.XML_PATH_TO_KEY_SALT);
        if (!isMissingOrBlank(existing)) {
            return Base64.getDecoder().decode(existing);
        }
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String encoded = Base64.getEncoder().encodeToString(salt);
        if (!XmlUtils.updateXMLTag(Constants.XML_PATH_TO_KEY_SALT, encoded)) {
            throw new IllegalStateException("Failed to persist key salt at "
                    + Constants.XML_PATH_TO_KEY_SALT
                    + " - ensure conf.xml has an empty <security key_salt=\"\"/> element");
        }
        return salt;
    }
}