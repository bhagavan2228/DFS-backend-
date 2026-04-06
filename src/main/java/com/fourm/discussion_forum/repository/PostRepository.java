package com.fourm.discussion_forum.repository;
import com.fourm.discussion_forum.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategoryId(Long categoryId);
    List<Post> findByUserId(Long userId);
}
