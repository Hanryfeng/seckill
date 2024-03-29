package org.seckill.enums;

/**
 * 使用枚举表述常量数据字典
 * @author hengjian.feng
 * @date 2019年11月19日
 */
public enum SeckillStatEnum {
    SUCCESS(1,"秒杀成功"),
    END(0,"秒杀结束"),
    REPEAT_KILL(-1,"重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATE_REWRITE(-3,"数据篡改");

    private int state;

    private String stateInfo;

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    SeckillStatEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public static SeckillStatEnum stateOf(int index){
        for (SeckillStatEnum statEnum: values()) {
            if(statEnum.getState()==index){
                return statEnum;
            }
        }
        return null;
    }
}
