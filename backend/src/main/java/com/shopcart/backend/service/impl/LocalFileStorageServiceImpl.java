package com.shopcart.backend.service.impl;

import com.shopcart.backend.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    @Value("${app.upload.allowed-content-types:image/jpeg,image/png,image/webp}")
    private String allowedContentTypesConfig;

    @Value("${app.upload.max-file-size-bytes:5242880}")
    private long maxFileSizeBytes;

    private Set<String> allowedContentTypes;
    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        this.allowedContentTypes = Arrays.stream(
                allowedContentTypesConfig.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());


        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory at " + this.fileStorageLocation, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file");
        }

        // 1. Validate MIME Type
        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypesConfig.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type: " + contentType + ". Allowed: " + allowedContentTypesConfig);
        }

        // 2. Detect actual image format using file signature
        try(InputStream inputStream = file.getInputStream()){
            byte[] header = inputStream.readNBytes(12);
            String extension = detectImageExtension(header);

            if(extension == null) {
                throw new IllegalArgumentException("Unsupported or invalid image content");
            }

            // 3. Check detected content type against allowed types
            String detectedContentType = switch (extension) {
                case ".jpg" -> "image/jpeg";
                case ".png" -> "image/png";
                case ".webp" -> "image/webp";
                default -> throw new IllegalArgumentException("Unsupported image format");
            };

            if(!allowedContentTypes.contains(detectedContentType)) {
                throw new IllegalArgumentException("Image type not allowed: " + detectedContentType);
            }

            // Also reject a MIME type that contradicts the file signature
            String reportedContentType = file.getContentType();

            if(reportedContentType == null || !detectedContentType.equalsIgnoreCase(reportedContentType.trim())){
                throw new IllegalArgumentException("Uploaded file content type does not match its content");
            }

            // 4. Generate a unique filename
            String uniqueFileName = UUID.randomUUID() + extension;

            // 5. Resolve and validate destination path
            Path targetLocation = fileStorageLocation
                    .resolve(uniqueFileName)
                    .normalize();

            if(!targetLocation.startsWith(fileStorageLocation)){
                throw new SecurityException("Cannot store file outside upload directory");
            }

            // 6. Save file without overwriting existing files
            try(InputStream fileStream = file.getInputStream()) {
                Files.copy(fileStream, targetLocation);
            } catch (IOException ex) {
                // Clean up a partially created file if possible
                try{
                    Files.deleteIfExists(targetLocation);
                } catch (IOException cleanupEx) {
                    ex.addSuppressed(cleanupEx);
                }
                throw ex;
            }

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store uploaded file", ex);
        }
    }

    @Override
    public void deleteFile(String fileName){
        if(fileName == null || fileName.isBlank()){
            return;
        }
        try {
            Path suppliedPath = Paths.get(fileName);

            //Accept only a single filename, not a directory path
            if(suppliedPath.isAbsolute() || suppliedPath.getNameCount() != 1 || fileName.contains("/") || fileName.contains("\\")){
                throw new IllegalArgumentException("Invalid file name");
            }

            Path targetLocation = fileStorageLocation.resolve(suppliedPath).normalize();

            if(!targetLocation.startsWith(fileStorageLocation)) {
                throw new SecurityException("Cannot delete file outside upload directory");
            }

            Files.deleteIfExists(targetLocation);
        } catch (IOException ex) {
            log.warn("Could not delete uploaded file: {}", fileName, ex);
        }
    }

    private String detectImageExtension(byte[] header) {
        // JPEG signature: FF D8 FF
        if(header.length >= 3 &&
                (header[0] & 0xFF) == 0XFF &&
                (header[1] & 0xFF) == 0xD8 &&
                (header[2] & 0XFF) == 0xFF) {
            return ".jpg";
        }

        // PNG signature
        if(header.length >= 8 &&
                (header[0] & 0xFF) == 0x89 &&
                header[1] == 0x50 &&
                header[2] == 0x4E &&
                header[3] == 0x47 &&
                header[4] == 0x0D &&
                header[5] == 0x0A &&
                header[6] == 0x1A &&
                header[7] == 0x0A){
            return ".png";
        }

        // WebP signature: RIFF ....WEBP
        if(header.length >= 12 &&
                header[0] == 'R' &&
                header[1] == 'I' &&
                header[2] == 'F' &&
                header[3] == 'F' &&
                header[8] == 'W' &&
                header[9] == 'E' &&
                header[10] == 'B' &&
                header[11] == 'P'
        ) {
            return ".webp";
        }

        return  null;
    }
}
