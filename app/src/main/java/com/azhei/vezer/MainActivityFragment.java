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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private String tempURL = "http://www.zoggus.com:3000/vezer/hello/temps";
    private String humURL = "http://www.zoggus.com:3000/vezer/hello/hums";
    private String rainURL = "http://www.zoggus.com:3000/vezer/hello/rains";
    private String pressURL = "http://www.zoggus.com:3000/vezer/hello/press";
    private String windURL = "http://www.zoggus.com:3000/vezer/hello/winds";
    private String lightURL = "http://www.zoggus.com:3000/vezer/hello/lights";

    private static String TAG = MainActivity.class.getSimpleName();

    private TextView tempValue;
    private LineChart lineChart;
    private TextView humValue;
    private TextView rainValue;
    private TextView pressValue;
    private TextView windValue;
    private TextView lightValue;

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
        windValue = (TextView) v.findViewById(R.id.windValue);
        lightValue = (TextView) v.findViewById(R.id.lightValue);

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        requestWeather(tempURL);
        requestWeather(humURL);
        requestWeather(rainURL);
        requestWeather(pressURL);
        requestWeather(windURL);
        requestWeather(lightURL);

        requestChart(tempURL);

        final SwipeRefreshLayout swipeRefresh = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                requestWeather(tempURL);
                requestWeather(humURL);
                requestWeather(rainURL);
                requestWeather(pressURL);
                requestWeather(windURL);
                requestWeather(lightURL);

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

        windValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestChart(windURL);
            }
        });

        lightValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestChart(lightURL);
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
                    String[] timestamp = new String[response.length()];

                    for (int i = 0; i < response.length(); i++){
                        JSONObject jsonResponse = (JSONObject) response.get(i);
                        payload[i] = jsonResponse.getString("payload");
                        timestamp[i] = jsonResponse.getString("timestamp");
                    }

                    loadChart(payload, timestamp, reqURL);

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
            case "http://www.zoggus.com:3000/vezer/hello/temps":
                tempValue.setText(data + " °C");
                break;
            case "http://www.zoggus.com:3000/vezer/hello/hums":
                humValue.setText(data + " %");
                break;
            case "http://www.zoggus.com:3000/vezer/hello/rains":
                rainValue.setText(data + " %");
                break;
            case "http://www.zoggus.com:3000/vezer/hello/press":
                pressValue.setText(data + "kPa");
                break;
            case "http://www.zoggus.com:3000/vezer/hello/winds":
                windValue.setText(data + "m/s");
                break;
            case "http://www.zoggus.com:3000/vezer/hello/lights":
                lightValue.setText(data + "lux");
                break;
        }
    }

    private void loadChart(String[] data, String[] timestamp,  String URL){
        ArrayList<Entry> entries = new ArrayList<>();
        String[] time = new String[timestamp.length];

        for(int i = 0; i < data.length; i++) {
            entries.add(new Entry(i, Float.valueOf(data[data.length - 1 - i])));
            time[i] = (timestamp [timestamp.length - 1 - i]).substring(16, 21);
        }

        String labeltag = "";

        switch (URL) {
            case "http://www.zoggus.com:3000/vezer/hello/temps":
                tempValue.setText(data[0] + " °C");
                labeltag = "Temperature";
                break;
            case "http://www.zoggus.com:3000/vezer/hello/hums":
                humValue.setText(data[0] + " %");
                labeltag = "Humidity";
                break;
            case "http://www.zoggus.com:3000/vezer/hello/rains":
                rainValue.setText(data[0] + " %");
                labeltag = "Rain Index";
                break;
            case "http://www.zoggus.com:3000/vezer/hello/press":
                pressValue.setText(data[0] + "kPa");
                labeltag = "Pressure";
                break;
            case "http://www.zoggus.com:3000/vezer/hello/winds":
                windValue.setText(data[0] + "m/s");
                labeltag = "Wind Speed";
                break;
            case "http://www.zoggus.com:3000/vezer/hello/lights":
                lightValue.setText(data[0] + "lux");
                labeltag = "Light Intensity";
                break;
        }

        LineDataSet dataset = new LineDataSet(entries, labeltag);
        dataset.setDrawValues(false);
        dataset.setDrawCircles(false);
        dataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData linedata = new LineData(dataset);
        lineChart.setData(linedata);

        setChartStyle(time);
    }

    private void setChartStyle(final String[] time) {
        lineChart.setDescription(null);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setEnabled(false);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return time[(int) value % time.length];
            }
        });
        xAxis.setLabelCount(4);
        //xAxis.setEnabled(false);


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
