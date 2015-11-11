package org.xbib.query;

/**
 * String tokenizer exception
 */
public class UnterminatedQuotedStringException extends RuntimeException {

    public UnterminatedQuotedStringException(String msg) {
        super(msg);
    }
}
