package com.appmobiles.exceptions;

import java.util.List;

import static com.appmobiles.Constants.LANGUAGE_ERROR_TEXT_PATTERN;

public class WrongLanguageException extends RuntimeException {

    public WrongLanguageException(String language, List<String> languages) {
        super("Detect wrong text language : " + (String.format(LANGUAGE_ERROR_TEXT_PATTERN, language, languages.toString())));
    }
}
