package com.azhei.vezer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;

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
    private LineChart lineChart;
    private TextView humValue;
    private TextView rainValue;
    private TextView pressValue;

    private ProgressDialog pDialog;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        tempValue = (TextView) v.findViewById(R.id.tempValue);
        lineChart = (LineChart) v.findViewById(R.id.lineChart);
        humValue = (TextView) v.findViewById(R.id.humValue);
        rainValue = (TextView) v.findViewById(R.id.rainValue);
        pressValue = (TextView) v.findViewById(R.id.pressValue);

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        requestWeather(tempURL);
        requestWeather(humURL);
        requestWeather(rainURL);
        requestWeather(pressURL);

        requestChart(tempURL);

        final SwipeRefreshLayout swipeRefresh = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                requestWeather(tempURL);
                requestWeather(humURL);
                requestWeather(rainURL);
                requestWeather(pressURL);

                requestChart(tempURL);

                swipeRefresh.setRefreshing(false);
            }
        });

        tempValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestChart(tempURL);
            }
        });

        humValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestChart(humURL);
            }
        });

        pressValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestChart(pressURL);
            }
        });

        rainValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestChart(rainURL);
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

                    loadData(payload, reqURL);

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

    private void requestChart(final String reqURL) {
        showpDialog();

        JsonArrayRequest jsonObjReq = new JsonArrayRequest(Method.GET, reqURL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, response.toString());

                try {
                    String[] payload = new String[response.length()];

                    for (int i = 0; i < response.length(); i++){
                        JSONObject jsonResponse = (JSONObject) response.get(i);
                        payload[i] = jsonResponse.getString("payload");
                    }

                    loadChart(payload, reqURL);

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

    private void loadData (String data, String URL){

        switch (URL) {
            case "http://192.168.0.13:3000/api/temps":
                tempValue.setText(data + " °C");
                break;
            case "http://192.168.0.13:3000/api/hums":
                humValue.setText(data + " %");
                break;
            case "http://192.168.0.13:3000/api/rains":
                rainValue.setText(data + " %");
                break;
            case "http://192.168.0.13:3000/api/press":
                pressValue.setText(data + "kPa");
                break;
        }
    }

    private void loadChart(String[] data, String URL){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i = 0; i < data.length; i++) {
            entries.add(new Entry(i, Float.valueOf(data[data.length - 1 - i])));
        }

        String labeltag = "";

        switch (URL) {
            case "http://192.168.0.13:3000/api/temps":
                tempValue.setText(data[0] + " °C");
                labeltag = "Temperature";
                break;
            case "http://192.168.0.13:3000/api/hums":
                humValue.setText(data[0] + " %");
                labeltag = "Humidity";
                break;
            case "http://192.168.0.13:3000/api/rains":
                rainValue.setText(data[0] + " %");
                labeltag = "Rain Index";
                break;
            case "http://192.168.0.13:3000/api/press":
                pressValue.setText(data[0] + "kPa");
                labeltag = "Pressure";
                break;
        }

        LineDataSet dataset = new LineDataSet(entries, labeltag);
        dataset.setDrawValues(false);
        dataset.setDrawCircles(false);
        dataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData linedata = new LineData(dataset);
        lineChart.setData(linedata);

        setChartStyle();
    }

    private void setChartStyle() {
        lineChart.setDescription(null);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setEnabled(false);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(false);

        lineChart.invalidate();
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
