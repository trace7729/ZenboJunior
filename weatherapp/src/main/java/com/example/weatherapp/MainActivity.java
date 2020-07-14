package com.example.weatherapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.SpeakConfig;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends RobotActivity {
   //public RobotAPI robotAPI;
   //public RobotCallback robotCallback;
   //public RobotCallback.Listen robotListenCallback;
   // List of Weather objects representing the forecast
   private List<Weather> weatherList = new ArrayList<>();

   // ArrayAdapter for binding Weather objects to a ListView
   private WeatherArrayAdapter weatherArrayAdapter;
   private ListView weatherListView; // displays weather info
   private static EditText locationEditText;


   private static String apikey = "c9ffcfad3f120a8d264eac37ea08f14d";
   private static String baseurl = "http://api.openweathermap.org/data/2.5/forecast?q=";
   //private String text;
   //private static String urlString;
   //private RobotCallback robotCallback;
   //private RobotCallback.Listen robotListenCallback;

   // configure Toolbar, ListView and FAB
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // autogenerated code to inflate layout and configure Toolbar
      setContentView(R.layout.activity_main);

      // create ArrayAdapter to bind weatherList to the weatherListView
      weatherListView = (ListView) findViewById(R.id.weatherListView);
      weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
      weatherListView.setAdapter(weatherArrayAdapter);

      //robotSpeak();
      // configure FAB to hide keyboard and initiate web service request
      locationEditText = findViewById(R.id.locationEditText);
      FloatingActionButton fab =
              (FloatingActionButton) findViewById(R.id.fab);
      fab.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            // get text from locationEditText and create web service URL
            //locationEditText =
            //(EditText) findViewById(R.id.locationEditText);
            URL url = createURL(locationEditText.getText().toString());
            Log.d("whaturllookk", url.toString());

            // hide keyboard and initiate a GetWeatherTask to download
            // weather data from OpenWeatherMap.org in a separate thread
            if (url != null) {
               dismissKeyboard(locationEditText);
               GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
               getLocalWeatherTask.execute(url);
            }
            else {
               Snackbar.make(findViewById(R.id.coordinatorLayout),
                       R.string.invalid_url, Snackbar.LENGTH_LONG).show();
            }
         }
      });
      FloatingActionButton fabvoice =
              (FloatingActionButton) findViewById(R.id.voice);
      fabvoice.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v){
            robotAPI.robot.speakAndListen("Speech to Text", new SpeakConfig().timeout(20));
            //Toast.makeText(new MainActivity(), Integer.toString(text1), Toast.LENGTH_LONG).show();
         }
      });
   }
   @Override
   protected void onPause() {
      super.onPause();

      //stop listen user utterance
      robotAPI.robot.stopSpeakAndListen();
   }
   // programmatically dismiss keyboard when user touches FAB
   private void dismissKeyboard(View view) {
      InputMethodManager imm = (InputMethodManager) getSystemService(
              Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
   }

   // create openweathermap.org web service URL using city
   private URL createURL(String city) {
      String apiKey = getString(R.string.api_key);
      String baseUrl = getString(R.string.web_service_url);
      try {
         // create URL for specified city and imperial units (Fahrenheit)
         //String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +
         //        "&units=imperial&cnt=16&APPID=" + apiKey;
         String urlString = baseUrl + city +
                 "&units=metric&cnt=16&APPID=" + apiKey;
         //Toast.makeText(getApplicationContext(), urlString, Toast.LENGTH_LONG).show();
         Log.d("myTag", urlString);
         return new URL(urlString);
      }
      catch (Exception e) {
         e.printStackTrace();
      }

      return null; // URL was malformed
   }

   public void getWeather(URL url){
      GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
      getLocalWeatherTask.execute(url);
      Log.d("getWeather",url.toString());
   }

   // makes the REST web service call to get weather data and
   // saves the data to a local HTML file
   private class GetWeatherTask
           extends AsyncTask<URL, Void, JSONObject> {

      @Override
      protected JSONObject doInBackground(URL... params) {
         HttpURLConnection connection = null;

         try {
            connection = (HttpURLConnection) params[0].openConnection();
            int response = connection.getResponseCode();
            Log.d("ConntectionP","cpnnection"+Integer.toString(response));

            if (response == HttpURLConnection.HTTP_OK) {
               StringBuilder builder = new StringBuilder();

               try (BufferedReader reader = new BufferedReader(
                       new InputStreamReader(connection.getInputStream()))) {

                  String line;

                  while ((line = reader.readLine()) != null) {
                     builder.append(line);
                  }
               }
               catch (IOException e) {
                  Snackbar.make(findViewById(R.id.coordinatorLayout),
                          R.string.read_error, Snackbar.LENGTH_LONG).show();
                  e.printStackTrace();
               }
               Log.d("ConntectionRETURN","cRnnection"+builder.toString());

               return new JSONObject(builder.toString());
            }
            else {
               Snackbar.make(findViewById(R.id.coordinatorLayout),
                       R.string.connect_error, Snackbar.LENGTH_LONG).show();
            }
         }
         catch (Exception e) {
            Snackbar.make(findViewById(R.id.coordinatorLayout),
                    R.string.connect_error, Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
         }
         finally {
            connection.disconnect(); // close the HttpURLConnection
         }

         return null;
      }

      // process JSON response and update ListView
      @Override
      protected void onPostExecute(JSONObject weather) {
         convertJSONtoArrayList(weather); // repopulate weatherList
         weatherArrayAdapter.notifyDataSetChanged(); // rebind to ListView
         weatherListView.smoothScrollToPosition(0); // scroll to top
      }
   }

   // create Weather objects from JSONObject containing the forecast
   private void convertJSONtoArrayList(JSONObject forecast) {
      weatherList.clear(); // clear old weather data

      try {
         // get forecast's "list" JSONArray
         JSONArray list = forecast.getJSONArray("list");
         //Toast.makeText(getApplicationContext(), list.length(), Toast.LENGTH_LONG).show();
         Long dayWeekNew;
         Long dayWeekOld=null;
         // convert each element of list to a Weather object
         for (int i = 0; i < list.length(); ++i) {
            JSONObject day = list.getJSONObject(i); // get one day's data
            dayWeekNew = day.getLong("dt");
            Log.d("dayWeekNew", String.valueOf(dayWeekNew));
            // get the day's temperatures ("temp") JSONObject
            JSONObject temperatures = day.getJSONObject("main");

            // get day's "weather" JSONObject for the description and icon
            JSONObject weather =
                    day.getJSONArray("weather").getJSONObject(0);

            // add new Weather object to weatherList
            Log.d("dayWeekOld", String.valueOf(dayWeekOld));
            if (!(Objects.equals(dayWeekNew, dayWeekOld))){
               weatherList.add(new Weather(
                       dayWeekNew, // date/time timestamp
                       temperatures.getDouble("temp_min"), // minimum temperature
                       temperatures.getDouble("temp_max"), // maximum temperature
                       temperatures.getDouble("humidity"), // percent humidity
                       weather.getString("description"), // weather conditions
                       weather.getString("icon"))); // icon name
            }
            dayWeekOld= dayWeekNew;
         }
      }
      catch (JSONException e) {
         e.printStackTrace();
      }
   }



   public static RobotCallback robotCallback = new RobotCallback() {
      @Override
      public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
         super.onResult(cmd, serial, err_code, result);
      }

      @Override
      public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
         super.onStateChange(cmd, serial, err_code, state);
      }

      @Override
      public void initComplete() {
         super.initComplete();

      }
   };
   public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
      String text;
      @Override
      public void onFinishRegister() {

      }

      @Override
      public void onVoiceDetect(JSONObject jsonObject) {

      }

      @Override
      public void onSpeakComplete(String s, String s1) {

      }

      @Override
      public void onEventUserUtterance(JSONObject jsonObject) {
         text = "onEventUserUtterance: " + jsonObject.toString();
         Log.d("Utterance11:", text);
         //Toast.makeText(new ZenboDialogSample(), text, Toast.LENGTH_LONG).show();
      }


      @Override
      public void onResult(JSONObject jsonObject) {
         JSONObject object;
         text = jsonObject.toString();
         Log.d("Name1990", text);
         try {
            object = jsonObject.getJSONObject("event_slu_query");
            text = object.getString("correctedSentence");
            text = text.replace(".","");
            Log.d("Name1991", text);
            locationEditText.setText(text);
            String urlString = baseurl + text +
                    "&units=metric&cnt=16&APPID=" + apikey;
            Log.d("urlString", urlString);
            URL urlConvert = new URL(urlString);
            //GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
            //getLocalWeatherTask.execute(url);
            //locationEditText.setText(text);
            //MainActivity main = new MainActivity();
            //main.getWeather(urlConvert);
         } catch (JSONException e) {
            e.printStackTrace();
         } catch (MalformedURLException e) {
            e.printStackTrace();
         }

      }

      @Override
      public void onRetry(JSONObject jsonObject) {

      }
   };

   public MainActivity () {
      super(robotCallback, robotListenCallback);
   }
}