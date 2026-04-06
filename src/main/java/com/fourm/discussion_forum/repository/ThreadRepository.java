package com.fourm.discussion_forum.repository;

import com.fourm.discussion_forum.entity.Thread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThreadRepository extends JpaRepository<Thread, Long> {
    List<Thread> findByTitleContainingIgnoreCase(String title);
}
