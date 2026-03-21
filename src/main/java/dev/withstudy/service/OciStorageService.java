package dev.withstudy.service;

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
    public void init() throws IOException {
        ConfigFileReader.ConfigFile config = ConfigFileReader.parse(configFile, profile);
        var provider = new ConfigFileAuthenticationDetailsProvider(config);
        storageClient = ObjectStorageClient.builder().build(provider);
        log.info("OCI Object Storage 클라이언트 초기화 완료 (namespace={}, bucket={})", namespace, bucket);
    }

    public String upload(String objectName, MultipartFile file) throws IOException {
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
