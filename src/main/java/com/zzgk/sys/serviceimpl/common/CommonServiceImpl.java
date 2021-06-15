package com.zzgk.sys.serviceimpl.common;

import com.zzgk.sys.dao.current.MyRepository;
import com.zzgk.sys.entity.current.SearchParam;
import com.zzgk.sys.service.common.CommonService;
import com.zzgk.sys.util.ResponseMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    MyRepository myRepository;

    @Override
    public void search(SearchParam param, HttpServletResponse response) {
        String tableName =null;
        String whereSql=null;
        Map<String,Object> whereParam = new HashMap<>();
        switch (param.getSearch_type()){
            case "role":
                tableName="role";
                whereSql="and (name like concat(:name,'%') or code like concat(:name,'%'))";
                whereParam.put("name",param.getKeywords());
                break;
            case "menu":
                tableName="permission";
                whereSql="and name like concat(:name,'%')";
                whereParam.put("name",param.getKeywords());
                break;
        }
        try {
            if (StringUtils.hasLength(tableName)){
                ResponseMsgUtil.sendSuccessMsg("ok",myRepository.searchReturnIPagination(tableName,whereSql,whereParam,
                                    param.getPage(), param.getSize()),response);
            }else {
                ResponseMsgUtil.sendFailMsg("搜索错误，请联系管理员",response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
