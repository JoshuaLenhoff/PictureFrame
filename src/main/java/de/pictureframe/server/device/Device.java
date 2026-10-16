package de.pictureframe.server.device;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "devices")
public class Device
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "last_sync")
    private Instant lastSync;

    protected Device() {}

    public Device(String name, String apiKey) {
        this.name = name;
        this.apiKey = apiKey;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getApiKey() { return apiKey; }
    public boolean isEnabled() { return enabled; }
    public Instant getLastSync() { return lastSync; }

}
