package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 没必要写接口，直接实现
 * @author hengjian.feng
 * @date 2019年11月22日
 */
public class RedisDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);
    }

    public void initNumber(long seckillId,int number){
        try {
            Jedis jedis=jedisPool.getResource();
            try {
                String key=seckillId+"_number";
                jedis.set(key,number+"");
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    public Long decrNumber(String key){
        try {
            Jedis jedis=jedisPool.getResource();
            try {
                Long number=jedis.decr(key);
                return number;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public Long incrNumber(String key){
        try {
            Jedis jedis=jedisPool.getResource();
            try {
                Long number=jedis.incr(key);
                return number;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    private RuntimeSchema<Seckill> schema= RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        try{
            Jedis jedis= jedisPool.getResource();
            try {
                String key="seckill:"+seckillId;
                //并没有实现内部序列化操作
                //get->byte[]->反序列化->Object(Seckill)
                //采用自定义序列化
                //protostuff : pojo
                byte[] bytes = jedis.get(key.getBytes());
                if(bytes != null){
                    //空对象
                    Seckill seckill = schema.newMessage();
                    ProtobufIOUtil.mergeFrom(bytes,seckill,schema);
                    //seckill被反序列化
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        }
        catch(Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        // set Object(seckill) -> 序列化 -> byte[]
        try{
            Jedis jedis=jedisPool.getResource();
            try {
                String key="seckill:"+seckill.getSeckillId();
                byte[] bytes=ProtobufIOUtil.toByteArray(seckill,schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout= 60 * 60;//一小时
                String result = jedis.setex(key.getBytes(),timeout,bytes);
                return result;
            } finally {
                jedis.close();
            }
        }
        catch(Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }
}
