CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS logs
(
    "id"              UUID         NOT NULL DEFAULT gen_random_uuid(),
    "ip"              VARCHAR(15)  NOT NULL,
    "user"            VARCHAR(255),
    "timestamp"       TIMESTAMPTZ  NOT NULL,
    "method"          VARCHAR(10)  NOT NULL,
    "endpoint"        VARCHAR(255) NOT NULL,
    "protocol"        VARCHAR(10)  NOT NULL,
    "status_code"     INTEGER      NOT NULL,
    "body_bytes_sent" INTEGER      NOT NULL,
    "http_referer"    VARCHAR(255),
    "user_agent"      VARCHAR(255) NOT NULL,
    "request_length"  INTEGER      NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS logs_anomalies
(
    "id"              UUID         NOT NULL DEFAULT gen_random_uuid(),
    "timestamp_from"  TIMESTAMPTZ  NOT NULL,
    "timestamp_to"    TIMESTAMPTZ  NOT NULL,
    "level"           VARCHAR(20)  NOT NULL,
    "endpoint"        VARCHAR(255) NOT NULL,
    "number_of_hits"  INTEGER      NOT NULL,
    "anomaly_type"    VARCHAR(255),
    "body_bytes_sent" INTEGER      NOT NULL,
    "request_length"  INTEGER      NOT NULL,
    "message"         TEXT         NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS last_analyzed_timestamp
(
    "id"        VARCHAR     NOT NULL,
    "timestamp" TIMESTAMPTZ NOT NULL,
    PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS idx_logs_timestamp ON logs USING btree ("timestamp");
CREATE INDEX IF NOT EXISTS idx_logs_anomalies_timestamp_from ON logs_anomalies USING btree ("timestamp_from");
CREATE INDEX IF NOT EXISTS idx_logs_anomalies_timestamp_to ON logs_anomalies USING btree ("timestamp_to");
CREATE INDEX IF NOT EXISTS idx_logs_anomaly_type ON logs_anomalies USING btree ("anomaly_type");