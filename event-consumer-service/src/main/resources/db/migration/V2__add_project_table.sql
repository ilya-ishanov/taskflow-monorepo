CREATE TABLE project(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(128) NOT NULL,
    archived BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);