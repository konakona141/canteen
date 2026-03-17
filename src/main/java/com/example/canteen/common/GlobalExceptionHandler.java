package com.example.canteen.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice // 核心注解：通知 Spring 拦截所有 Controller 的异常
public class GlobalExceptionHandler {

    /**
     * 拦截自定义的业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常发生: {}", e.getMessage());
        // 将异常信息封装进你定义的 Result 对象返回
        return Result.error(e.getMessage());
    }

    /**
     * 拦截系统异常（如数据库报错、空指针等）
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统运行出错: ", e);
        return Result.error("服务器开小差了，请稍后再试");
    }
}