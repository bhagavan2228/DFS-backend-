-- Manual schema initialization for Element Collections
-- This ensures the tables exist before Hibernate applies foreign key constraints
-- Note: Primary Keys are required by the MySQL configuration (sql_require_primary_key=1)

CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT NOT NULL,
    tags VARCHAR(255) NOT NULL,
    PRIMARY KEY (post_id, tags),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS post_attachments (
    post_id BIGINT NOT NULL,
    attachments VARCHAR(255) NOT NULL,
    PRIMARY KEY (post_id, attachments),
    CONSTRAINT fk_post_attachments_post FOREIGN KEY (post_id) REFERENCES posts(id)
) ENGINE=InnoDB;
