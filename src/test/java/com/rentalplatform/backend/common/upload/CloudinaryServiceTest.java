package com.rentalplatform.backend.common.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CloudinaryServiceTest {
    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                cloudinaryService,
                "maxFileSize",
                10 * 1024 * 1024L
        );
    }

    @Test
    @DisplayName("Should upload file successfully")
    void shouldUploadSuccessfully() throws Exception {

        // Arrange
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenReturn("data".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(
                        Map.of(
                                "secure_url",
                                "https://cloudinary.com/image.jpg"
                        )
                );

        // Act
        String result =
                cloudinaryService.upload(
                        file,
                        "avatars"
                );

        // Assert
        assertEquals(
                "https://cloudinary.com/image.jpg",
                result
        );

        verify(uploader)
                .upload(any(byte[].class), anyMap());
    }

    @Test
    @DisplayName("Should throw when file is null")
    void shouldThrowWhenFileIsNull() {

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> cloudinaryService.upload(
                                null,
                                "avatars"
                        )
                );

        assertEquals(
                ErrorCode.FILE_EMPTY,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw when file is empty")
    void shouldThrowWhenFileIsEmpty() {

        when(file.isEmpty()).thenReturn(true);

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> cloudinaryService.upload(
                                file,
                                "avatars"
                        )
                );

        assertEquals(
                ErrorCode.FILE_EMPTY,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw when file exceeds max size")
    void shouldThrowWhenFileTooLarge() {

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(20 * 1024 * 1024L);

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> cloudinaryService.upload(
                                file,
                                "avatars"
                        )
                );

        assertEquals(
                ErrorCode.FILE_TOO_LARGE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw when cloudinary upload fails")
    void shouldThrowWhenUploadFails() throws Exception {

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenReturn("data".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException());

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> cloudinaryService.upload(
                                file,
                                "avatars"
                        )
                );

        assertEquals(
                ErrorCode.FILE_UPLOAD_FAILED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should delete file successfully")
    void shouldDeleteFileSuccessfully() throws Exception {

        String url =
                "https://res.cloudinary.com/demo/image/upload/v1234567890/avatars/user1.jpg";

        when(cloudinary.uploader()).thenReturn(uploader);

        // Act
        cloudinaryService.delete(url);

        // Assert
        verify(uploader)
                .destroy(
                        eq("avatars/user1"),
                        anyMap()
                );
    }

    @Test
    @DisplayName("Should ignore null url")
    void shouldIgnoreNullUrl() {

        cloudinaryService.delete(null);

        verifyNoInteractions(cloudinary);
    }

    @Test
    @DisplayName("Should ignore blank url")
    void shouldIgnoreBlankUrl() {

        cloudinaryService.delete(" ");

        verifyNoInteractions(cloudinary);
    }

    @Test
    @DisplayName("Should throw when image url is invalid")
    void shouldThrowWhenImageUrlInvalid() {

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> cloudinaryService.delete(
                                "invalid-url"
                        )
                );

        assertEquals(
                ErrorCode.FILE_DELETE_FAILED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw when delete fails")
    void shouldThrowWhenDeleteFails() throws Exception {

        String url =
                "https://res.cloudinary.com/demo/image/upload/v1234567890/avatars/user1.jpg";

        when(cloudinary.uploader()).thenReturn(uploader);

        when(uploader.destroy(anyString(), anyMap()))
                .thenThrow(new RuntimeException());

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> cloudinaryService.delete(url)
                );

        assertEquals(
                ErrorCode.FILE_DELETE_FAILED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should return same url")
    void shouldReturnSameUrl() {

        String url =
                "https://cloudinary.com/image.jpg";

        assertEquals(
                url,
                cloudinaryService.getUrl(url)
        );
    }
}
