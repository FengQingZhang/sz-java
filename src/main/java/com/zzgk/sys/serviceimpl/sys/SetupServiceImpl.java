package com.zzgk.sys.serviceimpl.sys;

import com.zzgk.sys.dao.sys.RoleDao;
import com.zzgk.sys.entity.sys.Role;
import com.zzgk.sys.service.sys.SetupService;
import com.zzgk.sys.util.ResponseMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class SetupServiceImpl implements SetupService {

    @Autowired
    RoleDao roleDao;


    @Override
    public void roleAdd(Role role, HttpServletResponse response) {
        try {
            roleDao.save(role);
            ResponseMsgUtil.sendSuccessMsg("",null,response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void roleChange(Role role, HttpServletResponse response) {
        try {
            roleDao.update(role);
            ResponseMsgUtil.sendSuccessMsg("",null,response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
