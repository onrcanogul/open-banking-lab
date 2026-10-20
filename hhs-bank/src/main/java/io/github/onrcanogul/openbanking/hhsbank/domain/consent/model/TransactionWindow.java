package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

import java.time.OffsetDateTime;

public record TransactionWindow(OffsetDateTime from, OffsetDateTime to) {}
