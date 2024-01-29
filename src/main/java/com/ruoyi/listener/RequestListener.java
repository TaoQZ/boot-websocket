package com.ruoyi.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author T
 * 使websocket可以结合HttpSession使用
 */
@WebListener
public class RequestListener implements ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        //将所有request请求都携带上httpSession
        ((HttpServletRequest) sre.getServletRequest()).getSession();

        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        HttpSession session = request.getSession();
        System.out.println("requestInitialized session = " + session + ", getRequestURL#" + request.getRequestURL().toString() + ", getRemoteHost#" + request.getRemoteHost() + ", getRemoteAddr#" + request.getRemoteAddr());

        String host = request.getHeader("Host");
        String xRealIP = request.getHeader("X-Real-IP");
        String  xForwardedFor = request.getHeader("X-Forwarded-For");

        System.out.println("host#" + host + ", X-Real-IP#" + xRealIP + ", X-Forwarded-For#" + xForwardedFor);

        session.setAttribute("ClientIP", request.getRemoteAddr());
//        session.setAttribute("ClientIP", xForwardedFor);
    }

    public RequestListener() {
    }

    @Override
    public void requestDestroyed(ServletRequestEvent arg0) {
    }
}
