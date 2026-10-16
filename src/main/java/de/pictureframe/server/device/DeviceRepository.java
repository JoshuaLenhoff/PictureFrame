package de.pictureframe.server.device;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>
{

    List<Device> findByEnabledTrueOrderByNameAsc();

    Optional<Device> findByApiKey(String apiKey);

}
