package com.saferide.util;

import com.saferide.common.lang.Result;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WebUtils extends org.springframework.web.util.WebUtils {

    private static JwtUtils jwtUtils;

    public final static String JWT_PAYLOAD_KEY = "Authorization";

    public final static String JWT_USER_ID_KEY = "UserId";

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        WebUtils.jwtUtils = jwtUtils;
    }

    public static HttpServletRequest getHttpServletRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request;
    }

    public static Claims getJwtPayload() {
        String jwtPayload = getHttpServletRequest().getHeader(WebUtils.JWT_PAYLOAD_KEY);
        return jwtUtils.getClaimByToken(jwtPayload);
    }

    public static Long getUserId() {
        Long id = Long.parseLong(getJwtPayload().get(WebUtils.JWT_USER_ID_KEY).toString());
        return id;
    }

    public static ServerHttpResponse getServerHttpResponse(Result result){
        return null;
    }

}

