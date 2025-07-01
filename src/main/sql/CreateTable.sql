create table Currencies
(
    id INTEGER primary key autoincrement,
    code TEXT not null unique,
    full_name TEXT not null,
    sign      TEXT not null unique,
    check (length(code) = 3),
    check (length(full_name) <= 50),
    check (length(sign) <= 5)
);

create unique index currency_code_idx
    on Currencies (code);

create table ExchangeRates
(
    id               INTEGER primary key autoincrement,
    BaseCurrencyId   INTEGER not null references Currencies,
    TargetCurrencyId INTEGER not null references Currencies,
    Rate             DECIMAL(10, 6) not null,
    unique (BaseCurrencyId, TargetCurrencyId),
    check (BaseCurrencyId != TargetCurrencyId)
);

create unique index unique_BaseCurrencyId_TargetCurrencyId_idx
    on ExchangeRates (BaseCurrencyId, TargetCurrencyId);

