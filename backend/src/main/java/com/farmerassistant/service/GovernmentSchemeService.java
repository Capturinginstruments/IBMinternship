package com.farmerassistant.service;

import com.farmerassistant.dto.response.SchemeResponse;
import com.farmerassistant.exception.ResourceNotFoundException;
import com.farmerassistant.model.GovernmentScheme;
import com.farmerassistant.model.SchemeBookmark;
import com.farmerassistant.model.User;
import com.farmerassistant.repository.GovernmentSchemeRepository;
import com.farmerassistant.repository.SchemeBookmarkRepository;
import com.farmerassistant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GovernmentSchemeService {

    private final GovernmentSchemeRepository schemeRepository;
    private final SchemeBookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<SchemeResponse> getAll(Pageable pageable, Long userId) {
        return schemeRepository.findByIsActiveTrue(pageable)
                .map(s -> toResponse(s, userId));
    }

    @Transactional(readOnly = true)
    public Page<SchemeResponse> getFiltered(GovernmentScheme.SchemeCategory category,
                                             String state, String crop,
                                             Double minLand, Double maxLand,
                                             Pageable pageable, Long userId) {
        return schemeRepository.findByFilters(category, state, crop, minLand, maxLand, pageable)
                .map(s -> toResponse(s, userId));
    }

    @Transactional(readOnly = true)
    public SchemeResponse getById(Long id, Long userId) {
        GovernmentScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme", id));
        return toResponse(scheme, userId);
    }

    @Transactional(readOnly = true)
    public Page<SchemeResponse> searchByKeyword(String keyword, Pageable pageable, Long userId) {
        String searchPattern = "%" + keyword.toLowerCase().trim() + "%";
        return schemeRepository.searchByKeyword(searchPattern, pageable)
                .map(s -> toResponse(s, userId));
    }

    public SchemeResponse create(GovernmentScheme scheme, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));
        scheme.setCreatedBy(admin);
        scheme = schemeRepository.save(scheme);
        return toResponse(scheme, adminId);
    }

    public SchemeResponse update(Long id, GovernmentScheme updatedScheme) {
        GovernmentScheme existing = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme", id));
        existing.setTitle(updatedScheme.getTitle());
        existing.setDescription(updatedScheme.getDescription());
        existing.setEligibility(updatedScheme.getEligibility());
        existing.setBenefits(updatedScheme.getBenefits());
        existing.setDocumentsRequired(updatedScheme.getDocumentsRequired());
        existing.setOfficialUrl(updatedScheme.getOfficialUrl());
        existing.setCategory(updatedScheme.getCategory());
        existing.setApplicableStates(updatedScheme.getApplicableStates());
        existing.setApplicableCrops(updatedScheme.getApplicableCrops());
        existing.setMinLandAcres(updatedScheme.getMinLandAcres());
        existing.setMaxLandAcres(updatedScheme.getMaxLandAcres());
        existing.setDeadline(updatedScheme.getDeadline());
        existing.setActive(updatedScheme.isActive());
        return toResponse(schemeRepository.save(existing), null);
    }

    public void delete(Long id) {
        GovernmentScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme", id));
        scheme.setActive(false);
        schemeRepository.save(scheme);
    }

    public boolean toggleBookmark(Long schemeId, Long userId) {
        if (bookmarkRepository.existsByUserIdAndSchemeId(userId, schemeId)) {
            bookmarkRepository.deleteByUserIdAndSchemeId(userId, schemeId);
            return false;
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            GovernmentScheme scheme = schemeRepository.findById(schemeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Scheme", schemeId));
            bookmarkRepository.save(SchemeBookmark.builder().user(user).scheme(scheme).build());
            return true;
        }
    }

    @Transactional(readOnly = true)
    public Page<SchemeResponse> getUserBookmarks(Long userId, Pageable pageable) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(b -> toResponse(b.getScheme(), userId));
    }

    private SchemeResponse toResponse(GovernmentScheme scheme, Long userId) {
        boolean isBookmarked = userId != null &&
                bookmarkRepository.existsByUserIdAndSchemeId(userId, scheme.getId());
        return SchemeResponse.builder()
                .id(scheme.getId()).title(scheme.getTitle()).description(scheme.getDescription())
                .eligibility(scheme.getEligibility()).benefits(scheme.getBenefits())
                .documentsRequired(scheme.getDocumentsRequired()).officialUrl(scheme.getOfficialUrl())
                .category(scheme.getCategory()).applicableStates(scheme.getApplicableStates())
                .applicableCrops(scheme.getApplicableCrops()).minLandAcres(scheme.getMinLandAcres())
                .maxLandAcres(scheme.getMaxLandAcres()).deadline(scheme.getDeadline())
                .isActive(scheme.isActive()).isBookmarked(isBookmarked)
                .createdAt(scheme.getCreatedAt()).build();
    }
}
