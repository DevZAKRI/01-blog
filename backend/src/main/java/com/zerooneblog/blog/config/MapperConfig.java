package com.zerooneblog.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.repository.CommentRepository;
import com.zerooneblog.blog.repository.LikeRepository;

@Configuration
public class MapperConfig {
    @Bean
    public EntityMapper entityMapper(CommentRepository commentRepository, LikeRepository likeRepository) {
        return new EntityMapper(commentRepository, likeRepository);
    }
}
