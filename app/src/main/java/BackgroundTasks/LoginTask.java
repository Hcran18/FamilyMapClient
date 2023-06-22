package BackgroundTasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ApplicationLogic.DataCache;
import ApplicationLogic.ServerProxy;
import Request.LoginRequest;
import Result.LoginResult;

public class LoginTask implements Runnable {
    private final Handler messageHandler;
    private final LoginRequest request;
    private final String serverHost;
    private final String serverPort;

    public LoginTask(Handler messageHandler, LoginRequest request, String serverHost, String serverPort) {
        this.messageHandler = messageHandler;
        this.request = request;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    private static final String SUCCESS_KEY = "SuccessKey";

    @Override
    public void run() {
        ServerProxy serverProxy = new ServerProxy(serverHost, serverPort);

        LoginResult result = serverProxy.login(request);

        System.out.println("Request processed in server proxy");

        if(!result.isSuccess()) {
            System.out.println("Login failed");
            sendMessage(false);
        }
        else {
            System.out.println("Login was a success");

            DataCache cache = DataCache.getInstance();
            cache.setUserUsername(result.getUsername());
            cache.setUserAuthtoken(result.getAuthtoken());
            cache.setUserPersonID(result.getPersonID());

            System.out.println("Calling the Get Data Task");

            GetDataTask task = new GetDataTask(messageHandler, result.getAuthtoken(), serverHost, serverPort);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(task);
        }
    }

    private void sendMessage(boolean success) {
        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(SUCCESS_KEY, success);
        message.setData(messageBundle);

        System.out.println("Login message sent");

        messageHandler.sendMessage(message);
    }
}
