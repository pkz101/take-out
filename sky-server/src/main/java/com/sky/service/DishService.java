package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    /*
    * 新增菜品
    * */
    void savedishWithFlavor(DishDTO dishDTO);

    /*
    * 菜品分页查询
    * */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /*
    * 批量删除菜品
    * */
    void deleteBatch(List<Long> ids);

    /*
    * 根据id查询菜品和对应的口味数据
    * */
    DishVO getById(Long id);

    /*
    * 修改菜品
    * */
    void update(DishDTO dishDTO);

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /*
    * 根据分类id查询菜品数据
    * */
    List<Dish> list(Long categoryId);

    /*
    * 根据id查询菜品数据
    * */
    List<DishVO> listWithFlavor(Dish dish);
}
