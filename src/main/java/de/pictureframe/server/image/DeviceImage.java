package de.pictureframe.server.image;

import de.pictureframe.server.device.Device;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "device_images", uniqueConstraints = @UniqueConstraint(columnNames = {"device_id", "image_id"}))
public class DeviceImage
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name = "downloaded_at")
    private Instant downloadedAt;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled = false;

    @Column(name = "push_duration_seconds", nullable = false)
    private int pushDurationSeconds = 120;

    protected DeviceImage() {}

    public DeviceImage(Device device, Image image, boolean pushEnabled, int pushDurationSeconds) {
        this.device = device;
        this.image = image;
        this.pushEnabled = pushEnabled;
        this.pushDurationSeconds = pushDurationSeconds;
    }

    public Long getId() { return id; }
    public Device getDevice() { return device; }
    public Image getImage() { return image; }
    public Instant getDownloadedAt() { return downloadedAt; }
    public boolean isPushEnabled() { return pushEnabled; }
    public int getPushDurationSeconds() { return pushDurationSeconds; }
    public void setDownloadedAt(Instant downloadedAt) { this.downloadedAt = downloadedAt; }

}
