package com.crandall.familymapclient;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import ApplicationLogic.DataCache;
import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {
    private DataCache cache = DataCache.getInstance();
    private String personId;
    private Person person;
    private List<Event> events;
    private List<Person> people;
    public static final String Person_KEY = "ReceivedPersonKey";
    public static final String Event_KEY = "ReceivedEventKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Iconify.with(new FontAwesomeModule());

        Intent intent = getIntent();
        personId = intent.getStringExtra(Person_KEY);
        person = cache.getPerson(personId);
        System.out.println("Person: " + person.getFirstName());

        events = cache.getPersonEvents().get(person.getPersonID());

        people = cache.getImmediateFamily().get(person.getPersonID());

        TextView firstNameField = findViewById(R.id.firstNameField);
        firstNameField.setText(person.getFirstName());

        TextView lastNameField = findViewById(R.id.lastNameField);
        lastNameField.setText(person.getLastName());

        TextView genderField = findViewById(R.id.genderField);
        if (Objects.equals(person.getGender(), "m")) {
            genderField.setText("Male");
        }
        else if (Objects.equals(person.getGender(), "f")) {
            genderField.setText("Female");
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        expandableListView.setAdapter(new ExpandableListAdapter(PersonActivity.this, person, events, people));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.fragment_maps, menu);

        MenuItem searchItem = menu.findItem(R.id.search_button);
        MenuItem settingsItem = menu.findItem(R.id.settings_button);

        searchItem.setVisible(false);
        settingsItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return true;
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        // Group and family positions
        private static final int LIFE_EVENTS_GROUP_POSITION = 0;
        private static final int FAMILY_GROUP_POSITION = 1;
        private static final int FATHER_ONLY_POSITION = 0;
        private static final int MOTHER_ONLY_POSITION = 1;
        private static final int SPOUSE_ONLY_POSITION = 0;
        private static final int CHILD_ONLY_POSITION = 1;
        private static final int FATHER_POSITION = 0;
        private static final int MOTHER_POSITION = 1;
        private static final int SPOUSE_POSITION = 2;
        private static final int CHILD_POSITION = 3;
        private final Activity activity;
        private Person person;
        private List<Event> events;
        private List<Person> people;

        ExpandableListAdapter(Activity activity, Person person, List<Event> events, List<Person> people) {
            this.activity = activity;
            this.person = person;
            this.events = events;
            this.people = people;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    return events.size();
                case FAMILY_GROUP_POSITION:
                    return people.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    return "Life Events";
                case FAMILY_GROUP_POSITION:
                    return "Family";
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    return events.get(childPosition);
                case FAMILY_GROUP_POSITION:
                    return people.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    titleView.setText("Life Events");
                    break;
                case FAMILY_GROUP_POSITION:
                    titleView.setText("Family");
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView = getLayoutInflater().inflate(R.layout.life_family_item, parent, false);

            System.out.println("Initializing views");
            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    System.out.println("Initializing life view");
                    initializeLifeView(itemView, childPosition);
                    break;
                case FAMILY_GROUP_POSITION:
                    System.out.println("Initializing family view");
                    initializeFamilyView(itemView, childPosition, person);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }

            return itemView;
        }

        private void initializeLifeView(View itemView, final int childPosition) {
            Drawable icon = new IconDrawable(activity, FontAwesomeIcons.fa_map_marker)
                    .colorRes(R.color.black)
                    .sizeDp(40);

            ImageView imageView = itemView.findViewById(R.id.lifeFamilyImage);
            imageView.setImageDrawable(icon);

            TextView topText = itemView.findViewById(R.id.topDetails);
            Event event = cache.getEvent(events.get(childPosition).getEventID());
            String eventString = event.getEventType() + ": " + event.getCity() + ", " +
                    event.getCountry() + " (" + event.getYear() + ")";
            topText.setText(eventString);

            TextView bottomText = itemView.findViewById(R.id.bottomDetails);
            Person person = cache.getPerson(events.get(childPosition).getPersonID());
            String name = person.getFirstName() + " " + person.getLastName();
            bottomText.setText(name);

            LinearLayout newEvent = itemView.findViewById(R.id.Family_Event);
            newEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, EventActivity.class);
                    intent.putExtra(Event_KEY, event.getEventID());
                    startActivity(intent);
                }
            });
        }

        private void initializeFamilyView(View itemView, final int childPosition, Person eventPerson) {
            Person person = cache.getPerson(people.get(childPosition).getPersonID());
            String gender = person.getGender();

            System.out.println("Setting gender");
            Drawable icon;
            if (Objects.equals(gender, "m")) {
                icon = new IconDrawable(activity, FontAwesomeIcons.fa_male)
                        .colorRes(R.color.male_icon_color)
                        .sizeDp(40);
            }
            else {
                icon = new IconDrawable(activity, FontAwesomeIcons.fa_female)
                        .colorRes(R.color.female_icon_color)
                        .sizeDp(40);
            }

            ImageView imageView = itemView.findViewById(R.id.lifeFamilyImage);
            imageView.setImageDrawable(icon);

            TextView topText = itemView.findViewById(R.id.topDetails);
            String name = person.getFirstName() + " " + person.getLastName();
            topText.setText(name);

            System.out.println("Setting family");
            System.out.println("Child Position: " + childPosition);
            System.out.println("Person still: " + eventPerson);
            TextView bottomText = itemView.findViewById(R.id.bottomDetails);
            if (eventPerson.getSpouseID() == null) {
                switch (childPosition) {
                    case FATHER_ONLY_POSITION:
                        bottomText.setText("Father");
                        break;
                    case MOTHER_ONLY_POSITION:
                        bottomText.setText("Mother");
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognized group position");
                }
            }
            else if (eventPerson.getFatherID() == null && eventPerson.getMotherID() == null) {
                switch (childPosition) {
                    case SPOUSE_ONLY_POSITION:
                        bottomText.setText("Spouse");
                        break;
                    case CHILD_ONLY_POSITION:
                        bottomText.setText("Child");
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognized group position");
                }
            }
            else {
                switch (childPosition) {
                    case FATHER_POSITION:
                        bottomText.setText("Father");
                        break;
                    case MOTHER_POSITION:
                        bottomText.setText("Mother");
                        break;
                    case SPOUSE_POSITION:
                        bottomText.setText("Spouse");
                        break;
                    case CHILD_POSITION:
                        bottomText.setText("Child");
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognized group position");
                }
            }

            LinearLayout familyMember = itemView.findViewById(R.id.Family_Event);
            familyMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, PersonActivity.class);
                    intent.putExtra(Person_KEY, person.getPersonID());
                    startActivity(intent);
                }
            });

        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}