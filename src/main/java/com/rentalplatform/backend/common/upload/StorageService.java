package com.rentalplatform.backend.common.upload;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(MultipartFile file, String folder);

    void delete (String fileUrl);

    String getUrl(String fileUrl);
}
