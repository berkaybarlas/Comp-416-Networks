package utils;

public enum MessageType {
    /** Authentication request */
    AUTH_REQUEST(0),
    /** Authentication challange */
    AUTH_CHALLANGE(1),
    /** Authentication failed */
    AUTH_FAIL(2),
    /** Authentication successfull */
    AUTH_SUCCESS(3);

    public final int value;

    private MessageType(int value) {
        this.value = value;
    }
}