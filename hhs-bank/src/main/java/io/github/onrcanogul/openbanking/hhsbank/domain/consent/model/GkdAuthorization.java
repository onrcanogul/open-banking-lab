package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

import java.time.OffsetDateTime;

public record GkdAuthorization(String method, String redirectUrl, OffsetDateTime deadline) {}
