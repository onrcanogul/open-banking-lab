package io.github.onrcanogul.openbanking.hhsbank.application.consent;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentNotFoundException;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.repository.AccountRepository;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * What the bank's GKD channel needs to show the customer: the consent as requested by the TPP and the
 * customer's accounts to choose from. Not status-gated; the GKD channel decides what each status means.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GkdConsentQueryService {

    private final ConsentRepository consentRepository;
    private final AccountRepository accountRepository;

    public GkdConsentView get(String consentId) {
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ConsentNotFoundException(consentId));
        return new GkdConsentView(consent, accountRepository.findByCustomer(consent.customerId()));
    }

    public record GkdConsentView(Consent consent, List<Account> customerAccounts) {}
}
