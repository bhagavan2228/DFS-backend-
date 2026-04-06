package com.fourm.discussion_forum.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "replies")
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    @JsonIgnore
    private Thread thread;

    // Tracks which user IDs have liked this reply (prevents duplicate likes)
    @ElementCollection
    @CollectionTable(name = "reply_likes", joinColumns = @JoinColumn(name = "reply_id"))
    @Column(name = "user_id")
    @JsonIgnore
    private Set<Long> likedByUserIds = new HashSet<>();

    public Reply() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Thread getThread() { return thread; }
    public void setThread(Thread thread) { this.thread = thread; }
    public Set<Long> getLikedByUserIds() { return likedByUserIds; }
    public void setLikedByUserIds(Set<Long> likedByUserIds) { this.likedByUserIds = likedByUserIds; }
}
