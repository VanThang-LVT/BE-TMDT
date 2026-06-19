package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.CommissionConfigRequest;
import com.lvt.tmdt.dto.response.CommissionConfigResponse;
import com.lvt.tmdt.entity.Category;
import com.lvt.tmdt.entity.CommissionConfig;
import com.lvt.tmdt.enums.CommissionStatus;
import com.lvt.tmdt.repository.CategoryRepository;
import com.lvt.tmdt.repository.CommissionConfigRepository;
import com.lvt.tmdt.mapper.CommissionConfigMapper;
import com.lvt.tmdt.service.intf.CommissionConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionConfigServiceImpl implements CommissionConfigService {

    private final CommissionConfigRepository commissionConfigRepository;
    private final CategoryRepository categoryRepository;
    private final CommissionConfigMapper commissionConfigMapper;

    @Override
    public List<CommissionConfigResponse> getAllConfigs() {
        return commissionConfigRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(commissionConfigMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommissionConfigResponse createConfig(CommissionConfigRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
        if (request.getStatus() == CommissionStatus.ACTIVE) {
            commissionConfigRepository.findLatestByCategoryAndStatus(request.getCategoryId(), CommissionStatus.ACTIVE)
                    .ifPresent(existing -> {
                        existing.setStatus(CommissionStatus.INACTIVE);
                        commissionConfigRepository.save(existing);
                    });
        }

        CommissionConfig config = commissionConfigMapper.mapToEntity(request, category);

        config = commissionConfigRepository.save(config);
        return commissionConfigMapper.mapToResponse(config);
    }

    @Override
    @Transactional
    public CommissionConfigResponse updateConfig(Integer id, CommissionConfigRequest request) {
        CommissionConfig config = commissionConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cấu hình hoa hồng"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
        if (request.getStatus() == CommissionStatus.ACTIVE) {
            commissionConfigRepository.findLatestByCategoryAndStatus(request.getCategoryId(), CommissionStatus.ACTIVE)
                    .ifPresent(existing -> {
                        if (!existing.getCommissionId().equals(id)) {
                            existing.setStatus(CommissionStatus.INACTIVE);
                            commissionConfigRepository.save(existing);
                        }
                    });
        }

        config.setCategory(category);
        config.setCommissionRate(request.getCommissionRate());
        config.setStatus(request.getStatus());

        config = commissionConfigRepository.save(config);
        return commissionConfigMapper.mapToResponse(config);
    }

    @Override
    @Transactional
    public CommissionConfigResponse updateConfigStatus(Integer id, String status) {
        CommissionConfig config = commissionConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cấu hình hoa hồng"));

        CommissionStatus newStatus = CommissionStatus.valueOf(status);

        if (newStatus == CommissionStatus.ACTIVE) {
            commissionConfigRepository.findLatestByCategoryAndStatus(config.getCategory().getCategoryId(), CommissionStatus.ACTIVE)
                    .ifPresent(existing -> {
                        if (!existing.getCommissionId().equals(id)) {
                            existing.setStatus(CommissionStatus.INACTIVE);
                            commissionConfigRepository.save(existing);
                        }
                    });
        }

        config.setStatus(newStatus);
        config = commissionConfigRepository.save(config);
        return commissionConfigMapper.mapToResponse(config);
    }
}
