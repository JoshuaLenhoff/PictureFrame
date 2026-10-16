package de.pictureframe.server.image;

import de.pictureframe.server.device.Device;
import de.pictureframe.server.device.DeviceRepository;
import de.pictureframe.server.user.User;
import de.pictureframe.server.user.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

@Service
public class ImageUploadService
{

    private static final long MAX_FILE_SIZE = 15L * 1024 * 1024;
    private static final long MAX_PIXELS = 40_000_000L;

    private final DeviceRepository deviceRepository;
    private final ImageRepository imageRepository;
    private final DeviceImageRepository deviceImageRepository;
    private final UserRepository userRepository;
    private final Path storagePath;

    private final ImageProcessingService imageProcessingService;

    public ImageUploadService(
            DeviceRepository deviceRepository,
            ImageRepository imageRepository,
            DeviceImageRepository deviceImageRepository,
            UserRepository userRepository,
            ImageProcessingService imageProcessingService,
            @Value("${app.storage.location}") String storageLocation)
    {
        this.deviceRepository = deviceRepository;
        this.imageRepository = imageRepository;
        this.deviceImageRepository = deviceImageRepository;
        this.userRepository = userRepository;
        this.imageProcessingService = imageProcessingService;
        this.storagePath = Path.of(storageLocation).toAbsolutePath().normalize();
    }

    @Transactional
    public UUID upload(ImageUploadForm form, String username) throws IOException {
        MultipartFile file = form.getFile();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bitte ein Bild auswählen.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Das Bild darf maximal 15 MB groß sein.");
        }
        if (form.getDeviceIds() == null || form.getDeviceIds().isEmpty()) {
            throw new IllegalArgumentException("Bitte mindestens einen Bilderrahmen auswählen.");
        }
        if (form.getPushDurationSeconds() < 10 || form.getPushDurationSeconds() > 3600) {
            throw new IllegalArgumentException("Ungültige Push-Dauer.");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("Benutzer nicht gefunden."));
        List<Long> deviceIds = form.getDeviceIds().stream().distinct().toList();
        List<Device> devices = deviceRepository.findAllById(deviceIds);

        if (devices.size() != deviceIds.size() || devices.stream().anyMatch(d -> !d.isEnabled())) {
            throw new IllegalArgumentException("Mindestens ein Bilderrahmen ist ungültig.");
        }

        String contentType;
        String extension;
        try (InputStream input = file.getInputStream(); var imageInput = ImageIO.createImageInputStream(input)) {
            var readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Kein unterstütztes Bildformat.");
            }

            var reader = readers.next();
            try {
                reader.setInput(imageInput);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);

                if (format.equals("jpeg") || format.equals("jpg")) {
                    contentType = "image/jpeg";
                    extension = ".jpg";
                } else if (format.equals("png")) {
                    contentType = "image/png";
                    extension = ".png";
                } else {
                    throw new IllegalArgumentException("Nur JPEG und PNG sind erlaubt.");
                }

                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels <= 0 || pixels > MAX_PIXELS) {
                    throw new IllegalArgumentException("Bildauflösung nicht unterstützt.");
                }

                BufferedImage decoded = reader.read(0);
                if (decoded == null) {
                    throw new IllegalArgumentException("Bild konnte nicht gelesen werden.");
                }
            } finally {
                reader.dispose();
            }
        }

        UUID imageId = UUID.randomUUID();
        Files.createDirectories(storagePath);
        Path tempFile = Files.createTempFile(storagePath, "upload-", ".tmp");
        Path target = storagePath.resolve(imageId + ".jpg");
        boolean saved = false;

        try {
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            this.imageProcessingService.process(tempFile, target);

            Image image = new Image(
                    user,
                    imageId,
                    file.getOriginalFilename(),
                    target.toString(),
                    "image/jpeg",
                    Files.size(target)
            );

            this.imageRepository.save(image);
            for (Device device : devices) {
                DeviceImage deviceImage = new DeviceImage(device, image, form.isPushEnabled(), form.getPushDurationSeconds());
                this.deviceImageRepository.save(deviceImage);
            }

            saved = true;
            return imageId;
        } finally {
            Files.deleteIfExists(tempFile);

            if (!saved) {
                Files.deleteIfExists(target);
            }
        }
    }

}
