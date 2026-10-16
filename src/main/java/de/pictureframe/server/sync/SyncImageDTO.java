package de.pictureframe.server.sync;

import java.util.UUID;

public record SyncImageDTO(
        UUID id,
        String fileName,
        long fileSize,
        boolean pushEnabled,
        int pushDurationSeconds
) {}
