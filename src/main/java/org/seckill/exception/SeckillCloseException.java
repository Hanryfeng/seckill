package org.seckill.exception;

/**
 * 秒杀关闭异常
 * @author hengjian.feng
 * @date 2019年11月19日
 */
public class SeckillCloseException extends SeckillException {
    public SeckillCloseException(String s) {
        super(s);
    }

    public SeckillCloseException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
