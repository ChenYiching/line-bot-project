package com.example.bot.spring.covidcases;

public class Case {
    private String id;
    private String a01;
    private String a02;
    private String a03;
    private String a04;
    private String a05;
    private String a06;


    @Override
    public String toString() {
        return "Case{" +
                "id='" + id + '\'' +
                ", a01='" + a01 + '\'' +
                ", a02='" + a02 + '\'' +
                ", a03='" + a03 + '\'' +
                ", a04='" + a04 + '\'' +
                ", a05='" + a05 + '\'' +
                ", a06='" + a06 + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getA01() {
        return a01;
    }

    public void setA01(String a01) {
        this.a01 = a01;
    }

    public String getA02() {
        return a02;
    }

    public void setA02(String a02) {
        this.a02 = a02;
    }

    public String getA03() {
        return a03;
    }

    public void setA03(String a03) {
        this.a03 = a03;
    }

    public String getA04() {
        return a04;
    }

    public void setA04(String a04) {
        this.a04 = a04;
    }

    public String getA05() {
        return a05;
    }

    public void setA05(String a05) {
        this.a05 = a05;
    }

    public String getA06() {
        return a06;
    }

    public void setA06(String a06) {
        this.a06 = a06;
    }
}
