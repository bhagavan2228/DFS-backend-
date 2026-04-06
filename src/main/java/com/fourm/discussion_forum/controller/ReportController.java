package com.fourm.discussion_forum.controller;

import com.fourm.discussion_forum.dto.ReportRequest;
import com.fourm.discussion_forum.entity.Report;
import com.fourm.discussion_forum.entity.User;
import com.fourm.discussion_forum.repository.ReportRepository;
import com.fourm.discussion_forum.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportController(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<String> createReport(@RequestBody ReportRequest request, Authentication authentication) {
        User reporter = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());

        reportRepository.save(report);

        return ResponseEntity.ok("Signal flagged. Moderation protocol initiated.");
    }
}
