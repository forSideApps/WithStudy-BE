package dev.withstudy.controller;

import dev.withstudy.dto.response.CompanyDto;
import dev.withstudy.service.CompanyService;
import dev.withstudy.service.OciStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final OciStorageService ociStorageService;

    @Value("${app.oci.namespace}")
    private String namespace;

    @Value("${app.oci.bucket}")
    private String bucket;

    @Value("${app.oci.region}")
    private String region;

    private String logoBaseUrl() {
        return String.format(
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/WithStudy/",
                region, namespace, bucket
        );
    }

    /** 프론트엔드에서 사용하는 기존 엔드포인트 유지 */
    @GetMapping("/api/themes")
    public List<CompanyDto> getAll() {
        return companyService.findAll().stream()
                .map(c -> CompanyDto.from(c, logoBaseUrl()))
                .toList();
    }

    /** 어드민: 회사 목록 조회 */
    @GetMapping("/api/admin/companies")
    public List<CompanyDto> adminGetAll() {
        return companyService.findAll().stream()
                .map(c -> CompanyDto.from(c, logoBaseUrl()))
                .toList();
    }

    /** 어드민: 회사 등록 */
    @PostMapping("/api/admin/companies")
    public ResponseEntity<CompanyDto> adminCreate(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String slug = (String) body.get("slug");
        String accentColor = (String) body.getOrDefault("accentColor", "#6366f1");
        Integer displayOrder = body.containsKey("displayOrder")
                ? Integer.parseInt(body.get("displayOrder").toString())
                : null;

        if (name == null || name.isBlank() || slug == null || slug.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var company = companyService.create(name, slug, accentColor, displayOrder);
        return ResponseEntity.ok(CompanyDto.from(company, logoBaseUrl()));
    }

    /** 어드민: 회사 정보 수정 */
    @PatchMapping("/api/admin/companies/{id}")
    public ResponseEntity<CompanyDto> adminUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String slug = (String) body.get("slug");
        String accentColor = (String) body.getOrDefault("accentColor", "#6366f1");
        Integer displayOrder = body.get("displayOrder") != null
                ? Integer.parseInt(body.get("displayOrder").toString())
                : null;

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var company = companyService.update(id, name, slug, accentColor, displayOrder);
        return ResponseEntity.ok(CompanyDto.from(company, logoBaseUrl()));
    }

    /** 어드민: 회사 로고 업로드 → OCI Object Storage */
    @PostMapping("/api/admin/companies/{id}/logo")
    public ResponseEntity<Map<String, String>> adminUploadLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        var company = companyService.findById(id);
        String objectName = "WithStudy/" + company.getSlug() + ".png";

        try {
            String url = ociStorageService.upload(objectName, file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            log.error("로고 업로드 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "업로드 실패: " + e.getMessage()));
        }
    }
}
