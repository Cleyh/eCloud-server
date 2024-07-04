package com.jidays.ecloud;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public void handleOptionsRequest() {
        // Spring Boot 会自动处理 CORS 头部
    }
}
