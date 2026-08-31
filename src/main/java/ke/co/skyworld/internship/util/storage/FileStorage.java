package ke.co.skyworld.internship.util.storage;


import ke.co.skyworld.internship.config.Constants;
import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


public class FileStorage {

    private static final Tika TIKA = new Tika();

    public static StoredFile store(
            InputStream stream,
            String originalName,
            long sizeBytes
    ) throws Exception {
        Path uploadDir = Paths.get(Constants.getUploadDir());
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String extension = "";
        int i = originalName.lastIndexOf('.');
        if (i > 0) {
            extension = originalName.substring(i);
        }

        String storedName = UUID.randomUUID() + extension;
        Path targetPath = uploadDir.resolve(storedName);
        Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);

        String mimeType = TIKA.detect(targetPath);
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }

        return new StoredFile(originalName, storedName, mimeType, sizeBytes);
    }

    public static void delete(String storedName) throws IOException {
        if (storedName == null || storedName.isBlank()) return;
        Path filePath = Paths.get(Constants.getUploadDir()).resolve(storedName);
        Files.deleteIfExists(filePath);
    }

    public static byte[] read(String storedName) throws IOException {
        Path filePath = Paths.get(Constants.getUploadDir()).resolve(storedName);
        return Files.readAllBytes(filePath);
    }

    public record StoredFile(String originalName, String storedName, String mimeType, long sizeBytes) {
    }
}
