package dev.withstudy.dto.response;

import dev.withstudy.domain.Company;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDto {

    private Long id;
    private String name;
    private String slug;
    private String accentColor;
    private String logoUrl;

    public static CompanyDto from(Company company, String logoBaseUrl) {
        String logoUrl = logoBaseUrl + company.getSlug() + ".png";
        return new CompanyDto(
                company.getId(),
                company.getName(),
                company.getSlug(),
                company.getAccentColor(),
                logoUrl
        );
    }
}
