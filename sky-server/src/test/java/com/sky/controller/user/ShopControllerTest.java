package com.sky.controller.user;

import com.sky.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopControllerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ShopController shopController;

    @Test
    void getStatusReturnsShopStatusFromRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(ShopController.KEY)).thenReturn(1);

        Result<Integer> result = shopController.getStatus();

        assertThat(result.getCode()).isEqualTo(1);
        assertThat(result.getData()).isEqualTo(1);
        verify(valueOperations).get(ShopController.KEY);
        verify(valueOperations, never()).set(ShopController.KEY, 1);
    }
}
