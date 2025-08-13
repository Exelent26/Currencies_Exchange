package entity;

import java.math.BigDecimal;

public class ExchangeRate {
    private Integer id;
    private final Currency baseCurrency;
    private final Currency targetCurrency;
    private BigDecimal rate;

    public ExchangeRate(Integer id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {

        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public ExchangeRate(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
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

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
               "id=" + id +
               ", baseCurrency=" + (baseCurrency != null ? baseCurrency.getId() : "null") +
               ", TargetCurrency=" + (targetCurrency != null ? targetCurrency.getId() : "null") +
               ", exchangeRate=" + rate +
               '}';
    }
}
