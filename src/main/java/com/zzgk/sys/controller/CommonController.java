package com.zzgk.sys.controller;

import com.zzgk.sys.entity.current.SearchParam;
import com.zzgk.sys.service.common.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.soap.Addressing;

@Controller
public class CommonController {

    @Autowired
    CommonService commonService;


    @PostMapping("/common/search")
    public void commonSearch(SearchParam param, HttpServletResponse response){
        commonService.search(param,response);
    }
}
