package com.zzgk.sys.util;

import com.alibaba.fastjson.JSON;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应信息工具类
 * @author cpms
 */
public class ResponseMsgUtil {

    /**
     * 返回成功的处理信息
     * @param msg 返回前端的提示
     * @param data 返回前端的数据
     * @param response 响应
     */
    public static void sendSuccessMsg(String msg, Object data, HttpServletResponse response) throws IOException {
        Map<String,Object> map = new HashMap<>();
        map.put("code","200");
        map.put("msg",msg);
        map.put("state","ok");
        if (data!=null){
            map.put("data",data);
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        response.setContentType("text/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(JSON.toJSONString(map));
        out.flush();
        out.close();
    }

    /**
     * 返回失败的处理信息
     * @param msg 返回前端的提示信息
     * @param response 响应
     */
    public static void sendFailMsg(String msg,HttpServletResponse response) throws IOException {
        Map<String,Object> map = new HashMap<>();
        map.put("code","500");
        map.put("msg",msg);
        map.put("state","fail");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json;charset=utf-8");
        PrintWriter out =response.getWriter();
        out.write(JSON.toJSONString(map));
        out.flush();
        out.close();
    }
}
