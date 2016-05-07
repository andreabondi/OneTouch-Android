package it.andreabondi.onetouchcct;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class CompletePayPalPaymentActivity extends AppCompatActivity {

    private TextView textMessage;
    private Button completeButton;
    private List<String> params;
    private static final String URL = "/pptest/process.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_pay_pal_payment);

        textMessage = (TextView) findViewById(R.id.textMessage);
        completeButton = (Button) findViewById(R.id.completeButton);

        addListenerOnButton();

        // Process the intent url to see if the payment is authorised or cancelled
        params = getIntent().getData().getPathSegments();
        if(params.get(0).equals("return")) {
            textMessage.setText("Token: " + params.get(1) + ", PayerID: " + params.get(2));
        }
        else if(params.get(0).equals("cancel")) {
            textMessage.setText("Payment cancelled by user");
            completeButton.setEnabled(false);
        }

    }

    private void addListenerOnButton(){
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Toast.makeText(CompletePayPalPaymentActivity.this,
                        "Sending DoExpressCheckout to server", Toast.LENGTH_SHORT).show();

                // Send the EC Token and Payer ID received from the SetEC call to the page.
                // These parameters in the get will trigger a call to DoEC to complete the payment.
                // GetExpressCheckoutDetails call skipped for brevity
                RequestParams rp = new RequestParams();
                rp.add("token", params.get(1));
                rp.add("PayerID", params.get(2));

                HttpUtils.get(URL, rp, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("cct", "---------------- DoEC response : " + response);
                        try {
                            JSONObject serverResp = new JSONObject(response.toString());
                            textMessage.setText("Payment: " + serverResp.getString("ACK"));
                            completeButton.setVisibility(View.INVISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }
}
