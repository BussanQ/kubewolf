package com.bussanq.kubewolf.web.model.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQuery {
    @Min(1) @Max(1000000)
    private int pageNum = 1;
    @Min(1) @Max(100)
    private int pageSize = 10;
}
