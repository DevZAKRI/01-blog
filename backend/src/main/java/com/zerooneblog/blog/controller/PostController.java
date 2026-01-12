package com.zerooneblog.blog.controller;

import java.net.URI;
import java.util.logging.Logger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.dto.response.PostDto;
import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.PostRepository;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.service.PostService;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private static final Logger logger = Logger.getLogger(PostController.class.getName());
    private final PostService postService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostController(PostService postService, UserRepository userRepository, PostRepository postRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    private User currentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new NotFoundException("User not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping
    public ResponseEntity<PostDto> create(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.zerooneblog.blog.dto.request.CreatePostRequest req, Authentication auth) {
        try {
            logger.info("[PostController] POST /posts - Step 1: Starting post creation");
            logger.info("[PostController] Auth name: " + (auth != null ? auth.getName() : "null"));
            logger.info("[PostController] Title: " + req.getTitle());
            logger.fine("[PostController] Description length: " + (req.getDescription() != null ? req.getDescription().length() : "null"));
            logger.fine("[PostController] Media URLs count: " + (req.getMediaUrls() != null ? req.getMediaUrls().length : 0));
            
            logger.info("[PostController] Step 2: Getting current user");
            User u = currentUser(auth);
            logger.info("[PostController] Current user ID: " + u.getId() + ", username: " + u.getUsername());
            
            logger.fine("[PostController] Step 3: Creating post object");
            Post p = new Post();
            p.setAuthor(u);
            p.setTitle(req.getTitle());
            p.setDescription(req.getDescription());
            
            // Convert mediaUrls array to JSON string
            if (req.getMediaUrls() != null && req.getMediaUrls().length > 0) {
                logger.fine("[PostController] Step 4: Serializing media URLs");
                try {
                    String mediaJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req.getMediaUrls());
                    p.setMediaUrls(mediaJson);
                    logger.fine("[PostController] Media URLs serialized: " + mediaJson);
                } catch (Exception e) {
                    logger.severe("[PostController] ERROR: Failed to serialize media URLs: " + e.getMessage());
                    p.setMediaUrls(null);
                }
            } else {
                logger.fine("[PostController] Step 4: No media URLs to serialize");
            }
            
            logger.info("[PostController] Step 5: Calling postService.create()");
            Post saved = postService.create(p);
            logger.info("[PostController] Step 6: Post saved with ID: " + saved.getId());
            
            logger.fine("[PostController] Step 7: Converting to DTO");
            PostDto dto = EntityMapper.toDto(saved, u);
            logger.info("[PostController] Step 8: Post creation successful");
            return ResponseEntity.created(URI.create("/api/v1/posts/" + saved.getId())).body(dto);
        } catch (Exception e) {
            logger.severe("[PostController] ERROR: Exception occurred: " + e.getClass().getName());
            logger.severe("[PostController] ERROR: Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{id}")
    public PostDto edit(@PathVariable Long id, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.zerooneblog.blog.dto.request.UpdatePostRequest req, Authentication auth) {
        User u = currentUser(auth);
        Post p = new Post();
        p.setTitle(req.getTitle());
        p.setDescription(req.getDescription());
        
        // Convert mediaUrls array to JSON string
        if (req.getMediaUrls() != null && req.getMediaUrls().length > 0) {
            try {
                p.setMediaUrls(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req.getMediaUrls()));
            } catch (Exception e) {
                p.setMediaUrls(null);
            }
        }
        
        Post updated = postService.edit(id, p, u);
        return EntityMapper.toDto(updated, u);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        User u = currentUser(auth);
        postService.delete(id, u);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public PostDto get(@PathVariable Long id, Authentication auth) { 
        User u = (auth == null) ? null : currentUser(auth);
        return EntityMapper.toDto(postService.getByIdVisibleTo(id, u), u);
    }

    @GetMapping
    public Page<PostDto> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication auth) {
        User u = (auth == null) ? null : currentUser(auth);
        return postRepository.findAllByHiddenFalse(PageRequest.of(page, size)).map(p -> EntityMapper.toDto(p, u));
    }
}
