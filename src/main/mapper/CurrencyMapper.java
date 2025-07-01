package main.mapper;

import main.dto.CurrencyDto;
import main.entity.Currency;

public class CurrencyMapper implements Mapper<Currency, CurrencyDto> {
    public static final CurrencyMapper INSTANCE = new CurrencyMapper();

    @Override
    public CurrencyDto toDto(Currency entity) {
        return new CurrencyDto(entity.getId(), entity.getCode(), entity.getFullName(), entity.getSign());
    }

    @Override
    public Currency toEntity(CurrencyDto dto) {
        return new Currency(dto.getId(), dto.getCode(), dto.getFullName(), dto.getSign());
    }
}
