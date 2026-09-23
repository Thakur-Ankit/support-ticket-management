CREATE TABLE ticket (
    id          UUID PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    priority    VARCHAR(32) NOT NULL,
    status      VARCHAR(32) NOT NULL,
    assignee    VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ticket_created_at_desc ON ticket (created_at DESC);

CREATE TABLE comment (
    id          UUID PRIMARY KEY,
    ticket_id   UUID NOT NULL,
    content     TEXT NOT NULL,
    author      VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id)
);

CREATE INDEX idx_comment_ticket_id ON comment (ticket_id);
