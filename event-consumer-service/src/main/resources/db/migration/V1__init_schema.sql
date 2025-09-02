CREATE TABLE event_log (
          id BIGSERIAL PRIMARY KEY,
          event_type VARCHAR(128) NOT NULL,
          entity_id BIGINT NOT NULL,
          entity_type VARCHAR(128) NOT NULL,
          payload TEXT NOT NULL,
          processed BOOLEAN NOT NULL,
          created_at TIMESTAMP NOT NULL
);