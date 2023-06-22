package com.crandall.familymapclient;

import static com.crandall.familymapclient.PersonActivity.Event_KEY;
import static com.crandall.familymapclient.PersonActivity.Person_KEY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.List;

import ApplicationLogic.DataCache;
import model.Event;
import model.Person;

public class Search_Activity extends AppCompatActivity {
    private DataCache cache = DataCache.getInstance();
    private TextWatcher searchWatcher;
    private EditText searchText;
    private static final int PEOPLE_VIEW_TYPE = 0;
    private static final int EVENT_VIEW_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Iconify.with(new FontAwesomeModule());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(Search_Activity.this));

        List<Person> emptyPersonList = new ArrayList<>();
        List<Event> emptyEventList = new ArrayList<>();

        SearchAdapter adapter = new SearchAdapter(emptyPersonList, emptyEventList);
        recyclerView.setAdapter(adapter);

        searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No implementation needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No implementation needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();

                System.out.println("Query: " + query);

                List<Person> searchedPeople = new ArrayList<>();
                List<Event> searchedEvents = new ArrayList<>();
                if (!query.isEmpty()) {
                    searchedPeople = cache.searchPerson(query);
                    searchedEvents = cache.searchEvents(query);
                }
                else {
                    searchedPeople.clear();
                    searchedEvents.clear();
                }

                adapter.update(searchedPeople, searchedEvents);
            }
        };

        searchText = findViewById(R.id.searchEditText);
        searchText.addTextChangedListener(searchWatcher);
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
        private List<Person> searchedPeople;
        private List<Event> searchedEvents;

        SearchAdapter(List<Person> searchedPeople, List<Event> searchedEvents) {
            this.searchedPeople = searchedPeople;
            this.searchedEvents = searchedEvents;
        }

        public void update(List<Person> searchedPeople, List<Event> searchedEvents) {
            this.searchedPeople.clear();
            this.searchedPeople.addAll(searchedPeople);
            this.searchedEvents.clear();
            this.searchedEvents.addAll(searchedEvents);

            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return position < searchedPeople.size() ? PEOPLE_VIEW_TYPE : EVENT_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.life_family_item, parent, false);

            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if (position < searchedPeople.size()) {
                holder.bind(searchedPeople.get(position));
            }
            else {
                holder.bind(searchedEvents.get(position - searchedPeople.size()));
            }
        }

        @Override
        public int getItemCount() {
            return searchedPeople.size() + searchedEvents.size();
        }

        private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final ImageView imageView;
            private final TextView name;
            private final TextView eventDetails;
            private final int viewType;
            private Person person;
            private Event event;

            public SearchViewHolder(View view, int viewType) {
                super(view);
                this.viewType = viewType;

                itemView.setOnClickListener(this);

                if (viewType == PEOPLE_VIEW_TYPE) {
                    imageView = itemView.findViewById(R.id.lifeFamilyImage);
                    name = itemView.findViewById(R.id.topDetails);
                    eventDetails = findViewById(R.id.bottomDetails);
                }
                else {
                    imageView = itemView.findViewById(R.id.lifeFamilyImage);
                    eventDetails = itemView.findViewById(R.id.topDetails);
                    name = itemView.findViewById(R.id.bottomDetails);
                }
            }

            private void bind(Person person) {
                this.person = person;

                String fullName = person.getFirstName() + " " + person.getLastName();
                name.setText(fullName);

                if (person.getGender().toLowerCase().equals("m")) {
                    Drawable icon = new IconDrawable(Search_Activity.this, FontAwesomeIcons.fa_male)
                            .colorRes(R.color.male_icon_color)
                            .sizeDp(40);
                    imageView.setImageDrawable(icon);
                }
                else {
                    Drawable icon = new IconDrawable(Search_Activity.this, FontAwesomeIcons.fa_female)
                            .colorRes(R.color.female_icon_color)
                            .sizeDp(40);
                    imageView.setImageDrawable(icon);
                }
            }

            private void bind(Event event) {
                this.event = event;

                Drawable icon = new IconDrawable(Search_Activity.this, FontAwesomeIcons.fa_map_marker)
                        .colorRes(R.color.black)
                        .sizeDp(40);
                imageView.setImageDrawable(icon);

                String eventInfo = event.getEventType() + ": " + event.getCity() +
                        ", " + event.getCountry() + " (" + event.getYear() + ")";
                eventDetails.setText(eventInfo);

                Person person = cache.getPerson(event.getPersonID());
                String fullName = person.getFirstName() + " " + person.getLastName();
                name.setText(fullName);

            }

            @Override
            public void onClick(View v) {
                if (viewType == PEOPLE_VIEW_TYPE) {
                    Intent intent = new Intent(Search_Activity.this, PersonActivity.class);
                    intent.putExtra(Person_KEY, person.getPersonID());
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(Search_Activity.this, EventActivity.class);
                    intent.putExtra(Event_KEY, event.getEventID());
                    startActivity(intent);
                }
            }
        }
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
}