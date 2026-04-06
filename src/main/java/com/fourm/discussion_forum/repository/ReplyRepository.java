package com.fourm.discussion_forum.repository;

import com.fourm.discussion_forum.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByThreadId(Long threadId);

    long countByThread_Id(Long threadId);
}
