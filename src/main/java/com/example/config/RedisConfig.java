package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// 默认的 RedisTemplate 使用 JDK 序列化，可读性差且兼容性低。这个类使其改为 JSON 序列化，便于调试和跨语言使用。
@Configuration
public class RedisConfig {

	@Bean
	RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {

		RedisTemplate redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		jackson2JsonRedisSerializer.setObjectMapper(new ObjectMapper());

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);

		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		// Hash的value使用String序列化，因为验证码等简单字符串不需要JSON序列化
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());

		return redisTemplate;

	}

}
