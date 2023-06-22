package com.crandall.familymapclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ApplicationLogic.DataCache;
import ApplicationLogic.ServerProxy;
import BackgroundTasks.RegisterTask;
import Request.RegisterRequest;


public class DataCacheTest {
    private DataCache cache = DataCache.getInstance();
    private ServerProxy serverProxy;
    private Handler messageHandler;
    private RegisterRequest request;
    private boolean success = false;
    private static final String SUCCESS_KEY = "SuccessKey";

    @Before
    public void setUp() {
        request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle bundle = message.getData();
                success = bundle.getBoolean(SUCCESS_KEY, false);

                assertTrue(success);
            }
        };

        RegisterTask task = new RegisterTask(messageHandler, request,
                "localhost", "8080");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
    }

    @After
    public void tearDown() {
        serverProxy = new ServerProxy("localhost", "8080");
        serverProxy.clear();
    }

    @Test
    public void chronologicalEventsPass() {
        String firstName = cache.getUserFirstName();
        String lastName = cache.getUserLastName();

        assertNotEquals(firstName, lastName);
    }
}
