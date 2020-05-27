package org.itstep.botapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller("api/put_product")
public class MainController {

    @GetMapping
    @ResponseBody
    public String main(){
        return "ok";
    }
}
