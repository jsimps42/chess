package client;

import java.util.Arrays;
import java.util.Scanner;
import com.google.gson.Gson;
import model.*;
import exception.ResponseException;
import server.ServerFacade;

import static ui.EscapeSequences.*;

public class ChessClient {
    
    private String username = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
    }
    
    public enum State {
        SIGNEDOUT,
        SIGNEDIN
    }
}