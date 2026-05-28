package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.DishService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishControllerTest {

    @Mock
    private DishService dishService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private DishController dishController;

    @Test
    void deleteSkipsRedisDeleteWhenNoCacheKeysMatch() {
        when(redisTemplate.keys("dish_*")).thenReturn(null);

        Result result = dishController.delete(Collections.singletonList(1L));

        assertThat(result.getCode()).isEqualTo(1);
        verify(dishService).deleteBatch(Collections.singletonList(1L));
        verify(redisTemplate, never()).delete(org.mockito.ArgumentMatchers.<Collection<String>>any());
    }

    @Test
    void deleteClearsDishCacheWhenKeysMatch() {
        Set<String> keys = new LinkedHashSet<>();
        keys.add("dish_1");
        keys.add("dish_2");
        when(redisTemplate.keys("dish_*")).thenReturn(keys);

        Result result = dishController.delete(Collections.singletonList(1L));

        assertThat(result.getCode()).isEqualTo(1);
        verify(redisTemplate).delete(keys);
    }
}
