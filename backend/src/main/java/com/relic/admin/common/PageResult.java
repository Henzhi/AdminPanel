package com.relic.admin.common;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> implements Serializable {

    private Long total;
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }
}
