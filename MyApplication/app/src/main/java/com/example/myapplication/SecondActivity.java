package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;

public class SecondActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ImageView flagBig;
    ImageView flagImg1;
    ImageView flagImg2;
    ImageView flagImg3;
    TextView resultIntro;
    TextView result1;
    TextView result2;
    TextView result3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

       // final TextView textView = (TextView) findViewById(R.id.text);
        EditText editText = findViewById(R.id.editTextPersonName);

        resultIntro = findViewById(R.id.textViewIntro);
        result1 = findViewById(R.id.textViewResult1);
        result2 = findViewById(R.id.textViewResult2);
        result3 = findViewById(R.id.textViewResult3);

        flagBig = findViewById(R.id.imageViewFlagBig);
        flagImg1 = findViewById(R.id.imageViewFlag1);
        flagImg2 = findViewById(R.id.imageViewFlag2);
        flagImg3 = findViewById(R.id.imageViewFlag3);

        Spinner dropdown = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.actions_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
        dropdown.setSelection(1);

        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    RequestQueue queue = Volley.newRequestQueue(SecondActivity.this);
                    String url ="https://api.nationalize.io/?name=" + editText.getText().toString();
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("reponse1", response.toString());
                            //empty the resultsTexts between guesses
                            resultIntro.setText("");
                            result1.setText("");
                            result2.setText("");
                            result3.setText("");
                            //empty the flagImages between guesses
                            flagBig.setImageResource(android.R.color.transparent);
                            flagImg1.setImageResource(android.R.color.transparent);
                            flagImg2.setImageResource(android.R.color.transparent);
                            flagImg3.setImageResource(android.R.color.transparent);
                            try {
                                JSONArray countriesArr = (JSONArray) response.get("country");
                                String inputName = response.get("name").toString();
                                if(countriesArr.length() == 0) {
                                    resultIntro.setText("No result for '" + inputName +  "'");
                                } else {
                                    resultIntro.setText("Results for '" + inputName + "'");
                                    //we make sure that only maximum three predictions are made
                                    int maxPredictions = 3;
                                    if(countriesArr.length() < maxPredictions){
                                        maxPredictions = countriesArr.length();
                                    }
                                    for (int i = 0; i < maxPredictions ; i++) {
                                        formatResults(countriesArr, i);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("error", error.toString());
                            resultIntro.setText("Something went wrong!");
                        }
                    });
                    queue.add(request);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(i==0) {
            Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(startIntent);
        }
        if(i==2){
           Intent startIntent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(startIntent);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void formatResults(JSONArray countriesArr, int i) throws JSONException {
        JSONObject currItem = (JSONObject) countriesArr.get(i);
        Log.d("currItem", currItem.toString());
        double prob;
        String result = "";
        //set the country
        String country = (String) currItem.get("country_id");
        if(country.isEmpty()){
            country = "OTHER";
        }
        if(!country.equals("OTHER") && i == 0){
            String probStr = currItem.get("probability").toString();
            //if probability is 1 aka 100% it is an int - can only be 1 if exactly one result
            if(probStr.length() == 1){
                prob = 100.0;
            } else {
                prob = (double) currItem.get("probability") * 100;
            }
            String flagURL = "https://flagcdn.com/256x192/" + country.toLowerCase() + ".png";
            Log.d("flag", flagURL);
            Glide.with(SecondActivity.this).load(flagURL).into(flagBig);
            YoYo.with(Techniques.Pulse).duration(1500).repeat(0).playOn(flagBig);
            setResults(i, prob, result, country, result1, flagImg1);
        }
        else if(!country.equals("OTHER") && i == 1){
            prob = (double) currItem.get("probability") * 100;
            //formatting the decimal number
            setResults(i, prob, result, country, result2, flagImg2);
        }
        else if(!country.equals("OTHER") && i == 2){
            prob = (double) currItem.get("probability") * 100;
            setResults(i, prob, result, country, result3, flagImg3);
        }
    }

    private void setResults(int i, double prob, String result, String country, TextView result3, ImageView flagImg3) {
        //formatting the decimal number
        DecimalFormat df = new DecimalFormat("###.#");
        result += i + 1 + ") " + df.format(prob) + "% probability: " + country;
        result3.setText(result);
        String flagURL = "https://flagcdn.com/h80/" + country.toLowerCase() + ".png";
        Glide.with(SecondActivity.this).load(flagURL).into(flagImg3);
    }
}