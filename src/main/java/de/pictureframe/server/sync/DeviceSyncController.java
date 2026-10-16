package de.pictureframe.server.sync;

import de.pictureframe.server.device.Device;
import de.pictureframe.server.image.DeviceImage;
import de.pictureframe.server.image.DeviceImageRepository;

import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/device")
public class DeviceSyncController
{

    private final DeviceImageRepository deviceImageRepository;

    public DeviceSyncController(DeviceImageRepository deviceImageRepository) {
        this.deviceImageRepository = deviceImageRepository;
    }

    @GetMapping("/sync")
    public SyncResponse sync(@AuthenticationPrincipal Device device) {
        List<SyncImageDTO> images = this.deviceImageRepository
                        .findByDeviceIdAndDownloadedAtIsNull(device.getId())
                        .stream()
                        .map(this::toDto)
                        .toList();
        return new SyncResponse(images);
    }

    private SyncImageDTO toDto(DeviceImage deviceImage) {
        var image = deviceImage.getImage();

        return new SyncImageDTO(
                image.getId(),
                image.getFileName(),
                image.getFileSize(),
                deviceImage.isPushEnabled(),
                deviceImage.getPushDurationSeconds()
        );
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<Resource> downloadImage(@AuthenticationPrincipal Device device, @PathVariable UUID imageId) throws IOException {
        DeviceImage deviceImage = this.deviceImageRepository.findByDeviceIdAndImageId(device.getId(), imageId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Path path = Path.of(deviceImage.getImage().getFilePath());

        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(Files.size(path))
                .body(resource);
    }


    @PostMapping("/images/{imageId}/confirm")
    @Transactional
    public ResponseEntity<Void> confirmDownload(@AuthenticationPrincipal Device device, @PathVariable UUID imageId) {
        DeviceImage deviceImage = this.deviceImageRepository.findByDeviceIdAndImageId(device.getId(), imageId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (deviceImage.getDownloadedAt() == null) {
            deviceImage.setDownloadedAt(Instant.now());
        }

        return ResponseEntity.noContent().build();
    }

}
