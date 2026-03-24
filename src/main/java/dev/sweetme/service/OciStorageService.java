package dev.sweetme.service;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class OciStorageService {

    @Value("${app.oci.config-file}")
    private String configFile;

    @Value("${app.oci.profile}")
    private String profile;

    @Value("${app.oci.namespace}")
    private String namespace;

    @Value("${app.oci.bucket}")
    private String bucket;

    @Value("${app.oci.region}")
    private String region;

    private ObjectStorageClient storageClient;

    @PostConstruct
    public void init() {
        try {
            ConfigFileReader.ConfigFile config = ConfigFileReader.parse(configFile, profile);
            var provider = new ConfigFileAuthenticationDetailsProvider(config);
            storageClient = ObjectStorageClient.builder().build(provider);
            log.info("OCI Object Storage 클라이언트 초기화 완료 (namespace={}, bucket={})", namespace, bucket);
        } catch (Exception e) {
            log.warn("OCI Object Storage 초기화 실패 - 파일 업로드 기능이 비활성화됩니다. (config={}, reason={})", configFile, e.getMessage());
        }
    }

    public String upload(String objectName, MultipartFile file) throws IOException {
        if (storageClient == null) {
            throw new IllegalStateException("OCI 스토리지가 초기화되지 않았습니다. 설정을 확인하세요.");
        }
        PutObjectRequest request = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucket)
                .objectName(objectName)
                .contentType(file.getContentType() != null ? file.getContentType() : "image/png")
                .contentLength(file.getSize())
                .putObjectBody(file.getInputStream())
                .build();

        storageClient.putObject(request);
        String url = publicUrl(objectName);
        log.info("OCI 업로드 완료: {}", url);
        return url;
    }

    public String publicUrl(String objectName) {
        return String.format(
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                region, namespace, bucket, objectName
        );
    }
}
