package com.example.canteen.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // 必须有无参构造
@AllArgsConstructor // 全参构造
public class Result<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setSuccess(true);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.setSuccess(true);
        return r;
    }
    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }
}