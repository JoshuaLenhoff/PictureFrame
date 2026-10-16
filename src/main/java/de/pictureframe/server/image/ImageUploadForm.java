package de.pictureframe.server.image;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ImageUploadForm
{

    @NotNull
    private MultipartFile file;

    @NotEmpty
    private List<Long> deviceIds;

    private boolean pushEnabled;

    private int pushDurationSeconds = 120;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public List<Long> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<Long> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public int getPushDurationSeconds() {
        return pushDurationSeconds;
    }

    public void setPushDurationSeconds(int pushDurationSeconds) {
        this.pushDurationSeconds = pushDurationSeconds;
    }

}
