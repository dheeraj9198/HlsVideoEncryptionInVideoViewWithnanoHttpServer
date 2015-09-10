package dheeraj.sachan.videoviewenc.nanoHTTPServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.CharacterCodingException;
import java.util.LinkedList;
import java.util.List;

public abstract class 
        WebSocket {
    public static enum State {
        UNCONNECTED, CONNECTING, OPEN, CLOSING, CLOSED
    }

    protected InputStream in;

    protected OutputStream out;

    protected WebSocketFrame.OpCode continuousOpCode = null;

    protected List<WebSocketFrame> continuousFrames = new LinkedList<WebSocketFrame>();

    protected State state = State.UNCONNECTED;

    protected final NanoHTTPD.IHTTPSession handshakeRequest;

    protected final NanoHTTPD.Response handshakeResponse = new NanoHTTPD.Response(
            Status.SWITCH_PROTOCOL, null, (InputStream) null) {
        @Override
        protected void send(OutputStream out) {
            WebSocket.this.out = out;
            state = State.CONNECTING;
            super.send(out);
            state = State.OPEN;
            readWebsocket();
        }
    };

    public WebSocket(NanoHTTPD.IHTTPSession handshakeRequest) {
        this.handshakeRequest = handshakeRequest;
        this.in = handshakeRequest.getInputStream();

        handshakeResponse.addHeader(WebSocketResponseHandler.HEADER_UPGRADE,
                WebSocketResponseHandler.HEADER_UPGRADE_VALUE);
        handshakeResponse.addHeader(WebSocketResponseHandler.HEADER_CONNECTION,
                WebSocketResponseHandler.HEADER_CONNECTION_VALUE);
    }

    public NanoHTTPD.IHTTPSession getHandshakeRequest() {
        return handshakeRequest;
    }

    public NanoHTTPD.Response getHandshakeResponse() {
        return handshakeResponse;
    }

    // --------------------------------IO--------------------------------------

    protected void readWebsocket() {
        try {
            while (state == State.OPEN) {
                handleWebsocketFrame(WebSocketFrame.read(in));
            }
        } catch (CharacterCodingException e) {
            onException(e);
            doClose(WebSocketFrame.CloseCode.InvalidFramePayloadData, e.toString(), false);
        } catch (IOException e) {
            onException(e);
            if (e instanceof WebSocketException) {
                doClose(((WebSocketException) e).getCode(), ((WebSocketException) e).getReason(), false);
            }
        } finally {
            doClose(WebSocketFrame.CloseCode.InternalServerError, "Handler terminated without closing the connection.", false);
        }
    }

    protected void handleWebsocketFrame(WebSocketFrame frame) throws IOException {
        if (frame.getOpCode() == WebSocketFrame.OpCode.Close) {
            handleCloseFrame(frame);
        } else if (frame.getOpCode() == WebSocketFrame.OpCode.Ping) {
            sendFrame(new WebSocketFrame(WebSocketFrame.OpCode.Pong, true, frame.getBinaryPayload()));
        } else if (frame.getOpCode() == WebSocketFrame.OpCode.Pong) {
            onPong(frame);
        } else if (!frame.isFin() || frame.getOpCode() == WebSocketFrame.OpCode.Continuation) {
            handleFrameFragment(frame);
        } else if (continuousOpCode != null) {
            throw new WebSocketException(WebSocketFrame.CloseCode.ProtocolError, "Continuous frame sequence not completed.");
        } else if (frame.getOpCode() == WebSocketFrame.OpCode.Text || frame.getOpCode() == WebSocketFrame.OpCode.Binary) {
            onMessage(frame);
        } else {
            throw new WebSocketException(WebSocketFrame.CloseCode.ProtocolError, "Non control or continuous frame expected.");
        }
    }

    protected void handleCloseFrame(WebSocketFrame frame) throws IOException {
        WebSocketFrame.CloseCode code = WebSocketFrame.CloseCode.NormalClosure;
        String reason = "";
        if (frame instanceof WebSocketFrame.CloseFrame) {
            code = ((WebSocketFrame.CloseFrame) frame).getCloseCode();
            reason = ((WebSocketFrame.CloseFrame) frame).getCloseReason();
        }
        if (state == State.CLOSING) {
            //Answer for my requested close
            doClose(code, reason, false);
        } else {
            //Answer close request from other endpoint and close self
            State oldState = state;
            state = State.CLOSING;
            if (oldState == State.OPEN) {
                sendFrame(new WebSocketFrame.CloseFrame(code, reason));
            }
            doClose(code, reason, true);
        }
    }

    protected void handleFrameFragment(WebSocketFrame frame) throws IOException {
        if (frame.getOpCode() != WebSocketFrame.OpCode.Continuation) {
            //First
            if (continuousOpCode != null) {
                throw new WebSocketException(WebSocketFrame.CloseCode.ProtocolError, "Previous continuous frame sequence not completed.");
            }
            continuousOpCode = frame.getOpCode();
            continuousFrames.clear();
            continuousFrames.add(frame);
        } else if (frame.isFin()) {
            //Last
            if (continuousOpCode == null) {
                throw new WebSocketException(WebSocketFrame.CloseCode.ProtocolError, "Continuous frame sequence was not started.");
            }
            onMessage(new WebSocketFrame(continuousOpCode, continuousFrames));
            continuousOpCode = null;
            continuousFrames.clear();
        } else if (continuousOpCode == null) {
            //Unexpected
            throw new WebSocketException(WebSocketFrame.CloseCode.ProtocolError, "Continuous frame sequence was not started.");
        } else {
            //Intermediate
            continuousFrames.add(frame);
        }
    }

    public synchronized void sendFrame(WebSocketFrame frame) throws IOException {
        frame.write(out);
    }

    // --------------------------------Close-----------------------------------

    protected void doClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        if (state == State.CLOSED) {
            return;
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        state = State.CLOSED;
        onClose(code, reason, initiatedByRemote);
    }

    // --------------------------------Listener--------------------------------

    protected abstract void onPong(WebSocketFrame pongFrame);

    protected abstract void onMessage(WebSocketFrame messageFrame);

    protected abstract void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote);

    protected abstract void onException(IOException e);

    // --------------------------------Public Facade---------------------------

    public void ping(byte[] payload) throws IOException {
        sendFrame(new WebSocketFrame(WebSocketFrame.OpCode.Ping, true, payload));
    }

    public void send(byte[] payload) throws IOException {
        sendFrame(new WebSocketFrame(WebSocketFrame.OpCode.Binary, true, payload));
    }

    public void send(String payload) throws IOException {
        sendFrame(new WebSocketFrame(WebSocketFrame.OpCode.Text, true, payload));
    }

    public void close(WebSocketFrame.CloseCode code, String reason) throws IOException {
        State oldState = state;
        state = State.CLOSING;
        if (oldState == State.OPEN) {
            sendFrame(new WebSocketFrame.CloseFrame(code, reason));
        } else {
            doClose(code, reason, false);
        }
    }
}
