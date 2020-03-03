package org.seckill.mq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hengjian.feng
 * @date 2020年02月25日
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-rabbitmq.xml","classpath:spring/spring-dao.xml"})
//@ContextConfiguration({"classpath:spring/spring-rabbitmq.xml"})
public class RabbitmqTest {
    @Autowired
    private SeckillProducer seckillProducer;

    @Test
    public void sendAndReceiveTest(){
//        Date killTime=new Date();
//        Map<String,Object> map = new HashMap<String, Object>();
//        map.put("seckillId",1116511L);
//        map.put("phone",1234567L);
//        map.put("killTime",killTime);
//        map.put("result",null);

        try {
//            Integer result=seckillProducer.sendAndReceive(map);
            long id = 1000L;
            seckillProducer.send(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
