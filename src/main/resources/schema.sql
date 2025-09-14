
CREATE TABLE IF NOT EXISTS book
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    title          VARCHAR(255) NOT NULL,
    author         VARCHAR(255) NOT NULL,
    published_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_author (author),
    INDEX idx_author_published_date (author, published_date)
);
