package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api("用户dish端接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/list")
    @ApiOperation("查询指定分类下的菜品")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("查询分类下的菜品：{}",categoryId);
        String key = "dish_" + categoryId;
        // 缓存中是否有对应的菜品
        List<DishVO> list = getCachedDishList(key);
        // 缓存中有直接返回
        if (list != null && list.size() > 0){
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);

        // 缓存中没有，则查询数据库，添加到缓存中
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key,list);
        log.info("查询结果：{}",list);

        return Result.success(list);
    }

    @SuppressWarnings("unchecked")
    private List<DishVO> getCachedDishList(String key) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (!(cached instanceof List<?>)) {
            return null;
        }

        return (List<DishVO>) cached;
    }
}
