CREATE TABLE instruments (
  isin varchar(255) NOT NULL,
`timestamp` DATETIME(6) NULL,
description varchar(500) NULL,
CONSTRAINT instruments_PK PRIMARY KEY (isin)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE candlesticks (
  id INT auto_increment NOT NULL,
  instrument_id varchar(255) NULL,
open_timestamp DATETIME(6) NOT NULL,
close_timestamp DATETIME(6) NOT NULL,
open_price DOUBLE NOT NULL,
close_price DOUBLE NOT NULL,
high_price DOUBLE NOT NULL,
low_price DOUBLE NOT NULL,
CONSTRAINT candlesticks_PK PRIMARY KEY (id),
CONSTRAINT candlesticks_FK FOREIGN KEY (instrument_id) REFERENCES instruments(isin) ON DELETE CASCADE
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;
