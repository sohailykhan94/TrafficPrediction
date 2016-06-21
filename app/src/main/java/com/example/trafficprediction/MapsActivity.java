package com.example.trafficprediction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.location.places.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionCallback, GoogleApiClient.OnConnectionFailedListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private View btnRequestDirection;
    private GoogleMap googleMap;
    private String serverKey = "AIzaSyA10nTJ7pD4pgPyBgTTvKr8h9YC-ICNJTY";
    public LatLng camera = new LatLng(22.339544, 114.169781);
    public LatLng origin;
    public LatLng destination;
    public LinearLayout toolbar;
    ArrayList<HashMap<String, Double>> formList = new ArrayList<HashMap<String, Double>>();
    private static final String TAG = "Maps";
    private ListView lv;
    private Context context;
    private Handler handler;
    private Runnable task;
    private SlidingUpPanelLayout mLayout;

    private int count = 0;
    /**
     * GoogleApiClient wraps our service connection to Google Play Services and provides access
     * to the user's sign in state as well as the Google's APIs.
     */
    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;
    //Load animation
    Animation slide_down;

    Animation slide_up;
    private AutoCompleteTextView mAutocompleteView;
    private AutoCompleteTextView mAutocompleteViewTo;
    private TextView date;
    private TextView time;
    private TextView timeMain;
    private TextView timeHolder;
    private int pickerHour = -1;
    private int pickerMin = -1;
    private int pickerDay = -1;
    private int pickerMonth = -1;
    private Marker srcSelected;
    private ImageButton upButton;
    private Marker desSelected;
    private LinearLayout dragView;
    private int pickerYear = -1;
    private EditText etSearch;
    private ImageButton down;
    private ArrayList<Location> locationList;
    CustomAdapter arrayAdapter;

    private static final LatLngBounds BOUNDS_GREATER_HK = new LatLngBounds(
            new LatLng(22.171750, 114.265547), new LatLng(22.482559, 114.001907));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        btnRequestDirection = (FrameLayout) findViewById(R.id.topLayout);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        timeMain = (TextView) findViewById(R.id.timeMain);
        timeHolder = (TextView) findViewById(R.id.timeHolder);
        toolbar= (LinearLayout) findViewById(R.id.Toolbar);
        upButton = (ImageButton) findViewById(R.id.upButton);
        down = (ImageButton) findViewById(R.id.downButton);
        dragView = (LinearLayout) findViewById(R.id.dragView);
        down.setVisibility(View.GONE);
        down.setVisibility(View.INVISIBLE);
        timeMain.setVisibility(View.GONE);

        handler = new Handler();
        timeHolder.setText("Enter start and end location");
        context = this;
        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_places);

        mAutocompleteViewTo = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_places_to);

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAutocompleteViewTo.setOnItemClickListener(mAutocompleteClickListener2);

        slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
        slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_HK,
                null);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        mAutocompleteView.setAdapter(mAdapter);
        mAutocompleteViewTo.setAdapter(mAdapter);
        lv = (ListView) findViewById(R.id.list);

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        locationList = new ArrayList<>();
        arrayAdapter = new CustomAdapter(this, this.context, locationList);

        lv.setAdapter(arrayAdapter);
        lv.setItemsCanFocus(true);
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        etSearch = (EditText) findViewById(R.id.etSearch);

        // Add Text Change Listener to EditText
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                arrayAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public void makeInfo(int pos, LatLng src, LatLng des, String srcString, String desString) {
        Log.i("makeInfo", "=" + pos);
        try{
            srcSelected.remove();
            desSelected.remove();
        }catch(Exception e){

        }
        srcSelected = googleMap.addMarker(new MarkerOptions().position(src).title(srcString));
        desSelected = googleMap.addMarker(new MarkerOptions().position(des).title(desString));
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(src, 14));
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        pickerHour = hourOfDay;
        pickerMin = minute;
        String zone;
        if(hourOfDay>=12){
            zone = "PM";
        }else{
            zone = "AM";
        }
        String minString = String.valueOf(pickerMin);
        if(pickerMin==0){
            minString = "0"+String.valueOf(pickerMin);
            pickerMin = 0;
        }else if(pickerMin==0){
            minString = "15";
            pickerMin = 15;
        }else if(pickerMin==2){
            minString = "30";
            pickerMin = 30;
        }else{
            minString = "45";
            pickerMin = 45;
        }

        String hourString = String.valueOf(pickerHour);
        if(pickerHour<10){
            hourString = "0"+String.valueOf(pickerHour);
        }
        time.setText(hourString + ":" + minString + zone);

    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the time chosen by the user
        pickerDay = day;
        pickerMonth = month+1;
        pickerYear = year;
        date.setText(pickerDay + "/" + pickerMonth + "/" + pickerYear);

    }

    public String getJSON(int timeout) {
        HttpURLConnection c = null;
        try {
            String url = "http://104.199.188.25:4000/get_prediction?year=" + pickerYear + "&month=" + pickerMonth + "&day=" + pickerDay + "&hour=" + pickerHour + "&minute=" + pickerMin;
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }


    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    public void readJson() {
        try {
            JSONObject obj = new JSONObject(getJSON(100000));
            JSONArray m_jArry = obj.getJSONArray("nodes");
            HashMap<String, Double> m_li;

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                Log.d("Details-->", jo_inside.getString("link_id"));
                String link_id = jo_inside.getString("link_id");
                Double src_lat = jo_inside.getDouble("src_lat");
                Double src_long = jo_inside.getDouble("src_long");
                Double des_lat = jo_inside.getDouble("des_lat");
                Double des_long = jo_inside.getDouble("des_long");
                double saturation;
                String roadSaturation = jo_inside.getString("road_saturation");
                double type;
                String road_type = jo_inside.getString("route_type");
                if(road_type.equals("URBAN ROAD")){
                    type = 0;
                }else{

                    type = 1;
                }
                if(roadSaturation.equals("TRAFFIC GOOD")){
                    saturation = 0;
                }else if (roadSaturation.equals("TRAFFIC AVERAGE")) {
                    saturation = 1;
                }else{

                    saturation = 3;
                }

                //Add your values in your `ArrayList` as below:
                m_li = new HashMap<String, Double>();
                //m_li.put("link_id", link_id);
                m_li.put("src_lat", src_lat);
                m_li.put("src_long", src_long);
                m_li.put("des_lat", des_lat);
                m_li.put("des_long", des_long);
                m_li.put("saturation", saturation);
                m_li.put("type", type);

                formList.add(m_li);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showToolbar(View v){
        toolbar.setVisibility(View.VISIBLE);
        toolbar.startAnimation(slide_down);
        down.setVisibility(View.GONE);
        upButton.setVisibility(View.VISIBLE);
    }

    public void hideToolbar(View v){
        toolbar.setVisibility(View.INVISIBLE);
        toolbar.startAnimation(slide_up);
        down.setVisibility(View.VISIBLE);
        upButton.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 13));
    }

    public void requestClick(View v) {

        if(date.getText().toString().matches("") || time.getText().toString().matches("") || mAutocompleteView.getText().toString().matches("") || mAutocompleteViewTo.getText().toString().matches("")){
            Snackbar.make(btnRequestDirection, "Inputs can not be null. Please try again...", Snackbar.LENGTH_SHORT).show();
            return;
        }
        down.setVisibility(View.VISIBLE);
        down.setVisibility(View.VISIBLE);
        toolbar.startAnimation(slide_up);
        toolbar.setVisibility(View.INVISIBLE);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        readJson();
        requestDirection();
    }

    public void requestDirection() {
        Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        try {
            handler.removeCallbacks(task);
            locationList.clear();
            arrayAdapter.notifyDataSetChanged();
            googleMap.clear();
            GoogleDirection.withServerKey(serverKey)
                    .from(origin)
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction) {
                            //Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
                            if (direction.isOK()) {
//                                googleMap.addMarker(new MarkerOptions().position(origin));
//                                googleMap.addMarker(new MarkerOptions().position(destination));
                                Route route = direction.getRouteList().get(0);
                                Leg leg = route.getLegList().get(0);
                                googleMap.addMarker(new MarkerOptions().position(origin).title(leg.getStartAddress()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                                googleMap.addMarker(new MarkerOptions().position(destination).title(leg.getEndAddress()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                timeMain.setVisibility(View.VISIBLE);
                                timeHolder.setTextColor(context.getResources().getColor(R.color.black));
                                timeHolder.setText(leg.getDuration().getText());
                                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)timeHolder.getLayoutParams();
                                params.setMargins(0, 4, 0, 0); //substitute parameters for left, top, right, bottom
                                timeHolder.setLayoutParams(params);
                                ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                googleMap.addPolyline(DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, getApplicationContext().getResources().getColor(R.color.turq)));

                                //btnRequestDirection.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
            task = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Doing task");
                    final HashMap<String, Double> map_final = formList.get(count);
                    final int color;
                    if (map_final.get("saturation") == 0) {
                        color = getApplicationContext().getResources().getColor(R.color.green);
                    } else if (map_final.get("saturation") == 1) {
                        color = getApplicationContext().getResources().getColor(R.color.yellow);
                    } else {
                        color = getApplicationContext().getResources().getColor(R.color.red);
                    }
                    GoogleDirection.withServerKey(serverKey)
                            .from(new LatLng(map_final.get("src_lat"), map_final.get("src_long")))
                            .to(new LatLng(map_final.get("des_lat"), map_final.get("des_long")))
                            .transportMode(TransportMode.DRIVING)
                            .unit(Unit.METRIC)
                            .execute(new DirectionCallback() {
                                @Override
                                public void onDirectionSuccess(Direction direction) {
                                    //Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
                                    //String status = direction.getStatus();

                                    if (direction.isOK()) {
                                        try {
                                            Route route = direction.getRouteList().get(0);

                                            Leg leg = route.getLegList().get(0);
                                            String start = leg.getStartAddress();
                                            String end = leg.getEndAddress();
                                            Info distance = leg.getDistance();
                                            Log.d("Address -->", start + " " + end);
                                            DecimalFormat df = new DecimalFormat("#");
                                            df.setRoundingMode(RoundingMode.CEILING);
                                            Double time = 0.0;
                                            Double speed = 0.0;
                                            Double distance_new = Double.parseDouble(distance.getValue())/1000;

//                                            googleMap.addMarker(new MarkerOptions().position(new LatLng(map_final.get("src_lat"), map_final.get("src_long"))).title(leg.getStartAddress()));
//
//                                            googleMap.addMarker(new MarkerOptions().position(new LatLng(map_final.get("des_lat"), map_final.get("des_long"))).title(leg.getEndAddress()));
                                            if(map_final.get("type") == 0){
                                                if (map_final.get("saturation") == 0) {
                                                    speed = 31.0;
                                                }else if (map_final.get("saturation") == 1) {
                                                    speed = 22.5;
                                                }else {
                                                    speed = 14.0;
                                                }
                                                time = distance_new/speed;
                                            }else{
                                                if (map_final.get("saturation") == 0) {
                                                    speed = 51.0;
                                                }else if (map_final.get("saturation") == 1) {
                                                    speed = 40.0;
                                                }else {
                                                    speed = 24.0;
                                                }
                                                time = distance_new/speed;
                                            }
                                            time = time*60;
                                            String time_string;
                                            if(df.format(time).equals("1")){
                                                time_string = df.format(time) + " min";
                                            }else{
                                                time_string = df.format(time) + " mins";
                                            }
                                            String finalTimeString = time_string+" | "+ leg.getDuration().getText();
                                            if (map_final.get("saturation") == 0) {
                                                locationList.add(new Location(start, end, finalTimeString, 0, new LatLng(leg.getStartLocation().getLatitude(), leg.getStartLocation().getLongitude()), new LatLng(leg.getEndLocation().getLatitude(), leg.getEndLocation().getLongitude())));
                                            } else if (map_final.get("saturation") == 1) {
                                                locationList.add(new Location(start, end, finalTimeString, 1, new LatLng(leg.getStartLocation().getLatitude(), leg.getStartLocation().getLongitude()), new LatLng(leg.getEndLocation().getLatitude(), leg.getEndLocation().getLongitude())));
                                            } else {
                                                locationList.add(new Location(start, end, finalTimeString, 2, new LatLng(leg.getStartLocation().getLatitude(), leg.getStartLocation().getLongitude()), new LatLng(leg.getEndLocation().getLatitude(), leg.getEndLocation().getLongitude())));
                                            }
                                            arrayAdapter.notifyDataSetChanged();
                                            //Log.d("Count-->", "1");
                                        } catch (Exception ex) {
                                            Snackbar.make(btnRequestDirection, ex.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }

                                        ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                        googleMap.addPolyline(DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 3, color));

//                                    btnRequestDirection.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onDirectionFailure(Throwable t) {
                                    Snackbar.make(btnRequestDirection, "not found", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                    if(count + 1 == formList.size()){
                        handler.removeCallbacks(this);
                        count =0;
                    }
                    else{
                        count++;
                        handler.postDelayed(this, 100);
                    }
                }
            };
            handler.post(task);




        }catch (Exception e){
            Snackbar.make(btnRequestDirection, e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }


    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            //Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            //Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                //Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            Place placeOrg = places.get(0);
            Toast.makeText(getApplicationContext(), placeOrg.getLatLng().toString() ,
                    Toast.LENGTH_SHORT).show();
            origin = placeOrg.getLatLng();
            //Log.i(TAG, "Place details received: " + place.getName());

            places.release();
        }
    };

    private AdapterView.OnItemClickListener mAutocompleteClickListener2
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            //Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback2);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            //Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback2
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                //Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            Place place = places.get(0);
            Toast.makeText(getApplicationContext(), place.getLatLng().toString() ,
                    Toast.LENGTH_SHORT).show();
            destination = place.getLatLng();

            //Log.i(TAG, "Place details received: " + place.getName());

            places.release();
        }
    };

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
//        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
//                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

//        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
//                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDirectionSuccess(Direction direction) {

    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }
}