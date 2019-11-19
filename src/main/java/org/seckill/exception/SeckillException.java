package org.seckill.exception;

/**
 * 秒杀相关异常（DAO通用异常）
 * @author hengjian.feng
 * @date 2019年11月19日
 */
public class SeckillException extends RuntimeException {
    public SeckillException(String s) {
        super(s);
    }

    public SeckillException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
