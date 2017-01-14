package com.shltr.darrieng.shltr_android.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shltr.darrieng.shltr_android.Model.PwModel;
import com.shltr.darrieng.shltr_android.Pojo.PasswordPojo;
import com.shltr.darrieng.shltr_android.Pojo.UserToken;
import com.shltr.darrieng.shltr_android.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.GONE;

public class SignupActivity extends AppCompatActivity implements Callback<UserToken> {

    @BindView(R.id.debug_button)
    TextView debugButton;

    @BindView(R.id.signup_button)
    Button signupButton;

    @BindView(R.id.login_button)
    Button loginButton;

    @BindView(R.id.button_screen)
    LinearLayout buttonView;

    @BindView(R.id.text_screen)
    LinearLayout textScreenView;

    @BindView(R.id.enterInputView)
    TextInputEditText enterInputView;

    @BindView(R.id.enter_pw_view)
    TextInputEditText enterPwView;

    @BindView(R.id.go_button)
    FloatingActionButton goButton;
    
    Boolean isSigningUp;

    SharedPreferences preferences;

    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        preferences = getSharedPreferences(getString(R.string.base), MODE_PRIVATE);
        getSupportActionBar().hide();
        deterministicSkip();
        signupButton.setOnClickListener(v -> setUpForInput(true));
        loginButton.setOnClickListener(v -> setUpForInput(false));
        goButton.setOnClickListener(v -> {
            if (validateInput()) {
                // send to server, partay
                startNetworking();
            } else {
                // no-op
            }
        });
    }

    private boolean validateInput() {
        String nameInputView = enterInputView.getText().toString();
        String passwordInput = enterPwView.getText().toString();
        boolean isValid = false;
        try {
            Integer.parseInt(nameInputView);
            isValid = true;
        } catch (Exception e) {
            // no-op
        }

        if (nameInputView.contains("@")) {
            if (nameInputView.substring(nameInputView.indexOf("@")).contains(".")) {
                isValid = true;
            }
        }

        if (passwordInput.isEmpty()) {
            isValid = false;
        }

        return isValid;
    }

    private void setUpForInput(boolean isSigningUp) {
        buttonView.setVisibility(GONE);
        this.isSigningUp = isSigningUp;
        textScreenView.setVisibility(View.VISIBLE);
    }

    private void setUpForChoice() {
        textScreenView.setVisibility(View.GONE);
        buttonView.setVisibility(View.VISIBLE);
    }

    /**
     * Skip this activity if user is already logged in.
     */
    private void deterministicSkip() {
        if (preferences.contains(getString(R.string.token))) {
            startActivity(new Intent(this, RescueeActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        if (isSigningUp != null) {
            setUpForChoice();
            isSigningUp = null;
        }
    }

    private void passData(String token) {
        editor = preferences.edit();
        editor.putString(getString(R.string.token), token);
        editor.apply();
    }

    @Override
    public void onResponse(Call<UserToken> call, Response<UserToken> response) {
        if (response.isSuccessful()) {
            debugButton.setText(response.raw().body().toString());
            passData(response.body().getAccess_token());
            startActivity(new Intent(this, RescueeActivity.class));
        } else {
            Toast.makeText(this, "Failed to login/register", Toast.LENGTH_SHORT).show();
            debugButton.setText(response.code() + " " + response.raw().body().toString());
        }
    }

    @Override
    public void onFailure(Call<UserToken> call, Throwable t) {

    }

    public void startNetworking() {
        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(PwModel.LOGIN_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

        PwModel passwordModel = retrofit.create(PwModel.class);

        Call<UserToken> call;
        PasswordPojo pwpj =
            new PasswordPojo(enterInputView.getText().toString(), enterPwView.getText().toString());
        call = passwordModel.loginUser(pwpj);
        call.enqueue(this);
    }
}
