package de.pictureframe.server.image;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceImageRepository extends JpaRepository<DeviceImage, Long>
{

    @EntityGraph(attributePaths = {"image"})
    List<DeviceImage> findByDeviceIdAndDownloadedAtIsNull(Long deviceId);

    @EntityGraph(attributePaths = {"image"})
    Optional<DeviceImage> findByDeviceIdAndImageId(Long deviceId, UUID imageId);

}
