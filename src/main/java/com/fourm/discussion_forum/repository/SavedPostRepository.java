package com.fourm.discussion_forum.repository;
import com.fourm.discussion_forum.entity.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    List<SavedPost> findByUserId(Long userId);
    Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
