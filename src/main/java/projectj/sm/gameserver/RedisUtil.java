package projectj.sm.gameserver;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final StringRedisTemplate stringRedisTemplate;

    public Set<String> getFindKeys(String pattern) {
        return stringRedisTemplate.keys("*" + pattern + "*");
    }

    public String getData(String key) {
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setData(String key, String value) {
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(key,value, Duration.ofHours(3));
    }

    public void updateKey(String key, Duration duration) {
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.getAndExpire(key, duration);
    }

    public void deleteData(String key) {
        stringRedisTemplate.delete(key);
    }
}
