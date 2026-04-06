package com.fourm.discussion_forum.repository;
import com.fourm.discussion_forum.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category, Long> {}
