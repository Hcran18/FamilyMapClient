package com.crandall.familymapclient;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ApplicationLogic.DataCache;
import BackgroundTasks.LoginTask;
import BackgroundTasks.RegisterTask;
import Request.LoginRequest;
import Request.RegisterRequest;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterLoginFragment extends Fragment {
    private Listener listener;
    public interface Listener {
        void notifyDone();
    }

    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    private static final String SUCCESS_KEY = "SuccessKey";
    private boolean success;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(false);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register_login, container, false);

        Button loginButton = view.findViewById(R.id.signInButton);
        Button registerButton = view.findViewById(R.id.registerButton);

        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        EditText hostText = view.findViewById(R.id.serverHostField);
        EditText portText = view.findViewById(R.id.serverPortField);
        EditText usernameText = view.findViewById(R.id.usernameField);
        EditText passwordText = view.findViewById(R.id.passwordField);
        EditText firstNameText = view.findViewById(R.id.firstNameField);
        EditText lastNameText = view.findViewById(R.id.lastNameField);
        EditText emailText = view.findViewById(R.id.emailField);
        final RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No implementation needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean loginFieldsFilled = !hostText.getText().toString().isEmpty() &&
                        !portText.getText().toString().isEmpty() &&
                        !usernameText.getText().toString().isEmpty() &&
                        !passwordText.getText().toString().isEmpty();

                boolean registerFieldsFilled = !firstNameText.getText().toString().isEmpty() &&
                        !lastNameText.getText().toString().isEmpty() &&
                        !emailText.getText().toString().isEmpty();

                boolean isRadioButtonSelected = radioGroup.getCheckedRadioButtonId() != -1;

                boolean allFieldsFilled = loginFieldsFilled && registerFieldsFilled && isRadioButtonSelected;

                loginButton.setEnabled(loginFieldsFilled);
                registerButton.setEnabled(allFieldsFilled);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No implementation needed
            }
        };

        hostText.addTextChangedListener(textWatcher);
        portText.addTextChangedListener(textWatcher);
        usernameText.addTextChangedListener(textWatcher);
        passwordText.addTextChangedListener(textWatcher);
        firstNameText.addTextChangedListener(textWatcher);
        lastNameText.addTextChangedListener(textWatcher);
        emailText.addTextChangedListener(textWatcher);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                boolean loginFieldsFilled = !hostText.getText().toString().isEmpty() &&
                        !portText.getText().toString().isEmpty() &&
                        !usernameText.getText().toString().isEmpty() &&
                        !passwordText.getText().toString().isEmpty();

                boolean registerFieldsFilled = !firstNameText.getText().toString().isEmpty() &&
                        !lastNameText.getText().toString().isEmpty() &&
                        !emailText.getText().toString().isEmpty();

                boolean isRadioButtonSelected = checkedId != -1;

                boolean allFieldsFilled = loginFieldsFilled && registerFieldsFilled && isRadioButtonSelected;

                registerButton.setEnabled(allFieldsFilled);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText usernameText = view.findViewById(R.id.usernameField);
                final EditText passwordText = view.findViewById(R.id.passwordField);

                LoginRequest request = new LoginRequest();

                request.setUsername(usernameText.getText().toString());
                request.setPassword(passwordText.getText().toString());

                System.out.println("Username and Password received");

                @SuppressLint("HandlerLeak") Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        success = bundle.getBoolean(SUCCESS_KEY, false);

                        System.out.println("bundle recieved");

                        if (success) {
                            DataCache cache = DataCache.getInstance();
                            String firstName = cache.getUserFirstName();
                            String lastName = cache.getUserLastName();

                            String messageText = "Logged in as " + firstName + " " + lastName;
                            Toast.makeText(getContext(), messageText, Toast.LENGTH_LONG).show();

                            if (listener != null) {
                                listener.notifyDone();
                            }
                        }
                        else {
                            Toast.makeText(getContext(), "Login failed", Toast.LENGTH_LONG).show();
                        }

                    }
                };

                System.out.println("Login task to be executed");

                final EditText hostText = view.findViewById(R.id.serverHostField);
                final EditText portText = view.findViewById(R.id.serverPortField);

                LoginTask task = new LoginTask(uiThreadMessageHandler, request,
                        hostText.getText().toString(), portText.getText().toString());

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText usernameText = view.findViewById(R.id.usernameField);
                final EditText passwordText = view.findViewById(R.id.passwordField);
                final EditText firstNameText = view.findViewById(R.id.firstNameField);
                final EditText lastNameText = view.findViewById(R.id.lastNameField);
                final EditText emailText = view.findViewById(R.id.emailField);

                final RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
                int selectedID = radioGroup.getCheckedRadioButtonId();

                String selectedGender = null;
                if (selectedID == R.id.maleButton) {
                    selectedGender = "m";
                }
                else if (selectedID == R.id.femaleButton) {
                    selectedGender = "f";
                }

                RegisterRequest request = new RegisterRequest();

                request.setUsername(usernameText.getText().toString());
                request.setPassword(passwordText.getText().toString());
                request.setFirstName(firstNameText.getText().toString());
                request.setLastName(lastNameText.getText().toString());
                request.setEmail(emailText.getText().toString());
                request.setGender(selectedGender);

                System.out.println("Username: " + usernameText.getText().toString());

                @SuppressLint("HandlerLeak") Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        success = bundle.getBoolean(SUCCESS_KEY, false);

                        System.out.println("bundle recieved");

                        if (success) {
                            DataCache cache = DataCache.getInstance();
                            String firstName = cache.getUserFirstName();
                            String lastName = cache.getUserLastName();

                            String messageText = "Registered as " + firstName + " " + lastName;
                            Toast.makeText(getContext(), messageText, Toast.LENGTH_LONG).show();

                            if (listener != null) {
                                listener.notifyDone();
                            }
                        }
                        else {
                            Toast.makeText(getContext(), "Registration failed", Toast.LENGTH_LONG).show();
                        }

                    }
                };

                System.out.println("Register Task Submitted");

                final EditText hostText = view.findViewById(R.id.serverHostField);
                final EditText portText = view.findViewById(R.id.serverPortField);

                RegisterTask task = new RegisterTask(uiThreadMessageHandler, request,
                        hostText.getText().toString(), portText.getText().toString());

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });

        return view;
    }
}