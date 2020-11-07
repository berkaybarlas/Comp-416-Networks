package owm;

public class RequestError extends Exception {
    String message;

    /** Costum error for wrong requests
     */
    RequestError(String message) {
        this.message = message;
    }

    public String toString() {
        return ("RequestError: " + message);
    }
}
