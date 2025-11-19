package com.zerooneblog.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.blog.model.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
