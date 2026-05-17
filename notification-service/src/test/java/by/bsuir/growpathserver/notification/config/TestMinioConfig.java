package by.bsuir.growpathserver.notification.config;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import by.bsuir.growpathserver.notification.infrastructure.helper.MinioHelper;

@Configuration
@Profile("test")
public class TestMinioConfig {

    @Bean
    @Primary
    public MinioHelper minioHelper() {
        MinioHelper mock = mock(MinioHelper.class);

        doNothing().when(mock).initBucket(anyString());

        when(mock.getPutPresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("http://localhost:9000/test-bucket/test-key?presigned=true");

        when(mock.getGetPresignedUrl(anyString(), anyString(), anyInt()))
                .thenReturn("http://localhost:9000/test-bucket/test-key?presigned=true");

        Resource mockResource = new ByteArrayResource("test file content".getBytes());
        when(mock.download(anyString(), anyString())).thenReturn(mockResource);
        when(mock.downloadBytes(anyString(), anyString())).thenReturn("test file content".getBytes());

        return mock;
    }
}
