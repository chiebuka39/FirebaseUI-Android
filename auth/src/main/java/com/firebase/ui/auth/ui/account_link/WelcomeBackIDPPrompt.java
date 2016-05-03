package com.firebase.ui.auth.ui.account_link;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.R;
import com.firebase.ui.auth.choreographer.Controller;
import com.firebase.ui.auth.choreographer.ControllerConstants;
import com.firebase.ui.auth.choreographer.account_link.AccountLinkController;
import com.firebase.ui.auth.choreographer.idp.provider.FacebookProvider;
import com.firebase.ui.auth.choreographer.idp.provider.GoogleProvider;
import com.firebase.ui.auth.choreographer.idp.provider.IDPProvider;
import com.firebase.ui.auth.choreographer.idp.provider.IDPProviderParcel;
import com.firebase.ui.auth.choreographer.idp.provider.IDPResponse;
import com.firebase.ui.auth.ui.BaseActivity;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

public class WelcomeBackIDPPrompt extends BaseActivity implements View.OnClickListener, IDPProvider.IDPCallback {

    private static final String TAG = "WelcomeBackIDPPrompt";

    private IDPProvider mIDPProvider;
    private String mProviderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.sign_in));
        mProviderId = getProviderIdFromIntent();
        setContentView(R.layout.welcome_back_idp_prompt_layout);

        ((TextView) findViewById(R.id.welcome_back_idp_prompt))
                .setText(getIdpPromptString(getEmailFromIntent(), getAppNameFromIntent()));

        IDPProviderParcel parcel;
        switch (mProviderId) {
            case GoogleAuthProvider.PROVIDER_ID:
                parcel = GoogleProvider.createParcel(
                        JSONUtil.getGoogleClientId(getApplicationContext()));
                mIDPProvider = new GoogleProvider(this, parcel);
                break;
            case FacebookAuthProvider.PROVIDER_ID:
                parcel = FacebookProvider.createFacebookParcel(
                        JSONUtil.getFacebookApplicationId(getApplicationContext()));
                mIDPProvider = new FacebookProvider(this, parcel);
                break;
            default:
                Log.w(TAG, "Unknown provider: " + mProviderId);
                finish(RESULT_CANCELED, getIntent());
                return;
        }

        mIDPProvider.setAuthenticationCallback(this);
        findViewById(R.id.welcome_back_idp_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIDPProvider.startLogin(WelcomeBackIDPPrompt.this, getEmailFromIntent());
            }
        });
    }

    private String getIdpPromptString(String email, String appName) {
        String promptStringTemplate = getResources().getString(R.string.welcome_back_idp_prompt);
        return String.format(promptStringTemplate, email, appName, mIDPProvider.getName(this));
    }

    @Override
    protected Controller setUpController() {
        return new AccountLinkController(getApplicationContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mIDPProvider.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        finish(RESULT_OK, getIntent());
    }

    @Override
    public void onSuccess(IDPResponse idpResponse) {
        Intent data = getIntent();
        data.putExtra(ControllerConstants.EXTRA_PROVIDER, mProviderId);
        data.putExtra(ControllerConstants.EXTRA_IDP_RESPONSE, idpResponse);
        finish(RESULT_OK, data);
    }

    @Override
    public void onFailure(Bundle extra) {
        Toast.makeText(getApplicationContext(), "Error signing in", Toast.LENGTH_LONG).show();
        finish(RESULT_FIRST_USER, getIntent());
    }

    private String getAppNameFromIntent() {
        return getIntent().getStringExtra(ControllerConstants.EXTRA_EMAIL);
    }

    private String getProviderIdFromIntent() {
        return getIntent().getStringExtra(ControllerConstants.EXTRA_PROVIDER);
    }

    private String getEmailFromIntent() {
        return getIntent().getStringExtra(ControllerConstants.EXTRA_EMAIL);
    }

    public static Intent createIntent(
            Context context,
            String providerId,
            String appName,
            String email) {
        return new Intent().setClass(context, WelcomeBackIDPPrompt.class)
                .putExtra(ControllerConstants.EXTRA_APP_NAME, appName)
                .putExtra(ControllerConstants.EXTRA_PROVIDER, providerId)
                .putExtra(ControllerConstants.EXTRA_EMAIL, email)
                .putExtra(BaseActivity.EXTRA_ID, AccountLinkController.ID_WELCOME_BACK_IDP);
    }


}
