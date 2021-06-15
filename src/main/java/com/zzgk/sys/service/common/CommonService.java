package com.zzgk.sys.service.common;

import com.zzgk.sys.entity.current.SearchParam;

import javax.servlet.http.HttpServletResponse;

public interface CommonService {
    void search(SearchParam param, HttpServletResponse response);
}
