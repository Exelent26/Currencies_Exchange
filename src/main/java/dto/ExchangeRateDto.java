package dto;

import entity.Currency;

import java.math.BigDecimal;

public class ExchangeRateDto {


    private Integer id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;


    public ExchangeRateDto(Integer id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }


    public Integer getId() {
        return id;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

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
