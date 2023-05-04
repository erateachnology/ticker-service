-- Create the Price sequence
CREATE SEQUENCE IF NOT EXISTS Prices_SEQ START WITH 1 INCREMENT BY 50;
-- Create the Price table
CREATE TABLE Price (
        id bigint NOT NULL DEFAULT nextval('Prices_SEQ'),
        euc_id varchar(255) not null,
        fiat_symbol varchar(255) not null,
        created_on TIMESTAMP WITH TIME ZONE not null,
        formatted_price varchar(255) not null,
        primary key (id)
);
-- Create the Price index
CREATE unique INDEX price_euc_id_fiat_symbol_created_on_idx ON public.price (euc_id,fiat_symbol,created_on);