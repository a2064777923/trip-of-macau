package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatalogRelationBindingResponse {

    private Long id;
    private String code;
    private String name;
}
