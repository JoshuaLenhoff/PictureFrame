package de.pictureframe.server.sync;

import java.util.List;

public record SyncResponse(List<SyncImageDTO> images) {}
