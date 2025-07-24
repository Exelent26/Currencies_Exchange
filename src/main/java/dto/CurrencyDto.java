package dto;

public record CurrencyDto(Integer id, String code, String fullName, String sign) {

    @Override
    public String toString() {
        return "CurrencyDto{" +
               "id=" + id +
               ", code='" + code + '\'' +
               ", fullName='" + fullName + '\'' +
               ", sign='" + sign + '\'' +
               '}';
    }
}
