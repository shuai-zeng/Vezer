package com.azhei.vezer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private String tempURL = "http://192.168.0.13:3000/api/temps";
    private String humURL = "http://192.168.0.13:3000/api/hums";
    private String rainURL = "http://192.168.0.13:3000/api/rains";
    private String pressURL = "http://192.168.0.13:3000/api/press";

    private static String TAG = MainActivity.class.getSimpleName();

    private TextView tempValue;
    private TextView humValue;
    private TextView rainValue;
    private TextView pressValue;
    private ProgressDialog pDialog;

    private String jsonResponse;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        tempValue = (TextView) v.findViewById(R.id.tempValue);
        humValue = (TextView) v.findViewById(R.id.humValue);
        rainValue = (TextView) v.findViewById(R.id.rainValue);
        pressValue = (TextView) v.findViewById(R.id.pressValue);

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        Button refresh = (Button) v.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWeather(tempURL);
                requestWeather(humURL);
                requestWeather(rainURL);
                requestWeather(pressURL);
            }
        });

        return v;
    }

    private void requestWeather(final String reqURL) {
        showpDialog();

        JsonArrayRequest jsonObjReq = new JsonArrayRequest(Method.GET, reqURL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, response.toString());

                try {

                    JSONObject jsonResponse = (JSONObject) response.get(0);

                    String payload = jsonResponse.getString("payload");

                    float payloadValue = Float.valueOf(payload);

                    switch (reqURL) {
                        case "http://192.168.0.13:3000/api/temps":
                            tempValue.setText(payload + " °C");
                            break;
                        case "http://192.168.0.13:3000/api/hums":
                            humValue.setText(payload + " %");
                            break;
                        case "http://192.168.0.13:3000/api/rains":
                            rainValue.setText(payload + " %");
                            break;
                        case "http://192.168.0.13:3000/api/press":
                            pressValue.setText(payload + "kPa");
                            break;
                    }

                } catch (JSONException e) {
                    Toast.makeText(getActivity(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                hidepDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                hidepDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }


    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
