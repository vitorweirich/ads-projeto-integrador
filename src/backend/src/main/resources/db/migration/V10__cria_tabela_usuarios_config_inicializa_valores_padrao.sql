CREATE TABLE USER_SETTINGS (
    user_id BIGINT NOT NULL PRIMARY KEY,
    storage_limit_bytes BIGINT NOT NULL DEFAULT 2147483648,
    max_video_retention_days INT NOT NULL DEFAULT 1,
    modified_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES USERS(id)
);

INSERT INTO
    USER_SETTINGS (
        user_id,
        storage_limit_bytes,
        max_video_retention_days
    )
SELECT
    id,
    2147483648,
    1
FROM
    USERS;