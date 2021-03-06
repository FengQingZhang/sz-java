package com.zzgk.sys.serviceimpl.sys;

import com.alibaba.fastjson.JSONObject;
import com.zzgk.sys.dao.sys.*;
import com.zzgk.sys.entity.sys.*;
import com.zzgk.sys.util.JsonWebTokenUtil;
import com.zzgk.sys.util.ResponseMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    UserDao userDao;
    @Autowired
    AccountStateDao accountStateDao;
    @Autowired
    RoleDao roleDao;
    @Autowired
    MenuDao menuDao;
    @Autowired
    RefreshTokenDao refreshTokenDao;
    @Autowired
    JsonWebTokenUtil jwtTokenUtil;


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        UserEntity user = userDao.findByUserName(s);
        if (user!=null){
            SysUserDetail detail = new SysUserDetail();
            detail.setId(user.getId());
            detail.setUsername(user.getUsername());
            detail.setPassword(user.getPassword());
            AccountState accountState = accountStateDao.getAccountSateByUserid(user.getId());
            detail.setAccountNonExpired(accountState.getAccountNonExpired() == 1);
            detail.setAccountNonLocked(accountState.getAccountNonLocked()==1);
            detail.setEnabled(accountState.getEnabled()==1);
            detail.setCredentialsNonExpired(accountState.getCredentialsNonExpired()==1);
            //??????????????????
            List<GrantedAuthority> authorities =new ArrayList<>();
            List<Map<String,Object>> roles=roleDao.getRoleList(user.getId());
            for (Map<String,Object> one:roles) {
                SimpleGrantedAuthority authority=new SimpleGrantedAuthority((String) one.get("code"));
                authorities.add(authority);
            }
            detail.setAuthorities(authorities);
            return detail;
        }else
        throw new UsernameNotFoundException("??????????????????");
    }

    public void checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token=request.getHeader(jwtTokenUtil.getHeader());
        if(StringUtils.hasLength(token)&&!token.equals("null")){
            //??????username????????????
            String username=jwtTokenUtil.getUsernameIgnoreExpiration(token);
            if(username!=null&& SecurityContextHolder.getContext().getAuthentication()!=null){
                UserDetails userDetails=loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(token,userDetails)){
                    ResponseMsgUtil.sendSuccessMsg("?????????",null,response);
                }else {
                    //??????jwt??????????????????refresh_token?????????refresh_token?????????????????????????????????token????????????
                    String refreshToken = refreshTokenDao.getRefreshToken(username);
                    if (jwtTokenUtil.validateToken(refreshToken,userDetails)){
                        ResponseMsgUtil.sendSuccessMsg("??????jwt", jwtTokenUtil.refreshToken(token),response);
                    }else {
                        ResponseMsgUtil.sendFailMsg("?????????????????????????????????",response);
                    }
                }
            }
        }else {
            ResponseMsgUtil.sendFailMsg("??????????????????????????????",response);
        }

    }

    public void getMenu(String username,HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserEntity entity = userDao.findByUserName(username);
        Integer userid = entity.getId();
        List<Role> roles =roleDao.getRoleByUserId(userid);
        //List<Menu> menus = new ArrayList<>();
        List<Map<String,Object>> data = new ArrayList<>();
        for (Role role : roles) {
            List<Map<String, Object>> mapList = menuDao.getMainMenu(role.getId());
            for (Map<String, Object> one : mapList) {
                Map<String, Object> temp = new HashMap<>(one);
                if (menuDao.checkContainSubmenu((int)temp.get("id"))>0){
                    data.add(buildSubmenu(temp,role.getId()));
                }else
                    data.add(temp);
            }
        }
        //??????????????????????????????????????????????????????????????????????????????????????????id??????
        List<Map<String, Object>> list = data.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () ->new TreeSet<>(Comparator.comparing(m->m.get("id").toString()))
                        ),ArrayList::new
                )
        );
       /* for (Role role:roles){
            List<Menu> menu=menuDao.getMainMenuList(role.getId());
            if (!menus.containsAll(menu)){
                menus.addAll(menu);
            }
        }*/
        ResponseMsgUtil.sendSuccessMsg("????????????",list,response);
    }

    //???????????????
    public Map<String,Object>buildSubmenu(Map<String,Object> parentMenu,int role_id){
            List<Map<String,Object>> secondMenu = menuDao.getSecondMenu((int)parentMenu.get("id"),role_id);
            Map<String,Object> map = new HashMap<>(parentMenu);
            for (int i =0;i<secondMenu.size();i++) {
                Map<String,Object> temp = new HashMap<>(secondMenu.get(i));
                if (menuDao.checkContainSubmenu((int)temp.get("id"))>0){
                    temp.put("submenu",menuDao.getSecondMenu((int)temp.get("id"),role_id));
                    secondMenu.set(i,temp);
                }
            }
            map.put("submenu",secondMenu);
            return map;
    }

    /**
     * ??????????????????
     * @param page ??????
     * @param size ????????????
     * @param request ??????
     * @param response ??????
     */
    public void getRoleList(int page, int size, HttpServletRequest request, HttpServletResponse response) {
        PageRequest pageRequest = PageRequest.of((page-1),size);
        try {
            ResponseMsgUtil.sendSuccessMsg("ok",roleDao.findAll(pageRequest),response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     */
    public void refreshToken(HttpServletRequest request,HttpServletResponse response){
        String token=request.getHeader(jwtTokenUtil.getHeader());
        if (StringUtils.hasLength(token)&&!token.equals("null")){
            String username=jwtTokenUtil.getUsernameIgnoreExpiration(token);
            //??????jwt??????????????????refresh_token?????????refresh_token?????????????????????????????????token????????????
            String refreshToken = refreshTokenDao.getRefreshToken(username);
            UserDetails userDetails=loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(refreshToken,userDetails)){
                try {
                    String newToken = jwtTokenUtil.refreshToken(token);
                    ResponseMsgUtil.sendSuccessMsg("??????jwt",newToken ,response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    ResponseMsgUtil.sendFailMsg("?????????????????????????????????",response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            try {
                ResponseMsgUtil.sendFailMsg("?????????????????????????????????",response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
