package com.crandall.familymapclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import ApplicationLogic.ServerProxy;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.ClearResult;
import Result.EventResult;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;

public class ServerProxyTest {
    ServerProxy serverProxy;

    @Before
    public void setUp() {
        serverProxy = new ServerProxy("localhost", "8080");
        serverProxy.clear();
    }

    @After
    public void tearDown() {
        serverProxy.clear();
    }

    @Test
    public void RegisterPass() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        RegisterResult result = serverProxy.register(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getAuthtoken());
        assertNotNull(result.getPersonID());
        assertNull(result.getMessage());
        assertEquals(request.getUsername(), result.getUsername());
    }

    @Test
    public void RegisterFail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        serverProxy.register(request);

        //Attempt to register again
        RegisterResult result = serverProxy.register(request);

        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getAuthtoken());
        assertNull(result.getPersonID());
        assertNull(result.getUsername());
    }

    @Test
    public void LoginPass() {
        RegisterRequest RegisterRequest = new RegisterRequest();
        RegisterRequest.setUsername("sheila");
        RegisterRequest.setPassword("parker");
        RegisterRequest.setFirstName("Sheila");
        RegisterRequest.setLastName("Parker");
        RegisterRequest.setGender("f");
        RegisterRequest.setEmail("sheila@parker.com");

        serverProxy.register(RegisterRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(RegisterRequest.getUsername());
        loginRequest.setPassword(RegisterRequest.getPassword());

        LoginResult result = serverProxy.login(loginRequest);

        assertTrue(result.isSuccess());
        assertNotNull(result.getAuthtoken());
        assertNotNull(result.getPersonID());
        assertNull(result.getMessage());
        assertEquals(RegisterRequest.getUsername(), result.getUsername());
    }

    @Test
    public void LoginFail() {
        LoginRequest request = new LoginRequest();
        request.setUsername("sheila");
        request.setPassword("parker");

        LoginResult result = serverProxy.login(request);

        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
        assertNull(result.getAuthtoken());
        assertNull(result.getPersonID());
        assertNull(result.getUsername());
    }

    @Test
    public void getPeoplePass() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        RegisterResult RegisterResult = serverProxy.register(request);

        String authtoken = RegisterResult.getAuthtoken();

        PersonResult getPersonResult = serverProxy.getPeople(authtoken);

        assertTrue(getPersonResult.isSuccess());
        assertNotNull(getPersonResult.getData());
        assertNull(getPersonResult.getMessage());
    }

    @Test
    public void getPeopleFail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        serverProxy.register(request);

        String authtoken = "FakeAuthtoken";

        PersonResult result = serverProxy.getPeople(authtoken);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertNotNull(result.getMessage());
    }

    @Test
    public void getEventsPass() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        RegisterResult RegisterResult = serverProxy.register(request);

        String authtoken = RegisterResult.getAuthtoken();

        EventResult getEventsResult = serverProxy.getEvents(authtoken);

        assertTrue(getEventsResult.isSuccess());
        assertNotNull(getEventsResult.getData());
        assertNull(getEventsResult.getMessage());
    }

    @Test
    public void getEventsFail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        serverProxy.register(request);

        String authtoken = "FakeAuthtoken";

        EventResult result = serverProxy.getEvents(authtoken);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertNotNull(result.getMessage());
    }

    @Test
    public void clearPass() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        serverProxy.register(request);

        ClearResult result = serverProxy.clear();

        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
    }

    @Test
    public void clearPass2() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        request.setFirstName("Sheila");
        request.setLastName("Parker");
        request.setGender("f");
        request.setEmail("sheila@parker.com");

        serverProxy.register(request);

        request = new RegisterRequest();
        request.setUsername("hunter");
        request.setPassword("crandall");
        request.setFirstName("Hunter");
        request.setLastName("Crandall");
        request.setGender("m");
        request.setEmail("hunter@crandall.com");

        serverProxy.register(request);

        ClearResult result = serverProxy.clear();

        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
    }
}
