package com.crandall.familymapclient;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ApplicationLogic.DataCache;
import model.Event;
import model.Person;

public class MapsFragment extends Fragment {
    private DataCache cache = DataCache.getInstance();
    private Polyline eventSpousePolyline = null;
    private List<Polyline> eventPolylines = new ArrayList<>();
    private List<Polyline> familyTreePolylines = new ArrayList<>();
    private String currSelectedPersonID = null;
    private String givenEventID = null;
    private Event givenEvent;
    private boolean setSpecificEvent = false;

    public static MapsFragment newInstance(String givenEventID, boolean setSpecificEvent) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putString("givenEventID", givenEventID);
        args.putBoolean("setSpecificEvent", setSpecificEvent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        Iconify.with(new FontAwesomeModule());

        Bundle arguments = getArguments();
        if (arguments != null) {
            givenEventID = arguments.getString("givenEventID");
            givenEvent = cache.getEvent(givenEventID);
            setSpecificEvent = arguments.getBoolean("setSpecificEvent");
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(setSpecificEvent);
            setHasOptionsMenu(false);
        }
        else {
            setHasOptionsMenu(true);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_maps, menu);

        MenuItem searchItem = menu.findItem(R.id.search_button);
        MenuItem settingsItem = menu.findItem(R.id.settings_button);

        searchItem.setVisible(!setSpecificEvent);
        settingsItem.setVisible(!setSpecificEvent);

        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(setSpecificEvent);

        searchItem.setIcon(new IconDrawable(requireActivity(), FontAwesomeIcons.fa_search)
                .colorRes(R.color.white)
                .actionBarSize());
        settingsItem.setIcon(new IconDrawable(requireActivity(), FontAwesomeIcons.fa_gear)
                .colorRes(R.color.white)
                .actionBarSize());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.search_button) {
            Intent intent = new Intent(requireContext(), Search_Activity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.settings_button){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        LinearLayout outerLayout = view.findViewById(R.id.outerLayout);
        outerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), PersonActivity.class);
                intent.putExtra(PersonActivity.Person_KEY, currSelectedPersonID);
                startActivity(intent);
            }
        });
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        private final Map<String, Float> eventTypeColors = new HashMap<>();

        @Override
        public void onMapReady(GoogleMap googleMap) {

            // Place markers for all events in users family tree
            for (Person person : cache.getPeople().values()) {
                List<Event> events = cache.getPersonEvents().get(person.getPersonID());

                for (Event event : events) {
                    LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                    float markerColor = getColor(event.getEventType().toLowerCase());

                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(eventLocation)
                            .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

                    marker.setTag(event);
                }
            }

            if (!setSpecificEvent) {
                // Start camera at last event placed
                if (!cache.getEvents().isEmpty()) {
                    Event lastEvent = cache.getEvents().values().iterator().next();
                    LatLng lastLocation = new LatLng(lastEvent.getLatitude(), lastEvent.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 2f));
                }
            }
            else {
                if (!cache.getEvents().isEmpty()) {
                    LatLng lastLocation = new LatLng(givenEvent.getLatitude(), givenEvent.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 6f));
                    setEventDetails(givenEvent);

                    Person person = cache.getPerson(givenEvent.getPersonID());

                    if (person.getSpouseID() != null) {
                        if (getSpouseBirth(person) != null) {
                            Event spouseBirth = getSpouseBirth(person);
                            setSpouseLine(givenEvent, spouseBirth, googleMap);
                        }
                        else {
                            Event spouseEvent = cache.getPersonEvents().get(person.getSpouseID()).get(0);
                            setSpouseLine(givenEvent, spouseEvent, googleMap);
                        }
                    }

                    if (cache.getPersonEvents().get(person.getPersonID()).size() > 0) {
                        List<Event> events = cache.getPersonEvents().get(person.getPersonID());
                        setLifeStoryLines(events, googleMap);
                    }

                    setFamilyTreeLines(givenEvent, 16f, googleMap);
                }

                setSpecificEvent = false;
            }

            // Watch for when a marker is clicked
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                //Retrieve the marker, set event details, and draw lines
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    Event event = (Event) marker.getTag();
                    currSelectedPersonID = event.getPersonID();

                    assert event != null;
                    setEventDetails(event);

                    Person person = cache.getPerson(event.getPersonID());

                    if (person.getSpouseID() != null) {
                        if (cache.getPersonEvents().get(person.getSpouseID()).size() > 0) {
                            Event spouseEvent;
                            if (getSpouseBirth(person) != null) {
                                spouseEvent = getSpouseBirth(person);
                            } else {
                                spouseEvent = cache.getPersonEvents().get(person.getSpouseID()).get(0);
                            }

                            setSpouseLine(event, spouseEvent, googleMap);
                        }
                    }
                    else {
                        if (eventSpousePolyline != null) {
                            eventSpousePolyline.remove();
                        }
                    }

                    List<Event> events = cache.getPersonEvents().get(person.getPersonID());

                    if (eventPolylines.size() >= 1) {
                        for (Polyline polyline : eventPolylines) {
                            polyline.remove();
                        }
                    }
                    if (events.size() > 1) {
                        setLifeStoryLines(events, googleMap);
                    }


                    if (familyTreePolylines.size() >= 1) {
                        for (Polyline polyline : familyTreePolylines) {
                            polyline.remove();
                        }
                    }

                    setFamilyTreeLines(event,16f, googleMap);

                    return false;
                }
            });


        }

        // Assign colors to different event types
        private float getColor(String eventType) {
            if (!eventTypeColors.containsKey(eventType)) {
                float markerColor = getNextColor();
                eventTypeColors.put(eventType, markerColor);
            }

            return eventTypeColors.get(eventType);
        }

        // Grab colors to use
        private float getNextColor() {
            float[] colors = {
                    BitmapDescriptorFactory.HUE_BLUE,
                    BitmapDescriptorFactory.HUE_RED,
                    BitmapDescriptorFactory.HUE_GREEN,
                    BitmapDescriptorFactory.HUE_YELLOW,
                    BitmapDescriptorFactory.HUE_ORANGE,
                    BitmapDescriptorFactory.HUE_VIOLET,
                    BitmapDescriptorFactory.HUE_AZURE,
                    BitmapDescriptorFactory.HUE_MAGENTA,
                    BitmapDescriptorFactory.HUE_CYAN,
                    BitmapDescriptorFactory.HUE_ROSE
            };

            return colors[eventTypeColors.size() % colors.length];
        }
    };
    private void setEventDetails(Event event) {
        Person person = cache.getPerson(event.getPersonID());

        LinearLayout outerLayout = getView().findViewById(R.id.outerLayout);
        outerLayout.removeAllViews();

        // Get ready to set the gender icon
        Drawable genderIcon;
        if (Objects.equals(person.getGender(), "m")) {
            genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male)
                    .colorRes(R.color.male_icon_color)
                    .sizeDp(40);
        } else {
            genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female)
                    .colorRes(R.color.female_icon_color)
                    .sizeDp(40);
        }
        ImageView genderImageView = new ImageView(requireActivity());
        genderImageView.setImageDrawable(genderIcon);

        // Add the image
        outerLayout.addView(genderImageView);

        // Create the innerLayout for the text
        LinearLayout innerLayout = new LinearLayout(requireActivity());
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView eventPersonTextView = new TextView(requireActivity());
        eventPersonTextView.setText(person.getFirstName() + " " + person.getLastName());
        eventPersonTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        eventPersonTextView.setPadding(0, 0, 0, 8);
        eventPersonTextView.setGravity(Gravity.CENTER);

        TextView eventTypeTextView = new TextView(requireActivity());
        eventTypeTextView.setText(event.getEventType() + ": (" + event.getYear() + ")");
        eventTypeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        eventTypeTextView.setPadding(0, 0, 0, 8);
        eventTypeTextView.setGravity(Gravity.CENTER);

        TextView eventPlaceTextView = new TextView(requireActivity());
        eventPlaceTextView.setText(event.getCity() + " " + event.getCountry());
        eventPlaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        eventPlaceTextView.setPadding(0, 0, 0, 8);
        eventPlaceTextView.setGravity(Gravity.CENTER);

        // Add the rest of the views
        innerLayout.addView(eventPersonTextView);
        innerLayout.addView(eventTypeTextView);
        innerLayout.addView(eventPlaceTextView);
        outerLayout.addView(innerLayout);
    }

    private Event getSpouseBirth(Person person) {
        Person spouse = cache.getPerson(person.getSpouseID());

        return cache.getBirth(spouse.getPersonID());
    }

    private void setSpouseLine(Event personEvent, Event spouseBirth, GoogleMap googleMap) {
        LatLng eventLocation = new LatLng(personEvent.getLatitude(), personEvent.getLongitude());
        LatLng spouseEventLocation = new LatLng(spouseBirth.getLatitude(), spouseBirth.getLongitude());

        PolylineOptions spouseLine = new PolylineOptions()
                .add(eventLocation, spouseEventLocation)
                .color(Color.RED);

        if (eventSpousePolyline != null) {
            eventSpousePolyline.remove();
        }

        eventSpousePolyline = googleMap.addPolyline(spouseLine);
    }

    private void setLifeStoryLines(List<Event> events, GoogleMap googleMap) {

        for (int i = 0; i < events.size() - 1; i++) {
            Event event1 = events.get(i);
            Event event2 = events.get(i + 1);
            LatLng event1Location = new LatLng(event1.getLatitude(), event1.getLongitude());
            LatLng event2Location = new LatLng(event2.getLatitude(), event2.getLongitude());

            PolylineOptions eventPolyline = new PolylineOptions()
                    .add(event1Location, event2Location)
                    .color(Color.GREEN);

            eventPolylines.add(googleMap.addPolyline(eventPolyline));
        }
    }

    private void setFamilyTreeLines(Event event, float width, GoogleMap googleMap) {
        Person person = cache.getPerson(event.getPersonID());

        if (person.getFatherID() != null) {
            if (cache.getPersonEvents().get(person.getFatherID()).size() > 0) {
                Event fatherEvent;
                //Check for birth, if none then use first event
                if (cache.getBirth(person.getFatherID()) != null) {
                    fatherEvent = cache.getBirth(person.getFatherID());
                }
                else {
                    fatherEvent = cache.getPersonEvents().get(person.getFatherID()).get(0);
                }

                LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                LatLng fatherLocation = new LatLng(fatherEvent.getLatitude(), fatherEvent.getLongitude());

                PolylineOptions eventToFather = new PolylineOptions()
                        .add(eventLocation, fatherLocation)
                        .color(Color.BLUE)
                        .width(width);

                familyTreePolylines.add(googleMap.addPolyline(eventToFather));

                setFamilyTreeLines(fatherEvent, width - 3f, googleMap);
            }
        }

        if (person.getMotherID() != null) {
            if (cache.getPersonEvents().get(person.getMotherID()).size() > 0) {
                Event motherEvent;
                //Check for birth, if none then use first event
                if (cache.getBirth(person.getMotherID()) != null) {
                    motherEvent = cache.getBirth(person.getMotherID());
                }
                else {
                    motherEvent = cache.getPersonEvents().get(person.getMotherID()).get(0);
                }

                LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                LatLng motherLocation = new LatLng(motherEvent.getLatitude(), motherEvent.getLongitude());

                PolylineOptions eventToMother = new PolylineOptions()
                        .add(eventLocation, motherLocation)
                        .color(Color.YELLOW)
                        .width(width);

                familyTreePolylines.add(googleMap.addPolyline(eventToMother));

                setFamilyTreeLines(motherEvent, width - 3f, googleMap);
            }
        }
    }
}