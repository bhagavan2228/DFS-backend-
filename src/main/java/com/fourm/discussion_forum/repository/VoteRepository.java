package com.fourm.discussion_forum.repository;
import com.fourm.discussion_forum.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserIdAndPostId(Long userId, Long postId);
    Optional<Vote> findByUserIdAndCommentId(Long userId, Long commentId);
    Optional<Vote> findByUserIdAndReplyId(Long userId, Long replyId);
    List<Vote> findByPostId(Long postId);
}
