package org.seckill.exception;

/**
 * 重复秒杀异常（运行期异常）
 * @author hengjian.feng
 * @date 2019年11月19日
 */
public class RepeatKillException extends SeckillException {
    public RepeatKillException(String s) {
        super(s);
    }

    public RepeatKillException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
