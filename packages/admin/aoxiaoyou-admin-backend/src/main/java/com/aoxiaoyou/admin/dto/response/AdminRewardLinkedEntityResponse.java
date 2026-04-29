package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminRewardLinkedEntityResponse {
    private String ownerDomain;
    private Long ownerId;
    private String ownerCode;
    private String ownerName;
    private String bindingRole;
}
