package dheeraj.sachan.videoviewenc.nanoHTTPServer;

public enum Status implements NanoHTTPD.Response.IStatus {
    SWITCH_PROTOCOL(101, "Switching Protocols"), OK(200, "OK"), CREATED(201, "Created"), ACCEPTED(202, "Accepted"), NO_CONTENT(204, "No Content"), PARTIAL_CONTENT(206, "Partial Content"), REDIRECT(301,
            "Moved Permanently"), NOT_MODIFIED(304, "Not Modified"), BAD_REQUEST(400, "Bad Request"), UNAUTHORIZED(401,
            "Unauthorized"), FORBIDDEN(403, "Forbidden"), NOT_FOUND(404, "Not Found"), METHOD_NOT_ALLOWED(405, "Method Not Allowed"), RANGE_NOT_SATISFIABLE(416,
            "Requested Range Not Satisfiable"), INTERNAL_ERROR(500, "Internal Server Error");
    private final int requestStatus;
    private final String description;

    Status(int requestStatus, String description) {
        this.requestStatus = requestStatus;
        this.description = description;
    }

    @Override
    public int getRequestStatus() {
        return this.requestStatus;
    }

    @Override
    public String getDescription() {
        return "" + this.requestStatus + " " + description;
    }
}