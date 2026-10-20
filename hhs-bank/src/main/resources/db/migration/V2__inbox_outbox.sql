CREATE TABLE inbox_message (
                               consumer      VARCHAR2(100) NOT NULL,
                               event_id      VARCHAR2(64)  NOT NULL,
                               event_type    VARCHAR2(100) NOT NULL,
                               processed_at  TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
                               CONSTRAINT pk_inbox_message PRIMARY KEY (consumer, event_id)
);
CREATE INDEX ix_inbox_processed_at ON inbox_message (processed_at);

CREATE TABLE outbox_event (
                              id              VARCHAR2(64)   NOT NULL,
                              aggregate_type  VARCHAR2(50)   NOT NULL,
                              aggregate_id    VARCHAR2(64)   NOT NULL,
                              event_type      VARCHAR2(100)  NOT NULL,
                              topic           VARCHAR2(200)  NOT NULL,
                              payload         CLOB           NOT NULL,
                              request_id      VARCHAR2(36),
                              created_at      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
                              published_at    TIMESTAMP WITH TIME ZONE,
                              attempt_count   NUMBER(10)     DEFAULT 0 NOT NULL,
                              last_error      VARCHAR2(1000),
                              CONSTRAINT pk_outbox_event PRIMARY KEY (id),
                              CONSTRAINT ck_outbox_payload_json CHECK (payload IS JSON)
);
CREATE INDEX ix_outbox_pending ON outbox_event (published_at, created_at);

CREATE TABLE shedlock (
                          name        VARCHAR2(64)  NOT NULL,
                          lock_until  TIMESTAMP(3)  NOT NULL,
                          locked_at   TIMESTAMP(3)  NOT NULL,
                          locked_by   VARCHAR2(255) NOT NULL,
                          CONSTRAINT pk_shedlock PRIMARY KEY (name)
);