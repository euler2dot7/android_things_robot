package com.samgol.robot.remoteControl;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.WebSocket;
import fi.iki.elonen.WebSocketFrame;
import fi.iki.elonen.WebSocketFrame.CloseCode;

class Ws extends WebSocket {

    private CommandListener commandListener;

    public Ws(IHTTPSession handshakeRequest, CommandListener commandListener) {
        super(handshakeRequest);
        this.commandListener = commandListener;
        System.out.println("user connected......");
    }

    @Override
    protected void onPong(WebSocketFrame pongFrame) {
        System.out.println("user pong.......");
    }

    @Override
    protected void onMessage(WebSocketFrame messageFrame) {
//        System.out.println("User message : " + messageFrame.getTextPayload().trim());
        commandListener.onCommand(messageFrame.getTextPayload().trim());
    }

    @Override
    protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
        System.out.println("user disconnected.....");
    }

    @Override
    protected void onException(IOException e) {
        System.out.println(e.getMessage());
    }

}