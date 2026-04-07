package com.fourm.discussion_forum.service;

import com.fourm.discussion_forum.dto.ReplyRequest;
import com.fourm.discussion_forum.entity.Notification;
import com.fourm.discussion_forum.entity.Reply;
import com.fourm.discussion_forum.entity.Thread;
import com.fourm.discussion_forum.entity.User;
import com.fourm.discussion_forum.repository.NotificationRepository;
import com.fourm.discussion_forum.repository.ReplyRepository;
import com.fourm.discussion_forum.repository.ThreadRepository;
import com.fourm.discussion_forum.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ToxicityService toxicityService;

    public ReplyService(ReplyRepository replyRepository, ThreadRepository threadRepository,
                        UserRepository userRepository, NotificationRepository notificationRepository,
                        ToxicityService toxicityService) {
        this.replyRepository = replyRepository;
        this.threadRepository = threadRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.toxicityService = toxicityService;
    }

    public List<Reply> getRepliesByThreadId(Long threadId) {
        java.util.Objects.requireNonNull(threadId, "Thread ID must not be null");
        return replyRepository.findByThreadId(threadId);
    }

    public Reply createReply(Long threadId, ReplyRequest request, String email) {
        // AI Toxicity Filter Check
        if (toxicityService.isToxic(request.getContent())) {
            throw new RuntimeException("Content rejected: Internal AI filter flagged toxic language.");
        }

        java.util.Objects.requireNonNull(threadId, "Thread ID must not be null");
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        User replier = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reply reply = new Reply();
        reply.setContent(request.getContent());
        
        // Ghost Mode check
        if (Boolean.TRUE.equals(replier.getGhostMode())) {
            reply.setAuthor("Anonymous");
        } else {
            reply.setAuthor(replier.getName());
        }
        
        reply.setThread(thread);

        Reply saved = replyRepository.save(reply);

        // --- Auto-create notification for the thread author ---
        // Only notify if the replier is NOT the thread author
        userRepository.findByName(thread.getAuthor()).ifPresent(threadAuthor -> {
            if (!threadAuthor.getId().equals(replier.getId())) {
                Notification notification = new Notification();
                notification.setRecipient(threadAuthor);
                notification.setActor(replier);
                notification.setType(Notification.NotificationType.REPLY);
                String actorName = Boolean.TRUE.equals(replier.getGhostMode()) ? "Anonymous" : replier.getName();
                notification.setMessage(actorName + " replied to your thread \"" + thread.getTitle() + "\"");
                notification.setIsRead(false);
                notificationRepository.save(notification);
            }
        });

        return saved;
    }

    public java.util.Map<String, Object> toggleLike(Long replyId, String email) {
        java.util.Objects.requireNonNull(replyId, "Reply ID must not be null");
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyLiked = reply.getLikedByUserIds().contains(user.getId());

        if (alreadyLiked) {
            // Unlike — remove user and decrement
            reply.getLikedByUserIds().remove(user.getId());
            reply.setLikes(Math.max(0, reply.getLikes() - 1));
        } else {
            // Like — add user and increment
            reply.getLikedByUserIds().add(user.getId());
            reply.setLikes(reply.getLikes() + 1);
        }

        replyRepository.save(reply);
        return java.util.Map.of("likes", reply.getLikes(), "liked", !alreadyLiked);
    }
}
