package com.example.mynewsagg.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.SpeakConfig;
import com.asus.robotframework.API.WheelLights;
import com.example.mynewsagg.R;
import com.example.mynewsagg.adapter.DataAdapter;
import com.example.mynewsagg.model.ArticleStructure;
import com.example.mynewsagg.model.Constants;
import com.example.mynewsagg.model.NewsResponse;
import com.example.mynewsagg.network.ApiClient;
import com.example.mynewsagg.network.ApiInterface;
//import com.example.hole1.mynewsagg.network.interceptors.OfflineResponseCacheInterceptor;
import com.example.mynewsagg.network.interceptors.ResponseCacheInterceptor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class VoiceSearchActivity extends RobotActivity {

    private static EditText mEdtSearch;
    private TextView mTxvNoResultsFound;
    private SwipeRefreshLayout mSwipeRefreshSearch;
    private RecyclerView mRecyclerViewSearch;
    private DataAdapter adapter;
    private ArrayList<ArticleStructure> articleStructure = new ArrayList<>();

    private RobotAPI robotAPI;

    //private MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_search);

        this.robotAPI = new RobotAPI(getApplicationContext(), robotCallback);


        createToolbar();
        initViews();
        robotAPI.robot.speakAndListen("Speech to Text", new SpeakConfig().timeout(20));
        FloatingActionButton fab=
                (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                searchEverything(mEdtSearch.getText().toString().trim());
            }
        });

        mEdtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    searchEverything(mEdtSearch.getText().toString().trim());
                    return true;
                }

                return false;
            }
        });

        mSwipeRefreshSearch.setEnabled(false);
        mSwipeRefreshSearch.setColorSchemeResources(R.color.colorPrimary);
        //MainActivity mainActivity = new MainActivity();
        //boolean wheelOn = mainActivity.getwheelOn();
        //if (MainActivity.wheelOn == true) {
         //   robotAPI.wheelLights.startStarryNight(WheelLights.Lights.SYNC_BOTH, WheelLights.Speed.DEFAULT);
        //}
    }

    private void createToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();

        //stop listen user utterance
        robotAPI.robot.stopSpeakAndListen();
    }

    private void initViews() {
        mEdtSearch = findViewById(R.id.editText_search);
        mSwipeRefreshSearch = findViewById(R.id.swipe_refresh_layout_search);
        mRecyclerViewSearch = findViewById(R.id.search_recycler_view);
        mTxvNoResultsFound = findViewById(R.id.tv_no_results);
        mRecyclerViewSearch.setLayoutManager(new LinearLayoutManager(VoiceSearchActivity.this));
    }

    private void searchEverything(final String search) {
        mSwipeRefreshSearch.setEnabled(true);
        mSwipeRefreshSearch.setRefreshing(true);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addNetworkInterceptor(new ResponseCacheInterceptor());
        httpClient.readTimeout(60, TimeUnit.SECONDS);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        ApiInterface request = ApiClient.getClient(httpClient).create(ApiInterface.class);

        String sortBy = "publishedAt";
        String language = "en";
        Call<NewsResponse> call = request.getSearchResults(search, sortBy, language, Constants.API_KEY);
        //openSpeakActivity();
        call.enqueue(new Callback<NewsResponse>() {

            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {

                if (response.isSuccessful() && response.body().getArticles() != null) {
                    robotAPI.robot.speak("Search "+search);
                    if (response.body().getTotalResults() != 0) {
                        if (!articleStructure.isEmpty()) {
                            articleStructure.clear();
                        }

                        articleStructure = response.body().getArticles();
                        adapter = new DataAdapter(VoiceSearchActivity.this, articleStructure);
                        mRecyclerViewSearch.setVisibility(View.VISIBLE);
                        mTxvNoResultsFound.setVisibility(View.GONE);
                        mRecyclerViewSearch.setAdapter(adapter);
                        mSwipeRefreshSearch.setRefreshing(false);
                        mSwipeRefreshSearch.setEnabled(false);
                    } else if (response.body().getTotalResults() == 0){
                        mSwipeRefreshSearch.setRefreshing(false);
                        mSwipeRefreshSearch.setEnabled(false);
                        mTxvNoResultsFound.setVisibility(View.VISIBLE);
                        mRecyclerViewSearch.setVisibility(View.GONE);
                        mTxvNoResultsFound.setText("No Results found for \"" + search + "\"." );
                        robotAPI.robot.speak("No Results found for \"" + search + "\"." );
                    }
                }
            }


            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                mSwipeRefreshSearch.setRefreshing(false);
                mSwipeRefreshSearch.setEnabled(false);
            }
        });
    }



    @Override
    public void onBackPressed() {
        //robotAPI.wheelLights.startStatic(WheelLights.Lights.SYNC_BOTH);
        //robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
        super.onBackPressed();
    }

    //private void openSpeakActivity() {
    //   speakActivity.robotSpeak();
    //}

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
                mEdtSearch.setText(text);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public VoiceSearchActivity () {
        super(robotCallback, robotListenCallback);
    }
}