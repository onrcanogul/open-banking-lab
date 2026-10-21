CREATE TABLE consent_identity (
    consent_id       VARCHAR2(64) NOT NULL,
    identity_type    VARCHAR2(1)  NOT NULL,
    identity_value   VARCHAR2(30) NOT NULL,
    corporate_type   VARCHAR2(1),
    corporate_value  VARCHAR2(30),
    customer_type    VARCHAR2(1)  NOT NULL,
    CONSTRAINT pk_consent_identity PRIMARY KEY (consent_id),
    CONSTRAINT fk_cident_consent FOREIGN KEY (consent_id) REFERENCES consent (consent_id),
    CONSTRAINT ck_cident_customer_type CHECK (customer_type IN ('B', 'K'))
);

CREATE TABLE consent_gkd (
    consent_id    VARCHAR2(64)   NOT NULL,
    method        VARCHAR2(1)    NOT NULL,
    redirect_url  VARCHAR2(1024),
    deadline      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_consent_gkd PRIMARY KEY (consent_id),
    CONSTRAINT fk_cgkd_consent FOREIGN KEY (consent_id) REFERENCES consent (consent_id),
    CONSTRAINT ck_cgkd_method CHECK (method IN ('Y', 'A'))
);

CREATE TABLE consent_transaction_window (
    consent_id    VARCHAR2(64) NOT NULL,
    window_start  TIMESTAMP WITH TIME ZONE NOT NULL,
    window_end    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_consent_tx_window PRIMARY KEY (consent_id),
    CONSTRAINT fk_ctxw_consent FOREIGN KEY (consent_id) REFERENCES consent (consent_id),
    CONSTRAINT ck_ctxw_range CHECK (window_start <= window_end)
);
