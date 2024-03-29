package org.seckill.web;

import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * @author hengjian.feng
 * @date 2019年11月20日
 */
@Controller
@RequestMapping("/seckill") // url:/模块/资源/{id}/细分  example:/seckill/list
public class SeckillController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

//    @Autowired
//    private RedisDao redisDao;
//
//    /**
//     * 初始化库存数据
//     */
//    @PostConstruct
//    public void initNumber(){
//        List<Seckill> seckills=seckillService.getSeckillList();
//        for (Seckill seckill:seckills) {
//            redisDao.initNumber(seckill.getSeckillId(),seckill.getNumber());
//        }
//    }

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model){
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list",list);
        //list.jsp + model = ModelAndView
        return "list"; //WEB-INF/jsp/"list".jsp
    }

    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model){
        if(seckillId==null){
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getSeckillById(seckillId);
        if(seckill==null){
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill",seckill);
        return "detail";
    }

    //ajax json
    @RequestMapping(value = "/{seckillId}/exposer",method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})//produces是为了解决json中的中文乱码问题
    @ResponseBody //把响应的数据封装成JSON
    public SeckillResult<Exposer> exposer(@PathVariable Long seckillId){
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true,exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            result = new SeckillResult<Exposer>(false,e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/{seckillId}/{md5}/execution",method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5")String md5,
                                                   @CookieValue(value = "killPhone",required = false) Long phone){
        //springMVC valid
        if(phone==null){
            return new SeckillResult<SeckillExecution>(false,"未注册");
        }
//        try {
//            SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId,phone,md5);
//            return new SeckillResult<SeckillExecution>(true,seckillExecution);
//        } catch(RepeatKillException e1){
//            SeckillExecution seckillExecution= new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
//            return new SeckillResult<SeckillExecution>(true,seckillExecution);
//        } catch(SeckillCloseException e1){
//            SeckillExecution seckillExecution= new SeckillExecution(seckillId, SeckillStatEnum.END);
//            return new SeckillResult<SeckillExecution>(true,seckillExecution);
//        } catch (SeckillException e) {
//            logger.error(e.getMessage(),e);
//            SeckillExecution seckillExecution= new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
//            return new SeckillResult<SeckillExecution>(true,seckillExecution);
//        }


//        Long number=redisDao.decrNumber(seckillId+"_number");
//
//        if(number<0){
//            redisDao.incrNumber(seckillId+"_number");
//            return new SeckillResult<SeckillExecution>(true,new SeckillExecution(seckillId, SeckillStatEnum.END));
//        }

        //调用存储过程
        SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(seckillId,phone,md5);
//        if(seckillExecution.getState()!=1){
//            redisDao.incrNumber(seckillId+"_number");
//        }
        return new SeckillResult<SeckillExecution>(true,seckillExecution);
    }

    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now = new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }

}
