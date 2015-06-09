package com.hoangnguyen.buddiesradar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import com.hoangnguyen.buddiesradar.R;
import com.hoangnguyen.buddiesradar.data.LocalDb;
import com.hoangnguyen.buddiesradar.models.User;
import com.hoangnguyen.buddiesradar.tools.NotificationHelper;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private TextView mTvRegister;
    private ProgressBar mProgressBar;

    private LocalDb mLocalDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        init();
    }

    private void init() {
        mLocalDb = LocalDb.getInstance();

        mEtUsername = (EditText) findViewById(R.id.etUsername);
        mEtPassword = (EditText) findViewById(R.id.etPassword);
        mBtnLogin = (Button) findViewById(R.id.btnLogin);
        mTvRegister = (TextView) findViewById(R.id.tvRegister);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mBtnLogin.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btnLogin:
                login();
                break;
            case R.id.tvRegister:
                goToRegister();
                break;
        }
    }

    private void login() {
        String username = mEtUsername.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            // The input are empty, show an alert
            NotificationHelper.alert(this, getString(R.string.dialog_error_title),
                    getString(R.string.login_invalid_inputs_message));
        } else {
            showProgressBar();
            // Log the new user in Parse.com
            User.logInInBackground(username, password, new LogInCallback() {

                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    hideProgressBar();

                    if (e == null) {
                        // Logged in successfully
                        mLocalDb.setCurrentUser((User) parseUser);
                        goToMain();
                    } else {
                        NotificationHelper.alert(LoginActivity.this, getString(R.string.dialog_error_title),
                                e.getMessage());
                    }
                }
            });
        }
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void goToMain() {
        Intent homeIntent = new Intent(this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
    }

    private void goToRegister() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
