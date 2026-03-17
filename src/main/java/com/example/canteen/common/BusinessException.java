package com.example.canteen.common;

/**
 * 自定义业务异常：用于主动抛出逻辑错误
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}