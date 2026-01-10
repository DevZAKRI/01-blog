package com.zerooneblog.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.User;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByAuthor(User author, Pageable pageable);
    Page<Post> findByAuthorIn(Iterable<User> authors, Pageable pageable);
    Page<Post> findAllByHiddenFalse(Pageable pageable);
    Page<Post> findByAuthorAndHiddenFalse(User author, Pageable pageable);
    Page<Post> findByAuthorInAndHiddenFalse(Iterable<User> authors, Pageable pageable);
}
