package it.andreabondi.onetouchcct;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.paypal.android.lib.riskcomponent.RiskComponent;
import com.paypal.android.lib.riskcomponent.SourceApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "/pptest/process.php";
    private static final String PAYPAL_URL = "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=";

    private ImageButton buyButton;

    //=======================================
    //region Chrome Custom Tabs items

    private static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";

    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;
    private CustomTabsIntent customTabsIntent;

    //endregion
    //=======================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Uncomment the following line to enable debug for Magnes Risk Library
        //System.setProperty("dyson.debug.mode", Boolean.TRUE.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addListenerOnButton();

        //=======================================
        //region Setup Chrome Custom Tabs
        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                //Pre-warming
                mClient = customTabsClient;
                mClient.warmup(0L);
                //Initialize a session as soon as possible.
                mCustomTabsSession = mClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(MainActivity.this, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);

        customTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setShowTitle(false)
                .build();
        //endregion
        //=======================================
    }

    private void addListenerOnButton() {

        buyButton = (ImageButton) findViewById(R.id.buyNowButton);

        buyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                buyButton.setEnabled(false);

                RequestParams rp = new RequestParams();
                rp.add("paypal", "setEC");

                Toast.makeText(MainActivity.this,
                        "Sending SetExpressCheckout to server", Toast.LENGTH_SHORT).show();

                HttpUtils.get(URL, rp, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("cct", "---------------- SetEC response : " + response);
                        try {
                            JSONObject serverResp = new JSONObject(response.toString());

                            // This is the PayPal EC token received after the SetEC
                            String token = serverResp.getString("TOKEN");

                            //=======================================
                            //region Initialise Magnes Risk Library
                            Map<String, Object> additionalParams = new HashMap<>();
                            additionalParams.put("RISK_MANAGER_PAIRING_ID", token);

                            String riskPairingId = RiskComponent.getInstance().init(MainActivity.this.getApplicationContext(),
                                    SourceApp.UNKNOWN,
                                    "0.1",
                                    additionalParams);
                            //endregion
                            //=======================================

                            // Launch Chrome Custom Tab with PayPal authentication
                            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(PAYPAL_URL +  token));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        RiskComponent.getInstance().pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        String riskPairingId = RiskComponent.getInstance().resume(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RiskComponent.getInstance().stop();
    }


}
