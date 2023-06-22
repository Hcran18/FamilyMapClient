package ApplicationLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import kotlin.collections.MapsKt;
import model.Event;
import model.Person;

public class DataCache {
    private static DataCache instance = new DataCache();

    public static DataCache getInstance() {
        return instance;
    }

    private DataCache() {
    }

    private String UserUsername;
    private String UserAuthtoken;
    private String UserPersonID;

    // Map key = personID
    private Map<String, Person> people;

    // Map key = eventID
    private Map<String, Event> events;

    // Map key = personID
    private Map<String, List<Event>> personEvents;

    // Map key = personID
    private Map<String, List<Person>> immediateFamily;
    private List<Person> allPeople;
    private List<Event> allEvents;

    public void setAllEvents(List<Event> allEvents) {
        this.allEvents = allEvents;
    }

    public String getUserFirstName() {
        Person user = people.get(UserPersonID);
        String userFirstName = user.getFirstName();

        return userFirstName;
    }

    public String getUserLastName() {
        Person user = people.get(UserPersonID);
        String userLastName = user.getLastName();

        return userLastName;
    }

    public List<Person> searchPerson(String query) {
        List<Person> searchedPeople = new ArrayList<>();
        for (Person person : allPeople) {
            if (person.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    person.getLastName().toLowerCase().contains(query.toLowerCase())) {

                searchedPeople.add(person);
            }
        }

        return searchedPeople;
    }

    public List<Event> searchEvents(String query) {
        List<Event> searchedEvents = new ArrayList<>();
        for (Event event : allEvents) {
            String eventYear = String.valueOf(event.getYear());
            if (event.getEventType().toLowerCase().contains(query.toLowerCase()) ||
                    event.getCountry().toLowerCase().contains(query.toLowerCase()) ||
                    event.getCity().toLowerCase().contains(query.toLowerCase()) ||
                    eventYear.contains(query.toLowerCase())) {

                searchedEvents.add(event);
            }
        }

        return searchedEvents;
    }

    public Map<String, List<Event>> getPersonEvents() {
        return personEvents;
    }

    public void setPersonEvents(Map<String, List<Event>> personEvents) {
        this.personEvents = personEvents;
    }

    public Person getPerson(String personID) {
        return people.get(personID);
    }

    public Event getBirth(String personID) {
        List<Event> events = personEvents.get(personID);

        Event birth = null;
        for (Event event : events) {
            if (Objects.equals(event.getEventType().toLowerCase(), "birth")) {
                birth = event;
            }
        }

        return birth;
    }

    public Event getEvent(String eventID) {
        return events.get(eventID);
    }

    public Map<String, List<Person>> getImmediateFamily() {
        return immediateFamily;
    }

    public void setImmediateFamily(Map<String, List<Person>> immediateFamily) {
        this.immediateFamily = immediateFamily;
    }

    public void setUserUsername(String userUsername) {
        UserUsername = userUsername;
    }

    public void setUserAuthtoken(String userAuthtoken) {
        UserAuthtoken = userAuthtoken;
    }

    public void setUserPersonID(String userPersonID) {
        UserPersonID = userPersonID;
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public void setPeople(Map<String, Person> people) {
        this.people = people;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }

    public void setAllPeople(List<Person> allPeople) {
        this.allPeople = allPeople;
    }
}
