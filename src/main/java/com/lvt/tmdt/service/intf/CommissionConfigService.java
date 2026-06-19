package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.CommissionConfigRequest;
import com.lvt.tmdt.dto.response.CommissionConfigResponse;

import java.util.List;

public interface CommissionConfigService {
    List<CommissionConfigResponse> getAllConfigs();
    CommissionConfigResponse createConfig(CommissionConfigRequest request);
    CommissionConfigResponse updateConfig(Integer id, CommissionConfigRequest request);
    CommissionConfigResponse updateConfigStatus(Integer id, String status);
}
