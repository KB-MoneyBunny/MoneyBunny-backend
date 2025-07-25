package org.scoula.config;

import org.scoula.common.util.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("usable-sculpin-58851.upstash.io");
        config.setPort(6379);
        config.setPassword(RedisPassword.of("AeXjAAIjcDFiMDhkMDEzYzBhODU0YWU5OWJjNmE5Mjk5YjFlNzU1MnAxMA"));

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl() // SSL 사용 설정
                .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }



    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    // RedisUtil Bean 등록
    @Bean
    public RedisUtil redisUtil(RedisTemplate<String, String> redisTemplate) {
        return new RedisUtil(redisTemplate);
    }
}
