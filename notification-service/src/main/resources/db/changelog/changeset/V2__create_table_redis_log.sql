CREATE TABLE redis_log (
                           id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
                           notification VARCHAR(128) NOT NULL
)