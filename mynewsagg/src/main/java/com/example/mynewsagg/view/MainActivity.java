package com.example.mynewsagg.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.robotframework.API.DialogSystem;
import com.asus.robotframework.API.ExpressionConfig;
import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.WheelLights;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.mynewsagg.R;
import com.example.mynewsagg.adapter.DataAdapter;
import com.example.mynewsagg.model.ArticleStructure;
import com.example.mynewsagg.model.Constants;
import com.example.mynewsagg.model.NewsResponse;
import com.example.mynewsagg.mynewsaggApp;
import com.example.mynewsagg.network.ApiClient;
import com.example.mynewsagg.network.ApiInterface;
import com.example.mynewsagg.network.interceptors.OfflineResponseCacheInterceptor;
import com.example.mynewsagg.network.interceptors.ResponseCacheInterceptor;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.asus.robotframework.API.ExpressionConfig.READ_MODE_SENTENCE;

//import com.asus.robotframework.API.RobotAPI;
//import com.asus.robotframework.API.RobotCallback;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String[] SOURCE_ARRAY = {"google-news", "bbc-news",
            "buzzfeed", "mtv-news", "bbc-sport",  "talksport", "medical-news-today",
            "national-geographic"};
    private String SOURCE;

    //public RobotAPI robotAPI;
    //RobotCallback robotCallback;


    private ArrayList<ArticleStructure> articleStructure = new ArrayList<>();
    private ArrayList<String> articleTitles = new ArrayList<>();
    private DataAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Drawer result;
    private AccountHeader accountHeader;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Parcelable listState;

    private CallbackManager callbackManager;
    private RequestOptions options;
    private LoginManager loginManager;
    private FloatingActionButton fab;

    private TextView mTitle;
    private Boolean robotRead;

    public RobotAPI robotAPI;
    RobotCallback robotCallback;
    //private String[] wheelLightsID = {"SYNC_BOTH", "ASYNC_LEFT", "ASYNC_RIGHT"};
    //private String EditText_color;
    private boolean wheelOn;
    private ExpressionConfig config;
    //private AccessTokenTracker tokenTracker;
    private AccessToken accessToken;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createToolbar();
        createRecyclerView();
        SOURCE = SOURCE_ARRAY[0];
        mTitle.setText(R.string.toolbar_default_text);
        onLoadingSwipeRefreshLayout();
        createDrawer(savedInstanceState, toolbar);

        //this.robotAPI = new RobotAPI(getApplicationContext(), robotCallback);

        FacebookSdk.sdkInitialize(getApplicationContext());

        loadLocale();


        Log.d("Facebook logged in: ", String.valueOf(isLoggedIn()));
        accessToken = AccessToken.getCurrentAccessToken();
        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();

        options = new RequestOptions()
                .fitCenter()
                .override(125,125)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .priority(Priority.HIGH);
        createFab();
        if(isLoggedIn())
            loaduserProfile(accessToken);

        //robotAPI.robot.speak("Hello World. This is world News");

        //EditText_color.setText("0xFF0000");
        //EditText_color = "0xFF66FFAA";
        this.robotAPI = new RobotAPI(getApplicationContext(), robotCallback);

        //this.wheelOn = false;
        //if (getwheelOn()){
            //robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0xff0a1a);
            //robotAPI.wheelLights.startStatic(WheelLights.Lights.SYNC_BOTH);

        //}
        config = new ExpressionConfig();
        config.volume(80).languageId(DialogSystem.LANGUAGE_ID_EN_US).waitFactor(5).speed(100).readMode(READ_MODE_SENTENCE);
    }

    private void createToolbar() {
        toolbar = findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mTitle = findViewById(R.id.toolbar_title);

    }

    private void createRecyclerView() {
        recyclerView = findViewById(R.id.card_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }
    private void createFab(){
        fab = findViewById(R.id.fab);
        Glide.with(MainActivity.this)
                .asBitmap()
                .apply(options)
                .load(R.drawable.com_facebook_button_icon_blue)
                .into(new BitmapImageViewTarget(fab){
                    @Override
                    protected void setResource(Bitmap resource){
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(MainActivity.this.getResources(),resource);
                        circularBitmapDrawable.setCircular(true);
                        fab.setImageDrawable(circularBitmapDrawable);
                    }
                });
        //fab.setOnClickListener(new View.OnClickListener(){
         //   @Override
          //  public void onClick(View v){

           // }
        //});
    }

    private void createDrawer(Bundle savedInstanceState, final Toolbar toolbar) {
        PrimaryDrawerItem item0 = new PrimaryDrawerItem().withIdentifier(0).withName(R.string.general)
                .withSelectable(false);
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Google News")
                .withIcon(R.drawable.ic_googlenews);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("BBC News")
                .withIcon(R.drawable.ic_bbcnews);

        SectionDrawerItem item3 = new SectionDrawerItem().withIdentifier(3).withName(R.string.entertaiment);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName("Buzzfeed")
                .withIcon(R.drawable.ic_buzzfeednews);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName("MTV News")
                .withIcon(R.drawable.ic_mtvnews);
        SectionDrawerItem item6 = new SectionDrawerItem().withIdentifier(6).withName(R.string.sports);
        PrimaryDrawerItem item7 = new PrimaryDrawerItem().withIdentifier(7).withName("BBC Sports")
                .withIcon(R.drawable.ic_bbcsports);
        PrimaryDrawerItem item8 = new PrimaryDrawerItem().withIdentifier(8).withName("TalkSport")
                .withIcon(R.drawable.ic_talksport);
        SectionDrawerItem item9 = new SectionDrawerItem().withIdentifier(9).withName(R.string.science);
        PrimaryDrawerItem item10 = new PrimaryDrawerItem().withIdentifier(10).withName("Medical News Today")
                .withIcon(R.drawable.ic_medicalnewstoday);
        PrimaryDrawerItem item11 = new PrimaryDrawerItem().withIdentifier(11).withName("National Geographic")
                .withIcon(R.drawable.ic_nationalgeographic);
        SecondaryDrawerItem item12 = new SecondaryDrawerItem().withIdentifier(12).withName(R.string.newsapi_tag);


        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withSelectedItem(1)
                .addDrawerItems(item0, item1, item2, item3, item4, item5, item6,
                        item7, item8, item9, item10, item11,  item12)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        int selected = (int) (long) drawerItem.getIdentifier();
                        switch (selected) {
                            case 1:
                                SOURCE = SOURCE_ARRAY[0];
                                onLoadingSwipeRefreshLayout();
                                mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                robotAPI.robot.speak(SOURCE);
                                break;
                            case 2:
                                SOURCE = SOURCE_ARRAY[1];
                                onLoadingSwipeRefreshLayout();
                                mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                robotAPI.robot.speak(SOURCE);
                                break;
                            case 4:
                                SOURCE = SOURCE_ARRAY[2];
                                onLoadingSwipeRefreshLayout();
                                mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                robotAPI.robot.speak(SOURCE);
                                break;
                            case 5:
                                SOURCE = SOURCE_ARRAY[3];
                                onLoadingSwipeRefreshLayout();
                                mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                robotAPI.robot.speak(SOURCE);
                                break;
                            case 7:
                                SOURCE = SOURCE_ARRAY[4];
                                onLoadingSwipeRefreshLayout();
                                mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                robotAPI.robot.speak(SOURCE);
                                break;
                            case 8:
                                SOURCE = SOURCE_ARRAY[5];
                                onLoadingSwipeRefreshLayout();
                                mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                robotAPI.robot.speak(SOURCE);
                                break;
                            case 10:
                                SOURCE = SOURCE_ARRAY[6];
                                onLoadingSwipeRefreshLayout();
                                mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                robotAPI.robot.speak(SOURCE);
                                break;
                            case 11:
                                SOURCE = SOURCE_ARRAY[7];
                                onLoadingSwipeRefreshLayout();
                                mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                robotAPI.robot.speak(SOURCE);
                                break;
                            case 12:
                                Intent browserAPI = new Intent(Intent.ACTION_VIEW, Uri.parse("https://newsapi.org/"));
                                startActivity(browserAPI);
                                robotAPI.robot.speak("News A P I dot org");
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();
    }
    private void loadJSON() {
        swipeRefreshLayout.setRefreshing(true);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addNetworkInterceptor(new ResponseCacheInterceptor());
        httpClient.addNetworkInterceptor(new OfflineResponseCacheInterceptor());
        httpClient.cache(new Cache(new File(mynewsaggApp.getMynewsaggAppInstance().getCacheDir(), "ResponsesCache"), 10 * 1024 * 1024));
        httpClient.readTimeout(60, TimeUnit.SECONDS);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        ApiInterface request = ApiClient.getClient(httpClient).create(ApiInterface.class);

        Call<NewsResponse> call = request.getHeadlines(SOURCE, Constants.API_KEY);
        call.enqueue(new Callback<NewsResponse>() {

            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {

                if (response.isSuccessful() && response.body().getArticles() != null) {

                    if (!articleStructure.isEmpty()) {
                        articleStructure.clear();
                    }
                    articleStructure = response.body().getArticles();
                    for (int counter=0; counter<4;counter++) {
                        articleTitles.add(String.valueOf(articleStructure.get(counter).getTitle()));
                        Log.d("TitleMain: ", String.valueOf(articleStructure.get(counter).getTitle()));
                    }
                    speakTitles(articleTitles);
                    articleTitles.clear();

                    adapter = new DataAdapter(MainActivity.this, articleStructure);
                    recyclerView.setAdapter(adapter);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        loadJSON();
    }
    private void onLoadingSwipeRefreshLayout() {

        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadJSON();
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_newsapi:
                Intent browserAPI = new Intent(Intent.ACTION_VIEW, Uri.parse("https://newsapi.org/"));
                startActivity(browserAPI);
                break;
            case R.id.action_login:
                loginFB();
                break;
            case R.id.action_logout:
                logoutFB();
                break;
            case R.id.action_search:
                openSearchActivity();
                break;
            case R.id.action_voicesearch:
                openVoiceSearchActivity();
                break;
            case R.id.action_read:
                setRobotRead();
                break;
            case R.id.language:
                showChangeLanguageDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangeLanguageDialog() {
        /*List<CharSequence> listItems = Arrays.asList(
                Html.fromHtml("<font color='#FFFFFF'>English</font>"),
                Html.fromHtml("<font color='#FFFFFF'>繁體中文</font>"),
                Html.fromHtml("<font color='#FFFFFF'>Español</font>")
                );*/
        /*final ArrayAdapter arrayAdapter =
                new ArrayAdapter(this,android.R.layout.simple_expandable_list_item_2,listItems);*/
       final String[] listItems = {
                "English",
                "繁體中文",
                "Español"
        };
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this,R.style.MyAlertDialogStyle2);
        //mBuilder.
        mBuilder.setTitle(R.string.language_choose);
        mBuilder.setSingleChoiceItems(listItems,-1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    //English
                    setLocale("en",getRobotRead());
                    recreate();
                }else if(i==1){
                    //Traditional Chinese
                    setLocale("zh-rTW",getRobotRead());
                    recreate();
                } else if(i==2){
                    //Spanish
                    setLocale("es",getRobotRead());
                    recreate();
                }
                dialogInterface.dismiss();
            }
        });
        AlertDialog change = mBuilder.create();
        change.show();
    }
    private void setLocale(String lang, Boolean robotRead) {
        Locale locale = null;
        if (lang.equals("en")) {
            locale = Locale.ENGLISH;
        }else if(lang.equals("zh-rTW")){
            locale = Locale.TAIWAN;
        }else if(lang.equals("es")){
            locale = new Locale("es","ES");
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.putBoolean("robotRead",robotRead);
        //String tokengetter = accessToken.getToken();
        //editor.putString("token", tokengetter);
        editor.apply();
    }
    private void loadLocale(){
        prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang","en");
        Boolean robotRead = prefs.getBoolean("robotRead",getRobotRead());
        setLocale(language, robotRead);
    }

    private void openSearchActivity() {
        Intent searchIntent = new Intent(this, SearchActivity.class);
        startActivity(searchIntent);
    }
    private void openVoiceSearchActivity(){
        Intent voiceSearchIntent = new Intent(this, VoiceSearchActivity.class);
        startActivity(voiceSearchIntent);
    }
    private Boolean setRobotRead(){
        if(!getRobotRead()) {
            robotRead = true;
            Toast toast = Toast.makeText(getApplicationContext(), "Robot Read Titles: On", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            robotRead = false;
            Toast toast = Toast.makeText(getApplicationContext(), "Robot Read Titles: Off", Toast.LENGTH_SHORT);
            toast.show();
        }
        return robotRead;
    }
    private Boolean getRobotRead(){
        try {
            if (robotRead) {
                return robotRead;
            } else if (robotRead == null) {
                return false;
            } else
                return false;
        }catch (NullPointerException e){
        }
        return false;
    }
    private void speakTitles(ArrayList<String> speakTitles) {
        CountDownTimer mCountDownTimer = new CountDownTimer(speakTitles.get(0).length()*100, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
            }
        };
        if(getRobotRead()) {
            for (int counter = 0; counter < 3; counter++) {
                if (counter == 0){
                    mCountDownTimer.cancel();
                    robotAPI.robot.setExpression(RobotFace.DEFAULT,speakTitles.get(counter),config);
                    mCountDownTimer.start();
                }else {
                    String text;
                    text = speakTitles.get(counter);
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                    //toast.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                       @Override
                      public void run() {
                         toast.show();
                    }
                    }, text.length()*100);
                    robotAPI.robot.speak(text);
                }
            }
            //robotAPI.robot.setExpression(RobotFace.HIDEFACE);
        } else {
            //robotAPI.robot.setExpression(RobotFace.HIDEFACE);
            robotAPI.robot.stopSpeak();
        }
    }

    public void onBackPressed() {
        if (result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.mipmap.ic_launcher_round);
            builder.setMessage("Do you want to Exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff,0x393DB3);
                            robotAPI.wheelLights.startStatic(WheelLights.Lights.SYNC_BOTH);
                            prefs.edit().putBoolean("firstrun",true).apply();
                            finish();
                        }

                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle = result.saveInstanceState(bundle);
        bundle = accountHeader.saveInstanceState(bundle);

        super.onSaveInstanceState(bundle);
        listState = recyclerView.getLayoutManager().onSaveInstanceState();
        bundle.putParcelable(Constants.RECYCLER_STATE_KEY, listState);
        bundle.putString(Constants.SOURCE, SOURCE);
        bundle.putString(Constants.TITLE_STATE_KEY, mTitle.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            SOURCE = savedInstanceState.getString(Constants.SOURCE);
            createToolbar();
            mTitle.setText(savedInstanceState.getString(Constants.TITLE_STATE_KEY));
            listState = savedInstanceState.getParcelable(Constants.RECYCLER_STATE_KEY);
            createDrawer(savedInstanceState, toolbar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
        if(prefs.getBoolean("firstrun", true)){
            robotRead = true;
            prefs.edit().putBoolean("firstrun",false).apply();
        }
    }
    private void loginFB(){
        loginManager.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        List<String> permissions = new ArrayList<>();

        permissions.add("public_profile");
        permissions.add("email");
        //permissions.add("user_friends");

        loginManager.logInWithReadPermissions(this, permissions);
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        //handle the response in json object
                        /**
                        try{
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String email = object.getString("email");
                            String id = object.getString("id");
                            // image url
                            String image_url = "https://graph.facebook.com/"+id+ "/picture?type=normal";
                            // image on image view
                            try{
                                createFab();
                                Glide.with(MainActivity.this)
                                        .asBitmap()
                                        .apply(options)
                                        .load(image_url)
                                        .into(new BitmapImageViewTarget(fab) {
                                            @Override
                                            protected void setResource(Bitmap resource) {
                                                RoundedBitmapDrawable circularBitmapDrawable =
                                                        RoundedBitmapDrawableFactory.create(MainActivity.this.getResources(), resource);
                                                circularBitmapDrawable.setCircular(true);
                                                fab.setImageDrawable(circularBitmapDrawable);
                                            }
                                        });
                            }
                            catch (Exception e){
                                Log.e("Error: ",e.getMessage());
                                e.printStackTrace();;
                            }
                            //fab.setImageDrawable(new BitmapDrawable(getResources(), bmp));
                        }catch (JSONException e){
                            e.printStackTrace();
                        }**/
                    }
                });
                //facebookLogin = true;
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        AccessTokenTracker tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                //int color = Integer.decode("0x00ff00");
                if(currentAccessToken==null){
                    robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
                    Toast.makeText(MainActivity.this, "User Logged out", Toast.LENGTH_LONG).show();
                    wheelOn = false;
                    wheelLight(wheelOn);
                }else {
                    wheelOn = true;
                    wheelLight(wheelOn);
                    loaduserProfile(currentAccessToken);
                }
            }
        };
    }
    private void loaduserProfile(AccessToken newAccessToken){
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                //handle the response in json object
                try{
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    //String gender = object.getString("gender");
                    // image url
                    String image_url = "https://graph.facebook.com/"+id+ "/picture?type=normal";
                    // image on image view
                    try{
                        Glide.with(MainActivity.this)
                                .asBitmap()
                                .apply(options)
                                .load(image_url)
                                .into(new BitmapImageViewTarget(fab) {
                                    @Override
                                    protected void setResource(Bitmap resource) {
                                        RoundedBitmapDrawable circularBitmapDrawable =
                                                RoundedBitmapDrawableFactory.create(MainActivity.this.getResources(), resource);
                                        circularBitmapDrawable.setCircular(true);
                                        fab.setImageDrawable(circularBitmapDrawable);
                                    }
                                });
                        String welcome = "Welcome " + first_name+" "+last_name;
                        robotAPI.robot.speak(welcome);
                        Toast.makeText(MainActivity.this, welcome, Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e){
                        Log.e("Error: ",e.getMessage());
                        e.printStackTrace();
                    }
                    //fab.setImageDrawable(new BitmapDrawable(getResources(), bmp));
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        });
        // specify the parameter in the form of bundle object
        Bundle parameters = new Bundle();
        //comma operator to separate the data values
        parameters.putString("fields","first_name, last_name, email,id");
        request.setParameters(parameters); //set parameters on request
        request.executeAsync();
    }
    private void logoutFB(){
        loginManager.logOut();
        Glide.with(MainActivity.this)
                .asBitmap()
                .apply(options)
                .load(R.drawable.com_facebook_button_icon_blue)
                .into(new BitmapImageViewTarget(fab){
                    @Override
                    protected void setResource(Bitmap resource){
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(MainActivity.this.getResources(),resource);
                        circularBitmapDrawable.setCircular(true);
                        fab.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }
    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }
    private void wheelLight(boolean wheelLight){
        if (wheelLight){
            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff,0xff0a1a);
            robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0xff0a1a);
            robotAPI.wheelLights.startStatic(WheelLights.Lights.SYNC_BOTH);
        } else{
            robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}