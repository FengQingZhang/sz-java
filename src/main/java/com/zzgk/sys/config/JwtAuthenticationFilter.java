package com.zzgk.sys.config;

import com.zzgk.sys.serviceimpl.sys.UserServiceImpl;
import com.zzgk.sys.util.JsonWebTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录成功后，对此类进行鉴权
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    JsonWebTokenUtil tokenUtil;
    @Autowired
    UserServiceImpl userServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        //从请求头部获取json web token
        String jwt = request.getHeader(tokenUtil.getHeader());
        if (StringUtils.hasLength(jwt)&&!jwt.equals("null")&&!jwt.equals("undefined")) {
            //从jwt中获取用户名
            String username = tokenUtil.getUsernameFromToken(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                //通过用户名查询
                UserDetails userDetails = userServiceImpl.loadUserByUsername(username);
                //创建认证信息
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
                        userDetails.getPassword(), userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request,response);
    }
}
