CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_amount DOUBLE,
    status VARCHAR(255),
    order_date DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cake_id BIGINT,
    cake_name VARCHAR(255),
    price_snapshot DOUBLE,
    quantity INT,
    order_id BIGINT,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE basket_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cake_id BIGINT NOT NULL,
    cake_name VARCHAR(255) NOT NULL,
    price_snapshot DOUBLE NOT NULL,
    quantity INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
