package com.example.infection.utils;

/**
 * A string enum used for retrieving strings internal to  the application that are not stored in the Strings.xml.
 */
public enum StringEnum {
    STATE_SHARED_PREF_KEY("state"),
    RECOUP_SHARED_PREF_KEY("recoup");

    private String string;

    StringEnum(String envUrl) {
        this.string = envUrl;
    }

    public String getString() {
        return string;
    }
}
