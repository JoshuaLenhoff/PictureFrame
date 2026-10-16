package de.pictureframe.server.image;

import de.pictureframe.server.device.DeviceRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/images")
public class ImageUploadController
{

    private final DeviceRepository deviceRepository;
    private final ImageUploadService uploadService;

    public ImageUploadController(DeviceRepository deviceRepository, ImageUploadService uploadService) {
        this.deviceRepository = deviceRepository;
        this.uploadService = uploadService;
    }

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        model.addAttribute("devices", deviceRepository.findByEnabledTrueOrderByNameAsc());
        return "images/upload";
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("deviceIds") List<Long> deviceIds,
            @RequestParam(value = "pushEnabled", defaultValue = "false") boolean pushEnabled,
            @RequestParam(value = "pushDurationSeconds", defaultValue = "120") int pushDurationSeconds,
            Principal principal,
            RedirectAttributes redirectAttributes) throws IOException
    {
        ImageUploadForm form = new ImageUploadForm();
        form.setFile(file);
        form.setDeviceIds(deviceIds);
        form.setPushEnabled(pushEnabled);
        form.setPushDurationSeconds(pushDurationSeconds);

        try {
            uploadService.upload(form, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Bild erfolgreich hochgeladen!");
            return "redirect:/images/upload";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/images/upload";
        }
    }

}
