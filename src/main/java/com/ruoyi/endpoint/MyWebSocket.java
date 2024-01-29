package com.ruoyi.endpoint;

import com.ruoyi.common.core.utils.SpringUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.config.HttpSessionConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author T
 */

/**
 * 每个连接会话都会生成一个MyWebSocket
 * ws 接受websocket请求路径
 */
@ServerEndpoint(value = "/ws",configurator = HttpSessionConfigurator.class)
@Component
public class MyWebSocket {

    /** 保存所有在线socket连接 */
    private static Map<String, MyWebSocket> webSocketMap = new ConcurrentHashMap<>();

    // 如果分布式环境下 可以把该map换成redis
    // websocket无法实例化，所以 可以把websocket单独抽出来作为一个服务
    // 如果是服务需要链接socket，让前端直接对应socket服务地址
    // 如果是socket服务需要发消息给前端，可以把消息（这个消息要知道要发到哪个session里，也就是消息里需要有sessionId）先放到消息队列
    //然后服务根据session找到对应的mywebsocket，然后把消息发出去
    // 分布式 redis+ip 精确找到消息对应的会话
    public static Map<String, MyWebSocket> httpSessionMyWebSocketMap = new ConcurrentHashMap<>();

    public static Map<String, String> ipMao = new ConcurrentHashMap<>();

    private RedisService redisService;

    public MyWebSocket() {
        System.out.println("1");
        RedisService bean = SpringUtils.getBean(RedisService.class);
        redisService = bean;
    }

    /** 记录当前在线数目 */
    private static int count = 0;

    /**
     * 当前连接（每个websocket连入都会创建一个MyWebSocket实例 */
    private Session session;

    private HttpSession httpSession;

    private Logger log = LoggerFactory.getLogger(this.getClass());


    /** 处理连接建立 */
    @OnOpen
    public void onOpen(Session session,EndpointConfig endpointConfig) {
        this.session = session;
        webSocketMap.put(session.getId(), this);
        addCount();

        this.httpSession = (HttpSession) endpointConfig.getUserProperties().get(HttpSession.class.getName());
        httpSessionMyWebSocketMap.put(httpSession.getId(),this);

        ipMao.put(httpSession.getId(), httpSession.getAttribute("ClientIP").toString());

        redisService.setCacheObject(httpSession.getId(), httpSession.getAttribute("ClientIP").toString());

        log.info("新的连接加入：{}", session.getId());
    }

    /**
     * 接受消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到客户端{}消息：{}", session.getId(), message);
        try {
            this.sendMessage("收到消息：" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *处理错误
     */
    @OnError
    public void onError(Throwable error, Session session) {
        error.printStackTrace();
        log.info("发生错误{},{}", session.getId(), error.getMessage());
    }

    /**
     * 处理连接关闭
     */
    @OnClose
    public void onClose() {
        webSocketMap.remove(this.session.getId());
        httpSessionMyWebSocketMap.remove(httpSession.getId());
        ipMao.remove(httpSession.getId());

        redisService.deleteObject(httpSession.getId());

        reduceCount();
        log.info("连接关闭 id:{}", this.session.getId());
    }

    /**
     * 发送消息
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);//异步
    }

    /**
     * 群发消息
     * @param message
     */
    public static void broadcast(String message) {
        MyWebSocket.webSocketMap.forEach((k, v) -> {
            try {
                v.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 获取在线连接数目
     * @return
     */
    public static int getCount() {
        return count;
    }

    /**
     * 操作count，使用synchronized确保线程安全
     */
    public static synchronized void addCount() {
        MyWebSocket.count++;
    }

    public static synchronized void reduceCount() {
        MyWebSocket.count--;
    }

}
