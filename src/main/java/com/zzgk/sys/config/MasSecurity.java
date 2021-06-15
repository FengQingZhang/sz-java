package com.zzgk.sys.config;
import com.zzgk.sys.dao.sys.RefreshTokenDao;
import com.zzgk.sys.entity.sys.RefreshToken;
import com.zzgk.sys.util.JsonWebTokenUtil;
import com.zzgk.sys.util.ResponseMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * spring security 配置类
 * @author cpms
 */
@EnableWebSecurity
public class MasSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    LoginAuthProvider loginAuthProvider;
    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    JsonWebTokenUtil jsonWebTokenUtil;
    @Autowired
    RefreshTokenDao refreshTokenDao;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //登录处理
        auth.authenticationProvider(loginAuthProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //允许跨域，配置后SpringSecurity会自动寻找name=corsConfigurationSource的Bean
        http.cors();
        http.csrf().disable();
        //当访问接口失败的配置
        http.exceptionHandling().authenticationEntryPoint(new InterfaceAccessException());
        http.authorizeRequests()
                .antMatchers("/login","/refreshToken","/user/check_login").permitAll()
                .antMatchers("/admin/*").hasAnyRole("ROLE_ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .successHandler(new MySuccessHandler())//登录成功的处理
                .failureHandler(new MyFailHandler())//登录失败的处理
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);//因为用不到session，所以选择禁用
        //向过滤器链中添加，自定义的jwt过滤器和json过滤器
        //在UsernamePasswordAuthenticationFilter之前添加jwtAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        //在UsernamePasswordAuthenticationFilter之后添加jsonAuthenticationFilter
            .addFilterAfter(getJsonAuthenticationFilter(),UsernamePasswordAuthenticationFilter.class);
    }

    //登录成功的处理类
     class MySuccessHandler implements AuthenticationSuccessHandler{

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {

            UserDetails details = (UserDetails) authentication.getPrincipal();
            List<GrantedAuthority> roles = (List<GrantedAuthority>) details.getAuthorities();
            //登录时同时生成refreshToken，保存到表中
            RefreshToken token = new RefreshToken();
            token.setUsername(details.getUsername());
            String refreshToken = jsonWebTokenUtil.generateRefreshToken(details,roles.get(0).getAuthority());
            token.setToken(refreshToken);
            if (refreshTokenDao.existRefreshToken(details.getUsername())>0){
                refreshTokenDao.updateRefreshToken(token);
            }else {
                refreshTokenDao.save(token);
            }
            response.setHeader(jsonWebTokenUtil.getHeader(), jsonWebTokenUtil.generateToken(details,roles.get(0).getAuthority()));
            ResponseMsgUtil.sendSuccessMsg("成功",null,response);

        }
    }

    //登录失败的处理
    static class MyFailHandler implements AuthenticationFailureHandler{

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationException e) throws IOException, ServletException {
           ResponseMsgUtil.sendFailMsg(e.getMessage(),response);
        }
    }

    @Bean
    JsonAuthenticationFilter getJsonAuthenticationFilter() throws Exception {
        JsonAuthenticationFilter filter = new JsonAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManagerBean());
        return filter;
    }

}




