package org.georchestra.gateway.security.exceptions;

@SuppressWarnings("serial")
public class PendingUserException extends RuntimeException {
    /**
     * Constructs a new {@code PendingUserException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public PendingUserException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code PendingUserException} without a detail message.
     */
    public PendingUserException() {
    }
}
