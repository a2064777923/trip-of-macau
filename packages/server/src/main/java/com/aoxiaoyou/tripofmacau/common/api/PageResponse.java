package com.aoxiaoyou.tripofmacau.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "分页响应")
public class PageResponse<T> {

    @Schema(description = "当前页")
    private long pageNum;

    @Schema(description = "每页条数")
    private long pageSize;

    @Schema(description = "总条数")
    private long total;

    @Schema(description = "总页数")
    private long totalPages;

    @Schema(description = "数据列表")
    private List<T> list;

    public static <T> PageResponse<T> of(IPage<T> page) {
        return PageResponse.<T>builder()
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .totalPages(page.getPages())
                .list(page.getRecords())
                .build();
    }
}
