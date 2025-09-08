CREATE TABLE contact (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255),
    phone_number VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE customer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    shipping_address VARCHAR(255),
    billing_address VARCHAR(255),
    contacts_id BIGINT,
    cf VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_customer_contact FOREIGN KEY (contacts_id) REFERENCES contact(id)
);

CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    national_code VARCHAR(255),
    category VARCHAR(255),
    supplier_name VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE customer_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255),
    state VARCHAR(255),
    cf VARCHAR(255),
    date TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE order_line (
    id BIGINT NOT NULL AUTO_INCREMENT,
    national_code VARCHAR(255),
    quantity INTEGER,
    line_number VARCHAR(255),
    type VARCHAR(255),
    order_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_line_order FOREIGN KEY (order_id) REFERENCES customer_order(id)
);
