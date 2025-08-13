package mapper;

import dto.ExchangeRateDto;
import entity.ExchangeRate;

public class ExchangeRateMapper implements Mapper<ExchangeRate, ExchangeRateDto> {
    public static final ExchangeRateMapper INSTANCE = new ExchangeRateMapper();

    @Override
    public ExchangeRateDto toDto(ExchangeRate entity) {

        return new ExchangeRateDto(entity.getId(), entity.getBaseCurrency(), entity.getTargetCurrency(), entity.getRate());
    }

    @Override
    public ExchangeRate toEntity(ExchangeRateDto dto) {
        return new ExchangeRate(dto.id(), dto.baseCurrency(), dto.targetCurrency(), dto.rate());
    }
}
