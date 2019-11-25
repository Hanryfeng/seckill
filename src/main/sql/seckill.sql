-- 秒杀执行存储过程
-- ; 决定是否换行
DELIMITER $$ -- console 换行符 ; 转换为 $$
-- 定义存储过程
-- 参数: in 输入参数; out 输出参数
-- 函数row_count():返回上一条修改类型sql(delete,insert,update)的影响行数
-- row_count(): return 0:未修改数据;>0:表示修改的行数;<0:sql错误/未执行修改sql
-- 修改变量值: select ?? into ??
-- r_result: 1:成功秒杀 0:秒杀结束 -1:重复秒杀 -2:系统错误
-- 代码段不能有注释
-- END; END IF;后面要加分号
-- DEFAULT 要大写
CREATE PROCEDURE `seckill`.`execute_seckill`
    (in v_seckill_id bigint, in v_phone bigint,
     in v_kill_time timestamp ,out r_result int)
    BEGIN
        DECLARE insert_count int DEFAULT 0;
        START TRANSACTION;
        insert ignore into success_killed (seckill_id,user_phone,create_time,state)
            values (v_seckill_id,v_phone,v_kill_time,0);
        select row_count() into insert_count;
        IF (insert_count = 0) THEN
            ROLLBACK;
            set r_result=-1;
        ELSEIF(insert_count < 0) THEN
            ROLLBACK;
            set r_result=-2;
        ELSE
            update seckill
                set number=number-1
                where seckill_id=v_seckill_id
                and end_time > v_kill_time
                and start_time < v_kill_time
                and number>0;
            select row_count() into insert_count;
            IF (insert_count = 0) THEN
                ROLLBACK;
                set r_result=0;
            ELSEIF (insert_count < 0) THEN
                ROLLBACK;
                set r_result=-2;
            ELSE
                COMMIT;
                set r_result=1;
            END IF;
        END IF;
    END;
$$
--存储过程定义结束

DELIMITER ;

set @r_result=-3;--在console(mysql命令行)里面定义变量:set @变量名=初始值

--执行存储过程
call execute_seckill(1000,10987654321,now(),@r_result);

--获取结果
select @r_result;

-- 存储过程
-- 1:存储过程优化:事务行级锁持有的时间
-- 2:不要过度依赖储存过程
-- 3:简单的逻辑可以应用存储过程
-- 4:QPS:一个秒杀单6000/qps


