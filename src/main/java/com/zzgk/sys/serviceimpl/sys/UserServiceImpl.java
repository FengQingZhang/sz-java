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
            //查询用户权限
            List<GrantedAuthority> authorities =new ArrayList<>();
            List<Map<String,Object>> roles=roleDao.getRoleList(user.getId());
            for (Map<String,Object> one:roles) {
                SimpleGrantedAuthority authority=new SimpleGrantedAuthority((String) one.get("code"));
                authorities.add(authority);
            }
            detail.setAuthorities(authorities);
            return detail;
        }else
        throw new UsernameNotFoundException("该账号不存在");
    }

    public void checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token=request.getHeader(jwtTokenUtil.getHeader());
        if(StringUtils.hasLength(token)&&!token.equals("null")){
            //根据username加载权限
            String username=jwtTokenUtil.getUsernameIgnoreExpiration(token);
            if(username!=null&& SecurityContextHolder.getContext().getAuthentication()!=null){
                UserDetails userDetails=loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(token,userDetails)){
                    ResponseMsgUtil.sendSuccessMsg("已登录",null,response);
                }else {
                    //如果jwt过期，则获取refresh_token，判断refresh_token是否过期，不过期则刷新token返回前端
                    String refreshToken = refreshTokenDao.getRefreshToken(username);
                    if (jwtTokenUtil.validateToken(refreshToken,userDetails)){
                        ResponseMsgUtil.sendSuccessMsg("刷新jwt", jwtTokenUtil.refreshToken(token),response);
                    }else {
                        ResponseMsgUtil.sendFailMsg("登录状态过期请重新登录",response);
                    }
                }
            }
        }else {
            ResponseMsgUtil.sendFailMsg("请您登录后再进行操作",response);
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
        //因为一个用户可能拥有多个角色，所以菜单可能重复，这一步为根据id去重
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
        ResponseMsgUtil.sendSuccessMsg("查询成功",list,response);
    }

    //获取子菜单
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
     * 获取角色列表
     * @param page 页码
     * @param size 每页数量
     * @param request 请求
     * @param response 响应
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
     * 刷新令牌
     */
    public void refreshToken(HttpServletRequest request,HttpServletResponse response){
        String token=request.getHeader(jwtTokenUtil.getHeader());
        if (StringUtils.hasLength(token)&&!token.equals("null")){
            String username=jwtTokenUtil.getUsernameIgnoreExpiration(token);
            //如果jwt过期，则获取refresh_token，判断refresh_token是否过期，不过期则刷新token返回前端
            String refreshToken = refreshTokenDao.getRefreshToken(username);
            UserDetails userDetails=loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(refreshToken,userDetails)){
                try {
                    String newToken = jwtTokenUtil.refreshToken(token);
                    ResponseMsgUtil.sendSuccessMsg("刷新jwt",newToken ,response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    ResponseMsgUtil.sendFailMsg("登录状态过期请重新登录",response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            try {
                ResponseMsgUtil.sendFailMsg("登录状态过期请重新登录",response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
