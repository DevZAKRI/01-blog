package com.zerooneblog.blog.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.PostRepository;
import com.zerooneblog.blog.repository.UserRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
@AutoConfigureMockMvc
@Transactional
public class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private com.zerooneblog.blog.repository.ReportRepository reportRepository;

    @Test
    public void banAndUnbanUser() throws Exception {
        User u = new User();
        u.setUsername("testuser");
        u.setEmail("t@e.com");
        u.setPassword("x");
        userRepository.save(u);

        mockMvc.perform(put("/api/v1/admin/users/" + u.getId() + "/ban").with(user("admin").roles("ADMIN")).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        User banned = userRepository.findById(u.getId()).orElseThrow();
        assertThat(banned.isBanned()).isTrue();

        mockMvc.perform(put("/api/v1/admin/users/" + u.getId() + "/unban").with(user("admin").roles("ADMIN")).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        User unbanned = userRepository.findById(u.getId()).orElseThrow();
        assertThat(unbanned.isBanned()).isFalse();
    }

    @Test
    public void hideAndUnhidePost() throws Exception {
        User u = new User();
        u.setUsername("author");
        u.setEmail("a@a.com");
        u.setPassword("x");
        userRepository.save(u);

        Post p = new Post();
        p.setAuthor(u);
        p.setDescription("hello");
        postRepository.save(p);

        mockMvc.perform(put("/api/v1/admin/posts/" + p.getId() + "/hide").with(user("admin").roles("ADMIN")).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Post hidden = postRepository.findById(p.getId()).orElseThrow();
        assertThat(hidden.isHidden()).isTrue();

        mockMvc.perform(put("/api/v1/admin/posts/" + p.getId() + "/unhide").with(user("admin").roles("ADMIN")).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        Post unhidden = postRepository.findById(p.getId()).orElseThrow();
        assertThat(unhidden.isHidden()).isFalse();
    }

    @Test
    public void updateReportStatus() throws Exception {
        User reporter = new User();
        reporter.setUsername("reporter");
        reporter.setEmail("r@r.com");
        reporter.setPassword("x");
        userRepository.save(reporter);

        User target = new User();
        target.setUsername("target");
        target.setEmail("t@t.com");
        target.setPassword("x");
        userRepository.save(target);

        com.zerooneblog.blog.model.Report report = new com.zerooneblog.blog.model.Report();
        report.setReporter(reporter);
        report.setTargetUser(target);
        report.setReason("spam");
        reportRepository.save(report);

        mockMvc.perform(patch("/api/v1/admin/reports/" + report.getId()).with(user("admin").roles("ADMIN")).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"REVIEWED\"}"))
            .andExpect(status().isOk());

        com.zerooneblog.blog.model.Report updated = reportRepository.findById(report.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("REVIEWED");
    }
}
