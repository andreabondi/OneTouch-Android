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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    public static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";

    ImageButton buyButton;

    CustomTabsClient mClient;
    CustomTabsSession mCustomTabsSession;
    CustomTabsServiceConnection mCustomTabsServiceConnection;
    CustomTabsIntent customTabsIntent;

    static final String URL = "/pptest/process.php";
    static final String PAYPAL_URL = "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addListenerOnButton();

        /*
            Setup Chrome Custom Tabs
         */
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
        /*
            End custom tabs setup
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void addListenerOnButton() {

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
                        // If the response is JSONObject instead of expected JSONArray
                        Log.d("asd", "---------------- this is response : " + response);
                        try {

                            JSONObject serverResp = new JSONObject(response.toString());
                            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(PAYPAL_URL +  serverResp.getString("TOKEN")));

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                        // Pull out the first event on the public timeline

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.d("asd", "onFailure(int, Header[], String, Throwable) was not overriden, but callback was received", throwable);
                        Log.d("asd", responseString);
                    }
                });
                // Launch Chrome Custom Tabs on click
                // customTabsIntent.launchUrl(MainActivity.this, Uri.parse(URL));
            }
        });

    }

}
