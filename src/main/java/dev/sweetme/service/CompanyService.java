package dev.sweetme.service;

import dev.sweetme.domain.Company;
import dev.sweetme.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<Company> findAll() {
        return companyRepository.findAllOrderByDisplayOrder();
    }

    public Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다."));
    }

    public Company findBySlug(String slug) {
        return companyRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다."));
    }

    @Transactional
    public void updateOrder(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Company company = findById(ids.get(i));
            company.update(company.getName(), company.getSlug(), company.getAccentColor(), i + 1);
        }
    }

    @Transactional
    public Company update(Long id, String name, String slug, String accentColor, Integer displayOrder) {
        Company company = findById(id);
        company.update(name, slug, accentColor, displayOrder);
        return company;
    }

    @Transactional
    public Company create(String name, String slug, String accentColor, Integer displayOrder) {
        if (companyRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("이미 존재하는 slug입니다: " + slug);
        }
        Company company = Company.builder()
                .name(name)
                .slug(slug)
                .accentColor(accentColor)
                .displayOrder(displayOrder)
                .build();
        return companyRepository.save(company);
    }
}
