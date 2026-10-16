package de.pictureframe.server.image;

import de.pictureframe.server.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "images")
public class Image
{

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Image() {}

    public Image(User userId, String fileName, String filePath, String contentType, long fileSize) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public UUID getId() { return id; }
    public User getUserId() { return userId; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }

}
