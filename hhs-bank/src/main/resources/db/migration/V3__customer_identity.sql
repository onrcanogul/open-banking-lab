CREATE TABLE customer_identity (
                                   identity_type   VARCHAR2(1)   NOT NULL,
                                   identity_value  VARCHAR2(30)  NOT NULL,
                                   customer_type   VARCHAR2(1)   NOT NULL,
                                   customer_id     VARCHAR2(40)  NOT NULL,
                                   CONSTRAINT pk_customer_identity PRIMARY KEY (identity_type, identity_value, customer_type),
                                   CONSTRAINT ck_customer_identity_type CHECK (customer_type IN ('B', 'K'))
);
CREATE INDEX ix_customer_identity_customer ON customer_identity (customer_id);
