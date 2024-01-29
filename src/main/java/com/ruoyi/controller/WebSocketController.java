package com.ruoyi.controller;

import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.endpoint.MyWebSocket;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Member;

/**
 * @author T
 */
@RestController
@RequestMapping("/web")
public class WebSocketController {

    /**
     * 回顾： httpSession关闭浏览器销毁，再次打开后会生成新的sessionId(会话)
     * @param httpSession
     */
    @GetMapping
    public void demo(HttpSession httpSession) {
        MyWebSocket.broadcast("哈哈哈哈");
    }

    @GetMapping("/getCount")
    public Integer getCount() {
        return MyWebSocket.getCount();
    }

    /**
     * 给指定的用户发送消息
     * @param msg
     * @param session
     * @return
     * @throws IOException
     */
    @GetMapping("/sendMessage/{msg}")
    public String sendMessage(@PathVariable String msg, HttpSession session) throws IOException {
        MyWebSocket myWebSocket = MyWebSocket.httpSessionMyWebSocketMap.get(session.getId());
        String result = "您还没有连接";
        if (null != myWebSocket) {
            myWebSocket.sendMessage(msg);
            return msg;
        }
        return result;
    }



    @GetMapping("/sendMessage2/{wid}/{msg}")
    public String sendMessage2(@PathVariable String wid,@PathVariable String msg) throws IOException {
        MyWebSocket myWebSocket = MyWebSocket.httpSessionMyWebSocketMap.get(wid);
        String result = "您还没有连接";
        if (null != myWebSocket) {
            myWebSocket.sendMessage(msg);
            return msg;
        }
        return result;
    }


    @Autowired
    private RedisService redisService;

    @GetMapping("/sendMessage3/{wid}/{msg}")
    public String sendMessage3(@PathVariable String wid, @PathVariable String msg, HttpServletRequest request) throws IOException {
        Object ipobj = redisService.getCacheObject(wid);


        String result = "您还没有连接";
        if (ipobj == null) {
            return result;
        }

        String remoteHost = request.getRemoteHost();
        if (remoteHost.equals(ipobj.toString())) {
            MyWebSocket myWebSocket = MyWebSocket.httpSessionMyWebSocketMap.get(wid);
            myWebSocket.sendMessage(msg);
            return msg;
        } else {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            // 使用httpClient
            String url = "http://"+ipobj.toString()+":9208" + "/web/sendMessage2/"+wid+"/"+ msg;
            System.out.println(url);
            HttpGet httpGet = new HttpGet(url);

            httpClient.execute(httpGet);
            return msg;
        }


    }

}
