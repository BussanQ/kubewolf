package com.bussanq.kubewolf.web.config;

import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.web.res.ResultJson;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@Slf4j
@RestControllerAdvice
public class ExceptHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResultJson> api(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(ResultJson.error(e.getStatus(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultJson> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst().orElse("参数错误");
        return ResponseEntity.badRequest().body(ResultJson.error(400, message));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class, IllegalArgumentException.class})
    public ResponseEntity<ResultJson> invalid(Exception e) {
        return ResponseEntity.badRequest().body(ResultJson.error(400, "请求参数不合法"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResultJson> missing(NoResourceFoundException e) {
        return ResponseEntity.status(404).body(ResultJson.error(404, "未找到资源"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResultJson> method(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(405).body(ResultJson.error(405, "不支持的请求方式"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultJson> internal(Exception e) {
        log.error("Request failed", e);
        return ResponseEntity.internalServerError().body(ResultJson.error(500, "内部错误，请查看服务日志"));
    }
}
