package com.rentalplatform.backend.common.upload;

import com.cloudinary.Cloudinary;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService implements StorageService {

    private final Cloudinary cloudinary;

    @Value("${app.upload.max-file-size:10485760}") //10B
    private long maxFileSize;

    @Override
    public String upload(MultipartFile file, String folder){
        validateFile(file);

        try{
            Map<String, Object> options = Map.of(
                    "public_id", UUID.randomUUID().toString(),
                    "asset_folder", folder,
                    "resource_type", "auto",
                    "overwrite", true
            );

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);

            return result.get("secure_url").toString();
        }catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try{
        String publicId = extractPublicId(fileUrl);

        Map<String, Object> options = Map.of (
                "resource_type", "image"
        );

        cloudinary.uploader().destroy(publicId, options);
        } catch (Exception e){
            throw new AppException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    @Override
    public String getUrl(String fileUrl) {
        return fileUrl;
    }

    private void validateFile(MultipartFile file){
        if (file == null || file.isEmpty()){
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > maxFileSize){
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    private String extractPublicId(String fileUrl){
        String marker = "/upload/";
        int index = fileUrl.indexOf(marker);

        if (index == -1){
            throw new AppException(ErrorCode.INVALID_IMAGE_URL);
        }

        String path = fileUrl.substring(index + marker.length());

        // remove version: v1234567890/
        path = path.replaceFirst("^v\\d+/", "");

        //remove extension
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex != -1){
           path = path.substring(0, dotIndex);
        }
        return path;
    }
}
