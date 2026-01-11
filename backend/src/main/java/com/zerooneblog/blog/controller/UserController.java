package com.zerooneblog.blog.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.dto.request.UpdateUserRequest;
import com.zerooneblog.blog.dto.response.UserDto;
import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDto publicProfile(@PathVariable Long id) {
        User u = userService.findById(id);
        return EntityMapper.toDto(u);
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<?> subscribe(@PathVariable Long id, Authentication auth) {
        User subscriber = userService.findByEmail(auth.getName());
        User target = userService.findById(id);
        userService.subscribe(target, subscriber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<?> unsubscribe(@PathVariable Long id, Authentication auth) {
        User subscriber = userService.findByEmail(auth.getName());
        User target = userService.findById(id);
        userService.unsubscribe(target, subscriber);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Page<UserDto> listUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        System.out.println("[UserController] listUsers called - page: " + page + ", size: " + size);
        Page<UserDto> result = userService.listAll(PageRequest.of(page, size)).map(EntityMapper::toDto);
        System.out.println("[UserController] listUsers returning " + result.getTotalElements() + " total users, " + result.getContent().size() + " in current page");
        return result;
    }

    @GetMapping("/{authorId}/posts")
    public org.springframework.http.ResponseEntity<?> listUserPosts(@PathVariable Long authorId, Authentication auth,
                                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        // Public endpoint - anyone can view a user's posts
        var result = userService.listPostsByAuthor(authorId, PageRequest.of(page, size)).map(p -> {
            // Include like status if user is authenticated
            if (auth != null && auth.getName() != null) {
                try {
                    User currentUser = userService.findByEmail(auth.getName());
                    return com.zerooneblog.blog.mapper.EntityMapper.toDto(p, currentUser);
                } catch (Exception e) {
                    return com.zerooneblog.blog.mapper.EntityMapper.toDto(p);
                }
            }
            return com.zerooneblog.blog.mapper.EntityMapper.toDto(p);
        });
        if (result.getTotalElements() == 0) {
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                "message", "No posts found for this author",
                "content", result.getContent(),
                "totalElements", result.getTotalElements()
            ));
        }
        return org.springframework.http.ResponseEntity.ok(result);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@Valid @RequestBody UpdateUserRequest req, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        User updated = userService.updateProfile(user, req);
        return ResponseEntity.ok(EntityMapper.toDto(updated));
    }
}
