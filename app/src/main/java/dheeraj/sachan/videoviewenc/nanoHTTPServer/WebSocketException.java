package dheeraj.sachan.videoviewenc.nanoHTTPServer;


import java.io.IOException;

public class WebSocketException extends IOException {
    private WebSocketFrame.CloseCode code;
    private String reason;

    public WebSocketException(Exception cause) {
        this(WebSocketFrame.CloseCode.InternalServerError, cause.toString(), cause);
    }

    public WebSocketException(WebSocketFrame.CloseCode code, String reason) {
        this(code, reason, null);
    }

    public WebSocketException(WebSocketFrame.CloseCode code, String reason, Exception cause) {
        super(code + ": " + reason, cause);
        this.code = code;
        this.reason = reason;
    }

    public WebSocketFrame.CloseCode getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }
}
