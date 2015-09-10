package dheeraj.sachan.videoviewenc.nanoHTTPServer;

public interface IWebSocketFactory {
    WebSocket openWebSocket(NanoHTTPD.IHTTPSession handshake);
}
