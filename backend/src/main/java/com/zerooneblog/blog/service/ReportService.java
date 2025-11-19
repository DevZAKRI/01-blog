package com.zerooneblog.blog.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.blog.model.Report;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.ReportRepository;
import com.zerooneblog.blog.repository.UserRepository;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public Report reportUser(Long targetUserId, User reporter, String reason) {
        User target = userRepository.findById(targetUserId).orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        Report r = new Report();
        r.setReporter(reporter);
        r.setTargetUser(target);
        r.setReason(reason);
        return reportRepository.save(r);
    }
}
