package dto;

import entity.Currency;

import java.math.BigDecimal;

public record ExchangeRateDto(Integer id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {


    @Override
    public String toString() {
        return "ExchangeRateDto{" +
               "id=" + id +
               ", baseCurrency=" + baseCurrency +
               ", targetCurrency=" + targetCurrency +
               ", rate=" + rate +
               '}';
    }
}
