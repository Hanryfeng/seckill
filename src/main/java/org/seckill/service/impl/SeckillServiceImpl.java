package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hengjian.feng
 * @date 2019年11月19日
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    //日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //md5盐值字符串，用于混淆MD5
    private final String slat="novnainagr+g79f04+40fva1+f1";

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getSeckillById(long seckillId) {
        //优化点：缓存优化,超时的基础上维护一致性
        /**
         * 伪代码
         * get from cache
         * if null
         *      get db
         * else
         *      put cache
         * logic...
         */
        //1:访问redis
        Seckill seckill=redisDao.getSeckill(seckillId);
        if(seckill==null){
            //2:访问数据库
            seckill=seckillDao.queryById(seckillId);
            if(seckill==null){
                return null;
            }else{
                //3:放入redis
                redisDao.putSeckill(seckill);
            }
        }
        return seckill;
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill=getSeckillById(seckillId);
        if(seckill==null){
            return new Exposer(false,seckillId);
        }
        //系统当前时间
        Date nowTime = new Date();
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        if(startTime.getTime()>nowTime.getTime()
        ||endTime.getTime()<nowTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    //下面的方法可以重用，所以单独写
    private String getMD5(long seckillId){
        //加密途径：盐值 和 拼接规则
        String base = seckillId + "/" + slat;
        //spring的工具类“DigestUtils”，需要传入一个二进制的值
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如：只有一条修改操作，只读操作不需要事务控制
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if(md5==null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime = new Date();
        //减库存
        try {
            //并发优化：先插入购买明细，再进行update减库存（update会获取行级锁）
            //降低行级锁的持有时间
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
            //唯一：seckillId，userPhone
            if(insertCount<=0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else{
                //减库存，热点商品竞争
                int reduceCount = seckillDao.reduceNumber(seckillId,nowTime);
                if(reduceCount<=0){
                    //没有更新到记录，秒杀结束，rollback
                    throw new SeckillCloseException("seckill is closed");
                }else{
                    //秒杀成功，commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId,successKilled, SeckillStatEnum.SUCCESS);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            //所有编译期异常 转化为运行期异常
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }

    //不需要加Transactional,事务操作由MySQL接手
    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5){
        if(md5==null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATE_REWRITE);
        }
        Date killTime=new Date();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        //执行存储过程,result被赋值
        /**
         * 为什么这个方法不需要抛出异常？
         *         原本没有调用存储过程的执行秒杀操作之所以要抛出RuntimException，
         *         是为了让Spring事务管理器能够在秒杀不成功的时候进行回滚操作。
         *         而现在我们使用了存储过程，有关事务的提交或回滚已经在procedure里完成了，
         *         不需要再使用到Spring的事务了，既然如此，
         *         我们也就不需要在这个方法里抛出异常来让Spring帮我们回滚了。
         */
        try{
            seckillDao.killByProcedure(map);
            //在pom.xml里面引入依赖commons-collections,使用MapUtils
            //int result=(Integer) map.get("result");
            Integer result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, successKilled, SeckillStatEnum.SUCCESS);
            } else {
                //如果使用valueOf需要传入字符串,所以设计了stateOf方法,传入整型输出SeckillStatEnum
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }
        }
        catch(Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }
    }
}
