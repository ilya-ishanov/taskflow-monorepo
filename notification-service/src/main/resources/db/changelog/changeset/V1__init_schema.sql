CREATE TABLE notification (
                              id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
                              task_id BIGINT NOT NULL,
                              title VARCHAR(128) NOT NULL,
                              status VARCHAR(128) NOT NULL,
                              timestamp TIMESTAMP NOT NULL
)