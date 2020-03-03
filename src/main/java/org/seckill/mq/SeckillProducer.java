package org.seckill.mq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author hengjian.feng
 * @date 2020年02月25日
 */
@Service
public class SeckillProducer {

    @Resource(name = "amqpTemplate")
    private AmqpTemplate amqpTemplate;

    public Integer sendAndReceive(Map<String,Object> map){
        Integer result = (Integer)amqpTemplate.convertSendAndReceive("exchangeTest","queueTestKey", map);
        return result;
    }

    public void send(Long seckillId){
        amqpTemplate.convertAndSend("exchangeTest","queueTestKey", seckillId);
    }
}
