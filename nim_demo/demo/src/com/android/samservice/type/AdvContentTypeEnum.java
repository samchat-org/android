package com.android.samservice.type;

public enum AdvContentTypeEnum {
    Text(0),
    ImgUrl(1);

    private int value;

    AdvContentTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AdvContentTypeEnum typeOfValue(int value) {
        for (AdvContentTypeEnum e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return Text;
    }
}
