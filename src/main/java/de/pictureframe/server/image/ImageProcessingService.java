package de.pictureframe.server.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import org.springframework.stereotype.Service;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Iterator;

@Service
public class ImageProcessingService
{

    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;
    private static final float JPEG_QUALITY = 0.85f;

    public void process(Path source, Path target) throws IOException {
        int orientation = readOrientation(source);
        BufferedImage original = ImageIO.read(source.toFile());

        if (original == null) {
            throw new IOException("Bild konnte nicht gelesen werden.");
        }

        BufferedImage rotated = applyOrientation(original, orientation);

        double scale = Math.min(1.0, Math.min((double) MAX_WIDTH / rotated.getWidth(), (double) MAX_HEIGHT / rotated.getHeight()));
        int width = Math.max(1, (int) Math.round(rotated.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(rotated.getHeight() * scale));

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();

        try {
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(rotated, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }

        writeJpeg(result, target);
    }

    private int readOrientation(Path source) {
        try (InputStream input = Files.newInputStream(source)) {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (Exception ignored) {

        }
        return 1;
    }

    private BufferedImage applyOrientation(BufferedImage image, int orientation) {
        int w = image.getWidth();
        int h = image.getHeight();

        int targetW = orientation >= 5 && orientation <= 8 ? h : w;
        int targetH = orientation >= 5 && orientation <= 8 ? w : h;

        BufferedImage result = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        try {
            switch (orientation) {
                case 2 -> {
                    g.translate(w, 0);
                    g.scale(-1, 1);
                }
                case 3 -> {
                    g.translate(w, h);
                    g.rotate(Math.PI);
                }
                case 4 -> {
                    g.translate(0, h);
                    g.scale(1, -1);
                }
                case 5 -> {
                    g.rotate(Math.PI / 2);
                    g.scale(1, -1);
                }
                case 6 -> {
                    g.translate(h, 0);
                    g.rotate(Math.PI / 2);
                }
                case 7 -> {
                    g.translate(h, w);
                    g.rotate(Math.PI / 2);
                    g.scale(-1, 1);
                }
                case 8 -> {
                    g.translate(0, w);
                    g.rotate(-Math.PI / 2);
                }
            }
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }

        return result;
    }

    private void writeJpeg(BufferedImage image, Path target) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");

        if (!writers.hasNext()) {
            throw new IOException("Kein JPEG-Writer vorhanden.");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(Files.newOutputStream(target))) {
            writer.setOutput(output);

            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(JPEG_QUALITY);

            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
    }

}
