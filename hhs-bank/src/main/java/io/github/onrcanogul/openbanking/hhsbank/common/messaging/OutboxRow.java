package io.github.onrcanogul.openbanking.hhsbank.common.messaging;

import org.springframework.jdbc.core.RowMapper;

record OutboxRow(String id, String aggregateId, String topic, String payload, String requestId) {

    static final RowMapper<OutboxRow> MAPPER = (rs, rowNum) -> new OutboxRow(
            rs.getString("id"),
            rs.getString("aggregate_id"),
            rs.getString("topic"),
            rs.getString("payload"),
            rs.getString("request_id"));
}
