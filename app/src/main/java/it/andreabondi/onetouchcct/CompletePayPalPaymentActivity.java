package it.andreabondi.onetouchcct;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class CompletePayPalPaymentActivity extends AppCompatActivity {

    TextView textMessage;
    Button completeButton;
    List<String> params;
    static final String URL = "/pptest/process.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_pay_pal_payment);

        textMessage = (TextView) findViewById(R.id.textMessage);
        completeButton = (Button) findViewById(R.id.completeButton);

        addListenerOnButton();

        params = getIntent().getData().getPathSegments();
        if(params.get(0).equals("return")) {
            textMessage.setText("Token: " + params.get(1) + ", PayerID: " + params.get(2));
        }
        else if(params.get(0).equals("cancel")) {
            textMessage.setText("Payment cancelled by user");
            completeButton.setEnabled(false);
        }

    }

    public void addListenerOnButton(){
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                RequestParams rp = new RequestParams();
                rp.add("token", params.get(1));
                rp.add("PayerID", params.get(2));

                HttpUtils.get(URL, rp, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray
                        Log.d("asd", "---------------- this is response : " + response);
                        try {
                            JSONObject serverResp = new JSONObject(response.toString());
                            textMessage.setText("Payment " + serverResp.getString("ACK"));
                            completeButton.setVisibility(View.INVISIBLE);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }
}
