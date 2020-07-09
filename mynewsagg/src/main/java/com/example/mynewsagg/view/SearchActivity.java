package com.example.mynewsagg.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

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

public class SearchActivity extends AppCompatActivity {

    private EditText mEdtSearch;
    private TextView mTxvNoResultsFound;
    private SwipeRefreshLayout mSwipeRefreshSearch;
    private RecyclerView mRecyclerViewSearch;
    private DataAdapter adapter;
    private ArrayList<ArticleStructure> articleStructure = new ArrayList<>();

    private RobotAPI robotAPI;
    RobotCallback robotCallback;
    //private MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        this.robotAPI = new RobotAPI(getApplicationContext(), robotCallback);


        createToolbar();
        initViews();

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
        //if (wheelOn == true){
        //robotAPI.wheelLights.startStarryNight(WheelLights.Lights.SYNC_BOTH, WheelLights.Speed.DEFAULT);
    }

   private void createToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initViews() {
        mEdtSearch = findViewById(R.id.editText_search);
        mSwipeRefreshSearch = findViewById(R.id.swipe_refresh_layout_search);
        mRecyclerViewSearch = findViewById(R.id.search_recycler_view);
        mTxvNoResultsFound = findViewById(R.id.tv_no_results);
        mRecyclerViewSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
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
                        adapter = new DataAdapter(SearchActivity.this, articleStructure);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                mEdtSearch.setText("");
                mEdtSearch.requestFocus();
                InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                mgr.showSoftInput(mEdtSearch, InputMethodManager.SHOW_IMPLICIT);
                mRecyclerViewSearch.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cancelSearch() {
        onBackPressed();
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
}