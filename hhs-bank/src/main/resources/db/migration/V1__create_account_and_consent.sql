CREATE TABLE account (
                         account_ref  VARCHAR2(40)  NOT NULL,
                         customer_id  VARCHAR2(40)  NOT NULL,
                         iban         VARCHAR2(34)  NOT NULL,
                         currency     VARCHAR2(3)   NOT NULL,
                         created_at   TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
                         CONSTRAINT pk_account PRIMARY KEY (account_ref),
                         CONSTRAINT uq_account_iban UNIQUE (iban)
);
CREATE INDEX ix_account_customer ON account (customer_id);

CREATE TABLE account_balance (
                                 account_ref  VARCHAR2(40)  NOT NULL,
                                 amount       NUMBER(19,4)  NOT NULL,
                                 currency     VARCHAR2(3)   NOT NULL,
                                 as_of        TIMESTAMP WITH TIME ZONE NOT NULL,
                                 CONSTRAINT pk_account_balance PRIMARY KEY (account_ref),
                                 CONSTRAINT fk_balance_account FOREIGN KEY (account_ref) REFERENCES account (account_ref)
);

CREATE TABLE consent (
                         consent_id   VARCHAR2(64)  NOT NULL,
                         customer_id  VARCHAR2(40)  NOT NULL,
                         tpp_code     VARCHAR2(10)  NOT NULL,
                         status       VARCHAR2(30)  NOT NULL,
                         expires_at   TIMESTAMP WITH TIME ZONE NOT NULL,
                         created_at   TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
                         updated_at   TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
                         version      NUMBER(19)    DEFAULT 0 NOT NULL,
                         CONSTRAINT pk_consent PRIMARY KEY (consent_id),
                         CONSTRAINT ck_consent_status CHECK (status IN
                                                             ('AWAITING_AUTHORIZATION', 'AUTHORIZED', 'AUTHORIZATION_USED', 'REVOKED', 'EXPIRED'))
);
CREATE INDEX ix_consent_customer ON consent (customer_id);

CREATE TABLE consent_permission (
                                    consent_id   VARCHAR2(64)  NOT NULL,
                                    permission   VARCHAR2(30)  NOT NULL,
                                    CONSTRAINT pk_consent_permission PRIMARY KEY (consent_id, permission),
                                    CONSTRAINT fk_cperm_consent FOREIGN KEY (consent_id) REFERENCES consent (consent_id)
);

CREATE TABLE consent_account (
                                 consent_id   VARCHAR2(64)  NOT NULL,
                                 account_ref  VARCHAR2(40)  NOT NULL,
                                 CONSTRAINT pk_consent_account PRIMARY KEY (consent_id, account_ref),
                                 CONSTRAINT fk_cacc_consent FOREIGN KEY (consent_id) REFERENCES consent (consent_id),
                                 CONSTRAINT fk_cacc_account FOREIGN KEY (account_ref) REFERENCES account (account_ref)
);
CREATE INDEX ix_consent_account_ref ON consent_account (account_ref);