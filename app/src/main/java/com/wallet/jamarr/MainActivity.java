package com.wallet.jamarr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.coinbase.android.sdk.OAuth;
import com.coinbase.api.Coinbase;
import com.coinbase.api.CoinbaseBuilder;
import com.coinbase.api.entity.OAuthTokensResponse;
import com.coinbase.api.exception.CoinbaseException;

import java.util.concurrent.Semaphore;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

public class MainActivity extends RoboActivity {

    private static final String CLIENT_ID = "f56039c1befdf3d8f9460a9b4e34e8019474b43a08761a0a5302c8ddecec2640";
    private static final String CLIENT_SECRET = "c2502cdefaf5e78212969f20f085727a86fbe9007805748388ea4e2174a7c301";
    private static final String REDIRECT_URI = "https://wallet-c7ab0.firebaseapp.com/redirect";


    @InjectView(R.id.textView)
    private TextView mTextView;

    public class DisplayEmailTask extends RoboAsyncTask<String> {
        private OAuthTokensResponse mTokens;

        public DisplayEmailTask(OAuthTokensResponse tokens) {
            super(MainActivity.this);
            mTokens = tokens;
        }

        public String call() throws Exception {
            Coinbase coinbase = new CoinbaseBuilder().withAccessToken(mTokens.getAccessToken()).build();
            return coinbase.getUser().getEmail();
        }

        @Override
        public void onException(Exception ex) {
            mTextView.setText("There was an error fetching the user's email address: " + ex.getMessage());
        }

        @Override
        public void onSuccess(String email) {
            mTextView.setText("Success! The user's email address is: " + email);
        }
    }

    public class CompleteAuthorizationTask extends RoboAsyncTask<OAuthTokensResponse> {
        private Intent mIntent;

        public CompleteAuthorizationTask(Intent intent) {
            super(MainActivity.this);
            mIntent = intent;
        }

        @Override
        public OAuthTokensResponse call() throws Exception {
            return OAuth.completeAuthorization(MainActivity.this, CLIENT_ID, CLIENT_SECRET, mIntent.getData());
        }

        @Override
        public void onSuccess(OAuthTokensResponse tokens) {
            new DisplayEmailTask(tokens).execute();
        }

        @Override
        public void onException(Exception ex) {
            mTextView.setText("There was an error fetching access tokens using the auth code: " + ex.getMessage());
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            new CompleteAuthorizationTask(intent).execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            OAuth.beginAuthorization(this, CLIENT_ID, "user", REDIRECT_URI, null);
        } catch (CoinbaseException ex) {
            mTextView.setText("There was an error redirecting to Coinbase: " + ex.getMessage());
        }
    }

}