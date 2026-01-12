package com.zerooneblog.blog.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.blog.exception.BadRequestException;
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

    public boolean toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Reject likes on hidden posts (unless user is admin)
        if (post.isHidden() && !"ADMIN".equals(user.getRole())) {
            throw new BadRequestException("Cannot interact with hidden posts");
        }
        
        var existing = likeRepository.findByUserAndPost(user, post);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false; // unliked
        }
        PostLike l = new PostLike();
        l.setPost(post);
        l.setUser(user);
        likeRepository.save(l);
        return true; // liked
    }

    public void unlike(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Reject unlikes on hidden posts (unless user is admin)
        if (post.isHidden() && !"ADMIN".equals(user.getRole())) {
            throw new BadRequestException("Cannot interact with hidden posts");
        }
        
        likeRepository.findByUserAndPost(user, post).ifPresent(likeRepository::delete);
    }

    public long countLikes(Post post) { return likeRepository.countByPost(post); }
}
