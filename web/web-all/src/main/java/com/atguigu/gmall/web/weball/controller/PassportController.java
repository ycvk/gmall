package com.atguigu.gmall.web.weball.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author VERO
 * @version 1.0
 * @date 2021/9/27/21:04
 */
@Controller
public class PassportController {


    @GetMapping("login.html")
    public String listIndex(HttpServletRequest request) {

        //获取从哪里跳转的url
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);

        return "login";
    }

}
