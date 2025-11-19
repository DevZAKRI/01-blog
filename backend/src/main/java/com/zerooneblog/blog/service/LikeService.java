package com.zerooneblog.blog.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.PostLike;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.LikeRepository;
import com.zerooneblog.blog.repository.PostRepository;

@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    public void like(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        likeRepository.findByUserAndPost(user, post).ifPresent(l -> { throw new IllegalStateException("Already liked"); });
        PostLike l = new PostLike();
        l.setPost(post);
        l.setUser(user);
        likeRepository.save(l);
    }

    public void unlike(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        likeRepository.findByUserAndPost(user, post).ifPresent(likeRepository::delete);
    }

    public long countLikes(Post post) { return likeRepository.countByPost(post); }
}
