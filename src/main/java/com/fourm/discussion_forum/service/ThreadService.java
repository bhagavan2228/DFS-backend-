package com.fourm.discussion_forum.service;

import com.fourm.discussion_forum.dto.ThreadRequest;
import com.fourm.discussion_forum.entity.Thread;
import com.fourm.discussion_forum.entity.User;
import com.fourm.discussion_forum.repository.ThreadRepository;
import com.fourm.discussion_forum.repository.UserRepository;
import com.fourm.discussion_forum.repository.ReplyRepository;
import com.fourm.discussion_forum.entity.Reply;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

@Service
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;
    private final ToxicityService toxicityService;

    public ThreadService(ThreadRepository threadRepository, UserRepository userRepository, 
                         ReplyRepository replyRepository, ToxicityService toxicityService) {
        this.threadRepository = threadRepository;
        this.userRepository = userRepository;
        this.replyRepository = replyRepository;
        this.toxicityService = toxicityService;
    }

    public List<Thread> getAllThreads() {
        List<Thread> threads = threadRepository.findAll();
        threads.forEach(t -> t.setReplies((int) replyRepository.countByThread_Id(t.getId())));
        return threads;
    }

    public Thread getThreadById(@NonNull Long id) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thread not found"));
        thread.setReplies((int) replyRepository.countByThread_Id(thread.getId()));
        return thread;
    }

    public Thread createThread(ThreadRequest request, String email) {
        // AI Toxicity Filter Check
        if (toxicityService.isToxic(request.getTitle()) || toxicityService.isToxic(request.getContent())) {
            throw new RuntimeException("Content rejected: Internal AI filter flagged toxic language.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Thread thread = new Thread();
        thread.setTitle(request.getTitle());
        thread.setContent(request.getContent());
        
        // Ghost Mode check
        if (Boolean.TRUE.equals(user.getGhostMode())) {
            thread.setAuthor("Anonymous");
        } else {
            thread.setAuthor(user.getName());
        }

        Thread saved = threadRepository.save(thread);
        saved.setReplies(0);
        return saved;
    }

    public List<Thread> getSimilarThreads(String title) {
        if (title == null || title.length() < 3) return List.of();
        return threadRepository.findByTitleContainingIgnoreCase(title);
    }

    public String summarizeThread(@NonNull Long id) {
        Thread thread = getThreadById(id);
        List<Reply> replies = replyRepository.findByThreadId(id);
        
        StringBuilder summary = new StringBuilder();
        summary.append("Topic Analysis: ").append(thread.getTitle()).append("\n\n");
        summary.append("Core Discussion: ").append(thread.getContent().substring(0, Math.min(thread.getContent().length(), 100))).append("...\n\n");
        
        if (!replies.isEmpty()) {
            summary.append("Key Contributions:\n");
            String keyPoints = replies.stream()
                .limit(2)
                .map(r -> "- " + r.getContent().substring(0, Math.min(r.getContent().length(), 50)))
                .collect(Collectors.joining("...\n"));
            summary.append(keyPoints).append("...");
        } else {
            summary.append("No decentralized feedback yet.");
        }
        
        return summary.toString();
    }
}
