package com.bussanq.kubewolf.web.config;

import com.bussanq.kubewolf.web.res.ResultJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.ExecutionException;

/**
 * @author bussanq
 * @date 2024/11/17
 */
@Slf4j
@RestControllerAdvice
public class ExceptHandler {

    @ExceptionHandler({ExecutionException.class, RuntimeException.class})
    public ResultJson handleException(Exception e) {
        log.error("", e);
        return ResultJson.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResultJson exception(Exception e) {
        /*ajax
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exception", e.getMessage());
        modelAndView.setViewName("error/500");
        return modelAndView;*/
        log.error("", e);
        return ResultJson.error("内部错误");
    }



}
