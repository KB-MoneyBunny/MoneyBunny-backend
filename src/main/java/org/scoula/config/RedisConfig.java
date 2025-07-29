package org.scoula.config;

import org.scoula.common.util.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;


@Configuration
@PropertySource("classpath:application-dev.properties")
public class RedisConfig {

    // Redis 서버 연결 설정 (Upstash 사용)
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.ssl.enabled}")
    private boolean redisSslEnabled;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();

        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(RedisPassword.of(redisPassword));

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
