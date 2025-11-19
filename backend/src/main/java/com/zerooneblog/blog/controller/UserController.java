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
        User subscriber = userService.findByUsername(auth.getName());
        User target = userService.findById(id);
        userService.subscribe(target, subscriber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<?> unsubscribe(@PathVariable Long id, Authentication auth) {
        User subscriber = userService.findByUsername(auth.getName());
        User target = userService.findById(id);
        userService.unsubscribe(target, subscriber);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Page<UserDto> listUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return userService.listAll(PageRequest.of(page, size)).map(EntityMapper::toDto);
    }

    @GetMapping("/{authorId}/posts")
    public Object listPostsForSubscribedAuthor(@PathVariable Long authorId, Authentication auth,
                                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        User owner = userService.findByUsername(auth.getName());
        // will throw if not subscribed
        return userService.listPostsForSubscription(owner, authorId, PageRequest.of(page, size)).map(com.zerooneblog.blog.mapper.EntityMapper::toDto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@Valid @RequestBody UpdateUserRequest req, Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        User updated = userService.updateProfile(user, req);
        return ResponseEntity.ok(EntityMapper.toDto(updated));
    }
}
