package helloworldapp;

import io.temporal.rde.httpserver.RDEHttpServer;

public class CodecServer {
    public static void main(String[] args) throws Exception {
        new RDEHttpServer(InitiateHelloWorld.codecs(), 7777).start();
    }
}
