package org.seckill.dto;

import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;

/**
 *  封装秒杀执行后结果
 */
public class SeckillExecution {

    //Id
    private long seckillId;

    //秒杀成功对象
    private SuccessKilled successKilled;

    //秒杀执行结果状态
    private int state;

    //状态表示
    private String stateInfo;

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "seckillId=" + seckillId +
                ", successKilled=" + successKilled +
                ", state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                '}';
    }

    //state为成功秒杀
    public SeckillExecution(long seckillId, SuccessKilled successKilled, SeckillStatEnum seckillStatEnum) {
        this.seckillId = seckillId;
        this.successKilled = successKilled;
        this.state = seckillStatEnum.getState();
        this.stateInfo = seckillStatEnum.getStateInfo();
    }

    //state为秒杀失败
    public SeckillExecution(long seckillId, SeckillStatEnum seckillStatEnum) {
        this.seckillId = seckillId;
        this.state = seckillStatEnum.getState();
        this.stateInfo = seckillStatEnum.getStateInfo();
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }
}
