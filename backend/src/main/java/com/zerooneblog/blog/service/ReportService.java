package com.zerooneblog.blog.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.blog.exception.BadRequestException;
import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.Report;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.PostRepository;
import com.zerooneblog.blog.repository.ReportRepository;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.util.HtmlSanitizer;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final HtmlSanitizer htmlSanitizer;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository, 
                        PostRepository postRepository, HtmlSanitizer htmlSanitizer) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.htmlSanitizer = htmlSanitizer;
    }

    public Report reportUser(Long targetUserId, User reporter, String reason) {
        User target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Prevent reporting admin users
        if ("ADMIN".equals(target.getRole())) {
            throw new BadRequestException("Cannot report admin users");
        }
        
        // Prevent self-reporting
        if (target.getId().equals(reporter.getId())) {
            throw new BadRequestException("Cannot report yourself");
        }
        
        Report r = new Report();
        r.setReporter(reporter);
        r.setTargetUser(target);
        r.setReason(htmlSanitizer.sanitizePlainText(reason));
        return reportRepository.save(r);
    }

    public Report reportPost(Long postId, User reporter, String reason) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Prevent reporting posts by admin users
        if (post.getAuthor() != null && "ADMIN".equals(post.getAuthor().getRole())) {
            throw new BadRequestException("Cannot report posts by admin users");
        }
        
        // Prevent reporting own posts
        if (post.getAuthor() != null && post.getAuthor().getId().equals(reporter.getId())) {
            throw new BadRequestException("Cannot report your own posts");
        }
        
        Report r = new Report();
        r.setReporter(reporter);
        r.setTargetPost(post);
        r.setTargetUser(post.getAuthor()); // Also set the author as target for reference
        r.setReason(htmlSanitizer.sanitizePlainText(reason));
        return reportRepository.save(r);
    }
}
