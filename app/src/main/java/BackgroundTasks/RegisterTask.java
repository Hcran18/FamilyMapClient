package BackgroundTasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ApplicationLogic.DataCache;
import ApplicationLogic.ServerProxy;
import Request.RegisterRequest;
import Result.RegisterResult;

public class RegisterTask implements Runnable {
    private final Handler messageHandler;
    private final RegisterRequest request;
    private final String serverHost;
    private final String serverPort;

    public RegisterTask(Handler messageHandler, RegisterRequest request, String serverHost, String serverPort) {
        this.messageHandler = messageHandler;
        this.request = request;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    private static final String SUCCESS_KEY = "SuccessKey";

    @Override
    public void run() {

        ServerProxy serverProxy = new ServerProxy(serverHost, serverPort);

        RegisterResult result = serverProxy.register(request);

        if(!result.isSuccess()) {
            System.out.println("Register failed");
            sendMessage(false);
        }
        else {
            System.out.println("Register was a success");

            DataCache cache = DataCache.getInstance();
            cache.setUserUsername(result.getUsername());
            cache.setUserAuthtoken(result.getAuthtoken());
            cache.setUserPersonID(result.getPersonID());

            System.out.println("Get Data Task Submitted");

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

        System.out.println("Register message sent");

        messageHandler.sendMessage(message);
    }
}
