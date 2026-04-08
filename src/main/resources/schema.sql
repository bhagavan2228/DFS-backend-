-- Manual schema initialization for Element Collections
-- This ensures the tables exist before Hibernate applies foreign key constraints

CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT NOT NULL,
    tags VARCHAR(255),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts(id)
);

CREATE TABLE IF NOT EXISTS post_attachments (
    post_id BIGINT NOT NULL,
    attachments VARCHAR(255),
    CONSTRAINT fk_post_attachments_post FOREIGN KEY (post_id) REFERENCES posts(id)
);
