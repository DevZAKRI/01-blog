package com.zerooneblog.blog.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.blog.model.Report;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.ReportRepository;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.util.HtmlSanitizer;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final HtmlSanitizer htmlSanitizer;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository, HtmlSanitizer htmlSanitizer) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.htmlSanitizer = htmlSanitizer;
    }

    public Report reportUser(Long targetUserId, User reporter, String reason) {
        User target = userRepository.findById(targetUserId).orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        Report r = new Report();
        r.setReporter(reporter);
        r.setTargetUser(target);
        r.setReason(htmlSanitizer.sanitizePlainText(reason));
        return reportRepository.save(r);
    }
}
