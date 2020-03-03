package org.seckill.mq;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.MessageProperties;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hengjian.feng
 * @date 2020年02月25日
 */
@Service
public class SeckillConsumer implements MessageListener{

//    MessageConverter messageConverter=new SimpleMessageConverter();
//
    @Autowired
    private SeckillDao seckillDao;

//    @Resource(name = "amqpTemplate")
//    private AmqpTemplate amqpTemplate;

    @Override
    public void onMessage(Message message) {
        MessageConverter messageConverter=new SimpleMessageConverter();
        Long seckillId = (Long) messageConverter.fromMessage(message);
        //seckillDao.killByProcedure(map);
        //Integer result = MapUtils.getInteger(map, "result", -2);

        //System.out.println(message);
        try {
            seckillDao.reduceStock(seckillId);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        String result=new String("hhh");
//        MessageProperties messageProperties = new MessageProperties();
//        messageProperties.getHeaders().putAll(message.getMessageProperties().getHeaders());
//        Message message1=messageConverter.toMessage(result,messageProperties);
//        String replyTo=message.getMessageProperties().getReplyTo();
//        amqpTemplate.convertAndSend(replyTo,11);
    }

//    @RabbitListener(queues = "queueTest")
//    @SendTo("queueTest")
//    public Integer MessageProcess(Map<String,Object> map) {
//        //Map<String,Object> map=(Map<String,Object>)messageConverter.fromMessage(message);
////        for(int i=0;i<5;i++)
////            System.out.println("-----------------------------------------");
//        seckillDao.killByProcedure(map);
//        Integer result = MapUtils.getInteger(map, "result", -2);
//        return result;
//    }
}
