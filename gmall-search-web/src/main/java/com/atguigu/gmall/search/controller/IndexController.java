package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.annotations.LoginRequired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Controller
@CrossOrigin
public class IndexController {

    @RequestMapping("/index")
    @LoginRequired(isNeededSuccess = false)
    public String index() throws IOException, InvocationTargetException, IllegalAccessException {

//        List<PmsBaseCatalog1> pmsBaseCatalog1s = catalogService.getCatalogAll();
//        String pmsStr = JSON.toJSONString(pmsBaseCatalog1s);
//        File file = new File("d:/a.json");
//        FileOutputStream fileOutputStream = new FileOutputStream(file);
//        fileOutputStream.write(pmsStr.getBytes());

        return "index";
    }

}
