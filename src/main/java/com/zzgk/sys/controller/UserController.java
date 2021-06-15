package com.zzgk.sys.controller;

import com.zzgk.sys.service.sys.IAuthenticationFacade;
import com.zzgk.sys.serviceimpl.sys.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class UserController {

    @Autowired
    UserServiceImpl userService;
    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @PostMapping("/user/check_login")
    public void checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
         userService.checkLogin(request,response);
    }

    @GetMapping("/menu")
    public void getMenu(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication authentication = authenticationFacade.getAuthentication();
        String username =(String) authentication.getPrincipal();
        userService.getMenu(username,request,response);
    }

    @PostMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request,HttpServletResponse response){
        userService.refreshToken(request,response);
    }

}
