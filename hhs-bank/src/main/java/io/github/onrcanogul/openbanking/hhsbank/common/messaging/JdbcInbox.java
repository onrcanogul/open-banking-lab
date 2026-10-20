package io.github.onrcanogul.openbanking.hhsbank.common.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcInbox {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean tryRecord(String consumer, String eventId, String eventType) {
        try {
            jdbcTemplate.update("INSERT INTO inbox_message (consumer, event_id, event_type) VALUES (?, ?, ?)",
                    consumer, eventId, eventType);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

}
