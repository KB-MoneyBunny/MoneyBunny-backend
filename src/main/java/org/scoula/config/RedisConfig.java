package org.scoula.config;

import org.scoula.common.util.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("usable-sculpin-58851.upstash.io");
        config.setPort(6379); // 실제 포트로 교체 필요
        config.setPassword(RedisPassword.of("AeXjAAIjcDFiMDhkMDEzYzBhODU0YWU5OWJjNmE5Mjk5YjFlNzU1MnAxMA"));

        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
        jedisClientConfiguration.useSsl();

        return new JedisConnectionFactory(config, jedisClientConfiguration.build());
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
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
