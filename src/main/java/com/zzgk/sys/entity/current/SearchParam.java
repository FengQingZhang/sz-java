package com.zzgk.sys.entity.current;

import lombok.Data;

/**
 * 搜索参数
 */
@Data
public class SearchParam {
    private String keywords;
    private int page;
    private int size;
    private String search_type;
}
