package BackgroundTasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ApplicationLogic.DataCache;
import ApplicationLogic.ServerProxy;
import Result.EventResult;
import Result.PersonResult;
import model.Event;
import model.Person;

public class GetDataTask implements Runnable {
    private final Handler messageHandler;
    private final String authtoken;
    private final String serverHost;
    private final String serverPort;

    public GetDataTask(Handler messageHandler, String authtoken, String serverHost, String serverPort) {
        this.messageHandler = messageHandler;
        this.authtoken = authtoken;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    private static final String SUCCESS_KEY = "SuccessKey";

    @Override
    public void run() {
        ServerProxy serverProxy = new ServerProxy(serverHost, serverPort);

        PersonResult personResult = serverProxy.getPeople(authtoken);
        EventResult eventResult = serverProxy.getEvents(authtoken);

        DataCache cache = DataCache.getInstance();

        // Store people by PersonID
        Map<String, Person> people = new HashMap<>();;
        for (Person person : personResult.getData()) {
            people.put(person.getPersonID(), person);
        }
        cache.setPeople(people);

        // Store events by eventID
        Map<String, Event> events = new HashMap<>();;
        for (Event event : eventResult.getData()) {
            events.put(event.getEventID(), event);
        }
        cache.setEvents(events);

        // Store the list of events for each personID
        Map<String, List<Event>> personEvents = new HashMap<>();
        for (Event event : events.values()) {
            String personID = event.getPersonID();

            if (!personEvents.containsKey(personID)) {
                personEvents.put(personID, new ArrayList<>());
            }

            List<Event> eventsList = personEvents.get(personID);
            eventsList.add(event);

            eventsList.sort(Comparator.comparing(Event::getYear)
                    .thenComparing(e -> e.getEventType().toLowerCase()));
        }
        cache.setPersonEvents(personEvents);

        // Retrieve the family for each person in order father, mother, spouse, child
        Map<String, List<Person>> immediateFamily = new HashMap<>();
        for (Person person : people.values()){
            String personID = person.getPersonID();

            if (!immediateFamily.containsKey(personID)) {
                immediateFamily.put(personID, new ArrayList<>());
            }

            List<Person> family = immediateFamily.get(personID);

            if (person.getFatherID() != null) {
                Person father = cache.getPerson(person.getFatherID());
                family.add(father);
            }

            if (person.getMotherID() != null) {
                Person mother = cache.getPerson(person.getMotherID());
                family.add(mother);
            }

            if (person.getSpouseID() != null) {
                Person spouse = cache.getPerson(person.getSpouseID());
                family.add(spouse);
            }

            for (Person child : people.values()) {
                if (child.getFatherID() != null && child.getFatherID().equals(personID) ||
                        child.getMotherID() != null && child.getMotherID().equals(personID)) {
                    family.add(child);
                }
            }
        }
        cache.setImmediateFamily(immediateFamily);

        List<Person> allPeople = new ArrayList<>();
        for (Person person : personResult.getData()) {
            allPeople.add(person);
        }
        cache.setAllPeople(allPeople);

        List<Event> allEvents = new ArrayList<>();
        for (Event event : eventResult.getData()) {
            allEvents.add(event);
        }
        cache.setAllEvents(allEvents);

        System.out.println("Get Data Success");

        sendMessage(true);
    }

    private void sendMessage(boolean success) {
        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(SUCCESS_KEY, success);
        message.setData(messageBundle);

        messageHandler.sendMessage(message);
    }
}
