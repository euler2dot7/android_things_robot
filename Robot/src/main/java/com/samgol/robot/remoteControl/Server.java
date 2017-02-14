package com.samgol.robot.remoteControl;


import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.IWebSocketFactory;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.WebSocket;
import fi.iki.elonen.WebSocketResponseHandler;

public class Server extends NanoHTTPD implements ImageConsumer {
    private WebSocketResponseHandler responseHandler;
    private AssetManager assetManager;
    private Ws mWs;

    private IWebSocketFactory webSocketFactory = new IWebSocketFactory() {
        @Override
        public WebSocket openWebSocket(IHTTPSession handshake) {
            mWs = new Ws(handshake, commandListener);
            return mWs;
        }
    };

    private CommandListener commandListener;

    public Server(int port, CommandListener commandListener, AssetManager assetManager) {
        super(port);
        this.commandListener = commandListener;
        this.assetManager = assetManager;

        responseHandler = new WebSocketResponseHandler(webSocketFactory);
    }

    @Override
    public Response serve(IHTTPSession session) {
        NanoHTTPD.Response ws = responseHandler.serve(session);
        if (ws == null) {
            String uri = session.getUri();
            try {
                if (uri.equals("/")) {
//                    InputStream inputStream = assetManager.open("magc.html");
                    InputStream inputStream = assetManager.open("wscontrol.html");
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/html", inputStream);
                }
//				if (uri.equals("/iotRobot.js")) {
//					File file = new File("website/iotRobot.js");
//					return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/javascript", new FileInputStream(file));
//				}
            } catch (Exception e) {
                System.out.println("Somthing wrong");
                e.printStackTrace();
                new NanoHTTPD.Response("<html><body style='color:red;font-family: Consolas;'>hello, i am runing....</body></html>");
            }
        }
        return ws;
    }

    @Override
    public void onBase64Image(String msg) {
        try {
            mWs.send("data:image/png;base64," + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMsg(String msg) {
        try {
            if (mWs != null)
                mWs.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
