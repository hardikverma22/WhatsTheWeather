package com.example.android.whatstheweather;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    EditText cityName;
    ImageView icon;
    TextView weatherText,NameTextView,tempTextView ;
    String textToDisplay="";
    LinearLayout linearLayout;
    public class DownloadTask extends AsyncTask<String,Void,String>{
        String result="";
        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection=null;

            try
            {
                url = new URL(urls[0]);
                urlConnection= (HttpURLConnection) url.openConnection();
                //stream to hold the data
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data=reader.read();
                while(data!=-1){
                    char current =(char)data;
                    result+=current;
                    data=reader.read();
                }

                return result;
            }
            catch(Exception e)
            {
                Log.i("Writing it here","in downloadtask catch");
                //anything can not run from this background thread..thts y calling it on UI Thread
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(),"Could not fetch Weather Info",Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(),"Please Check your Internet Connection or City Name",Toast.LENGTH_SHORT).show();
                        linearLayout.setVisibility(View.INVISIBLE);
                    }
                });

                e.printStackTrace();
                return "Something Went Wrong";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            try {
                Log.i("weather",result);
                weatherText.setText("");
                textToDisplay="";
                if(result.contains("city not found")){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

                    //1. simple dailog with 2 buttons
                    builder.setMessage("Please enter a valid City")

                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                }else {

                    String temp = "";

                    String description = "";
                    String iconId="";

                    JSONObject jsonObject = new JSONObject(result);
                    String weather = jsonObject.getString("weather");
                    //Log.i("Weather Info",weather);
                    JSONArray arr = new JSONArray(weather);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject jsonpart = arr.getJSONObject(i);
                        Log.i("main : ", jsonpart.getString("main"));

                        Log.i("description : ", jsonpart.getString("description"));



                        description = jsonpart.getString("description");
                        iconId =jsonpart.getString("icon");
                        if (description != "") {
                            textToDisplay += description + "\r\n";
                        }

                    }

                    String main=jsonObject.getString("main");
                    JSONObject mainObject = new JSONObject(main);
                    temp=mainObject.getString("temp");


                    //setting up the temp and humidity
                    tempTextView.setText(temp+"\u00b0");


                    String cityNameinJson = jsonObject.getString("name");

                    //setting up the city name in result
                    NameTextView.setText(cityNameinJson);

                    //getting the icon based on the api iconcode
                    Resources res = getResources();
                    String mDrawableName = "a"+iconId;
                    int resID = res.getIdentifier(mDrawableName , "drawable", getPackageName());
                    Drawable drawable = ContextCompat.getDrawable( getApplicationContext(), resID);
                    //setting up the ison
                    icon.setImageDrawable(drawable );
                    //setting up the weather text
                    weatherText.setText(textToDisplay);


                    /*
                    Calendar c = Calendar.getInstance();
                    System.out.println("Current time => " + c.getTime());

                    SimpleDateFormat df = new SimpleDateFormat("MMMM dd \n hh:mm a");
                    String formattedDate = df.format(c.getTime());


                    //setting up the date
                    dateTextView.setText(formattedDate);

                    */
                    linearLayout.animate().translationY(25f).setDuration(700);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                linearLayout.setVisibility(View.INVISIBLE);
            }
        }
    }
    public void findWeather(View view) throws ExecutionException, InterruptedException, JSONException {
        //AT THE START OF BTN CLICK RESET THE PREVIOUS TEXT
        textToDisplay="";
        linearLayout.setTranslationY(-25f);

        InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(cityName.getWindowToken(),0);

        if(cityName.getText().toString().equals("")){
            //Log.i("Damn True","inside if");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //1. simple dailog with 2 buttons
        builder.setMessage("Please enter a valid City")

                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
            builder.show();
        }else{

            linearLayout.setVisibility(View.VISIBLE);
            try {
                String encodedString = URLEncoder.encode(cityName.getText().toString(),"UTF-8");
                DownloadTask downloadTask = new DownloadTask();


                String result = downloadTask.execute("http://api.openweathermap.org/data/2.5/weather?q=" + encodedString+ "&appid=bfad1ae5c2f17db0c3fe5e85b57ce50a&units=metric").get();


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }catch (Exception ex){
                ex.printStackTrace();
            }


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityName =(EditText)findViewById(R.id.cityName);
        weatherText =(TextView)findViewById(R.id.weatherText);
        icon=(ImageView)findViewById(R.id.icon);
        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
        NameTextView=(TextView)findViewById(R.id.NameTextView);
        tempTextView =(TextView)findViewById(R.id.TempTextView);

    }

}
