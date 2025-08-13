package entity;

public class Currency {
    private Integer id;
    private String code;
    private String fullName;
    private String sign;

    public Currency(){
    }

    public Currency(Integer id, String code, String fullName, String sign) {
        this.id = id;
        this.code = code;
        this.fullName = fullName;
        this.sign = sign;
    }
    public Currency(String code, String fullName, String sign) {
        this.code = code;
        this.fullName = fullName;
        this.sign = sign;
    }

    public Integer getId() {
        return id;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if(code == null || code.length() != 3) {
            throw new IllegalArgumentException("Code must be 3 characters");
        }
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }


    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        if(sign == null || sign.length() > 5) {
            throw new IllegalArgumentException("Sign must be at least 5 characters and cant be null");
        }
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "Currency{" +
               "id=" + id +
               ", code='" + code + '\'' +
               ", name='" + fullName + '\'' +
               ", sign='" + sign + '\'' +
               '}';
    }
}
