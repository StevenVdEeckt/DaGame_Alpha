package andreas.gps;

// insert here the main game activity
//holoholo

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Math;

import andreas.gps.sensoren.SensorCollector;
import andreas.gps.sensoren.Sensor_SAVE;
import andreas.gps.sensoren.SoundAct;

public class gameMode extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, Servercomm.ServercommEventListener {

    //    variables

    private Circle circleLoc;
    private Circle circleTarget;
    private MyLocations locations = new MyLocations();
    private int r = 5;
    private static final String TAG = "abcd";
    private LatLng CURRENT_TARGET;
    private LatLng TARGET_MAIN = new LatLng(50.864164, 4.678891);
    private LatLng TARGET_SEC = new LatLng(50.864021, 4.678460);
    private Marker markerTarget;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    public Marker mymarker;
    boolean gps_connected = false;
    boolean network_connected = false;
    boolean connections_working = false;
    public float zoomlevel = 18;
    public boolean zoomed = false;
    public LatLng loc;
    static final String STATE_SCORE = "playerScore";
    Calendar c = Calendar.getInstance();
    private double mySpeed = 0;
    private int kill_button_counter = 0;
    //text kkillmoves
    private String killedText = "kill confirmed";
    private String killedPointsAddedText = "point added!";
    private String killedNotText = "You missed try again!";
    private String killmoveAcellorText="accelerate!";
    private String killmoveGyroText="Shoot him down!";
    private String killmoveSoundText="Scream him to dead!";
    private String killmoveSpeedText="get to your highest speed!";
    private String killmovelightText="Remove al light!";
    private String killmovePressButtonText="Press him to dead!";
    private double killmoveAcellorValue = 0.5;
    private double killmoveGyroValue = 40;
    private double killmoveSoundValue = 25000;
    private double killmoveSpeedValue = 6.4;
    private double killmovelightValue = 2;
    private double killmovePressButtonValue = 5;
    public String playerscategory = "playerscategory";
    public String targetchange = "targetchange";
    public String hunting = "hunting";
    public String myusername;
    Servercomm mServercomm = new Servercomm();
    public String NotifyOnline = "I'm online";
    public String NotifyOffline = "I'm offline";
    public String askNumberOfPlayers = "Who is online?";
    public int numberOfOnlinePlayers = 0;
    public String TargetID = "";
    public String getPriorities = "Send priorities";
    public String eliminated = "You're eliminated";
    public String pickedTarget = "You're new target";
    public int huntedby = 0;
    public String droppedTarget = "Dropped target";
    public String locationUpdate = "Update location";
    public String priorityID = "";
    private Handler mHandler = new Handler();
    public double prioritylevel = 0;
    public LatLng targetLocation;
    public String priorityCategory = "Priority";
    public String getNewLocation = "Request new location";
    public String giveNewLocation = "Giving new location";










    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Got into oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //login
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_low_in_rank);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)        // 5 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        final Button zoombutton = (Button)findViewById(R.id.zoombutton);

        final Button targetbutton = (Button)findViewById(R.id.targetbutton);


        zoombutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "clicked!");
                Log.i(TAG, String.valueOf(loc));
                if (loc != null) {
                    Log.i(TAG, "moving camera");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlevel));
                }
            }
        });



        if (savedInstanceState != null) {
            // Restore value of members from saved state
            int mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            String points_str = Integer.toString(mCurrentScore);
            points_score.setText(points_str);}
        mHandler = new Handler();
        startRequestingLocationUpdates();


            Log.i(TAG, "Oncreate success");


    }

    private void startRequestingLocationUpdates() {
        targetLocationRequest.run();
    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);




    }
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (connections_working){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            while (location == null){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            Log.i(TAG, "Handle New Location.");
            handleNewLocation(location);


        } else if (!network_connected) {
            Log.i(TAG, "No network.");
            show_alertdialog_network();
        } else {
            Log.i(TAG, "No GPS.");
            show_alertdialog_gps();



        }}

    public void show_alertdialog_network() {
        Log.i(TAG,"show_alertdialog_network");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No network!");
        builder.setMessage("Please turn on wifi or network data.");
        builder.setPositiveButton("To network data", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings$DataUsageSummaryActivity"));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("To wifi", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNeutralButton("Nahh", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(gameMode.this, "No game for you!", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }

    public void show_alertdialog_gps() {
        Log.i(TAG,"show_alertdialog_gps");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No gps!");
        builder.setMessage("Please turn on location services.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Nahh", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(gameMode.this, "No game for you!", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, "handling New Location");
        this.loc = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, String.valueOf(loc));
        if (!zoomed){
            Log.i(TAG,"zooming.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlevel));
            zoomed = true;
        }
        locations.addMyLocation(loc);

        if (circleLoc == null){
            circleLoc = mMap.addCircle(new CircleOptions()
                    .center(loc)
                    .radius(r)
                    .strokeColor(Color.BLUE));
        } else {
            circleLoc.remove();

            if (mMap != null) {
                circleLoc = mMap.addCircle(new CircleOptions()
                        .center(loc)
                        .radius(r)
                        .strokeColor(Color.BLUE));
            }
        }

        addPoints(locations.getMyLocation(locations.getMySize() - 1), locations.getTargetLocation(locations.getTargetSize() - 1));

        if (mymarker != null) {
            mymarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(loc)
                .title("I am here!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mymarker = mMap.addMarker(options);
        Log.i(TAG, "Marker placed.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onconnectionfailed");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Changed.");
        mySpeed=location.getSpeed();
        handleNewLocation(location);



    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Paused.");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            mGoogleApiClient.disconnect();
        }

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        TextView points_score = (TextView) findViewById(R.id.points_score);
        String points_str = (String) points_score.getText();
        int mCurrentScore = Integer.parseInt(points_str);
        editor.putInt(STATE_SCORE, mCurrentScore);
        editor.apply();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Onresume");
        zoomed = false;
        super.onResume();
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) network_connected = true;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) gps_connected = true;
        if (network_connected && gps_connected) connections_working = true;
        Log.i(TAG,"Connecting apiclient");
        mGoogleApiClient.connect();

        TextView points_score = (TextView) findViewById(R.id.points_score);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (preferences.getInt(STATE_SCORE,0) != 0) {
            int mCurrentScore = preferences.getInt(STATE_SCORE, 0);
            String points_str = Integer.toString(mCurrentScore);
            points_score.setText(points_str);}

    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        Log.i(TAG,"CalculationByDistance");
        int Radius = 6371000;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
    }

    public void addPoints(LatLng location, LatLng target) {
        if (CalculationByDistance(location, target) <= r*2) {
            killMovegenerator(null);
            changeTarget(TARGET_MAIN,TARGET_SEC);
        }
    }

    public void killMovegenerator(View view){
        int seconds = c.get(Calendar.SECOND);
        if (seconds<10){
            killMoveAccelor(null);
        }
        else if (seconds>=10 && seconds<20){
            killMoveGyroscoop(null);
        }
        else if (seconds>=20 && seconds<30){
            killMoveSound(null);
        }
        else if (seconds>=40 && seconds<50){
            killMoveSpeed(null);
        }
        else if (seconds>=50&& seconds<55){
            killMovePressButton(null);
        }
        else if (seconds>=55){
            killMovelight(null);
        }
    }

    public void killMoveAccelor(View view) {

        CountDownTimer start = new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            public Sensor_SAVE sensorsave = new Sensor_SAVE();
            SensorCollector sensorcol = new SensorCollector(sensorsave);

            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmoveAcellorText + millisUntilFinished / 1000);
                sensorcol.start(getApplicationContext());
                if (sensorsave.getAccelerox() > killmoveAcellorValue) {
                    killMoveText.setText(killedText);


                }
            }

            public void onFinish() {
                sensorcol.stop();
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    String points_str = (String) points_score.getText();
                    int points_int = Integer.parseInt(points_str);
                    points_int += 100;
                    points_str = Integer.toString(points_int);
                    points_score.setText(points_str);
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }

    public void killMoveSound(View view) {

        CountDownTimer start = new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            SoundAct soundact = new SoundAct(0);
            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmoveSoundText + millisUntilFinished / 1000);
                soundact.getMaxsound();
                if (soundact.getMaxsound() > killmoveSoundValue) {
                    killMoveText.setText(killedText);


                }
            }

            public void onFinish() {
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    String points_str = (String) points_score.getText();
                    int points_int = Integer.parseInt(points_str);
                    points_int += 100;
                    points_str = Integer.toString(points_int);
                    points_score.setText(points_str);
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }
    public void killMoveGyroscoop(View view) {

        CountDownTimer start = new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            public Sensor_SAVE sensorsave = new Sensor_SAVE();
            SensorCollector sensorcol = new SensorCollector(sensorsave);

            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmoveGyroText + millisUntilFinished / 1000);
                sensorcol.start(getApplicationContext());
                if (sensorsave.getGyroscoopx() > killmoveGyroValue) {
                    killMoveText.setText(killedText);


                }
            }
            public void onFinish() {
                sensorcol.stop();
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    String points_str = (String) points_score.getText();
                    int points_int = Integer.parseInt(points_str);
                    points_int += 100;
                    points_str = Integer.toString(points_int);
                    points_score.setText(points_str);
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }
    public void killMovelight(View view) {

        CountDownTimer start = new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            public Sensor_SAVE sensorsave = new Sensor_SAVE();
            SensorCollector sensorcol = new SensorCollector(sensorsave);

            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmovelightText + millisUntilFinished / 1000);
                sensorcol.start(getApplicationContext());
                if (sensorsave.getLicht() < killmovelightValue) {
                    killMoveText.setText(killedText);


                }}



            public void onFinish() {
                sensorcol.stop();
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    String points_str = (String) points_score.getText();
                    int points_int = Integer.parseInt(points_str);
                    points_int += 100;
                    points_str = Integer.toString(points_int);
                    points_score.setText(points_str);
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }

    public void changeTarget() {
        Log.i(TAG,"changeTarget");
        prioritylevel = 0;
        JSONObject data = new JSONObject();
        try {
            data.put("sender", myusername);
            data.put("receiver", "");
            data.put("category", "");
            data.put("message", getPriorities);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mServercomm.sendMessage(data);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Picking target..");
        progressDialog.show();
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                progressDialog.dismiss();
                t.cancel();
            }
        }, 5000);
        TargetID = priorityID;
        requestLocationUpdate();
        progressDialog.setMessage("Tracking target..");
        progressDialog.show();
        t.schedule(new TimerTask() {
            public void run() {
                progressDialog.dismiss();
                t.cancel();
            }
        }, 5000);
    }



    public void killMoveSpeed(View view) {

        CountDownTimer start = new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);

            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmoveSpeedText + millisUntilFinished / 1000);
                if (mySpeed > killmoveSpeedValue) {
                    killMoveText.setText(killedText);
                }
            }

            public void onFinish() {
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    String points_str = (String) points_score.getText();
                    int points_int = Integer.parseInt(points_str);
                    points_int += 100;
                    points_str = Integer.toString(points_int);
                    points_score.setText(points_str);
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();
    }

    public void killMoveCounter(View view){
        kill_button_counter += 1;
    }

    public void killMovePressButton(View view) {
        CountDownTimer start = new CountDownTimer(5000, 200) {
            Button kill_button = (Button) findViewById(R.id.kill_button);
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);

            public void onTick(long millisUntilFinished) {
                killMoveText.setText(killmovePressButtonText + millisUntilFinished / 1000);
                kill_button.setVisibility(View.VISIBLE);
                if (kill_button_counter > killmovePressButtonValue) {
                    kill_button.setText(killedText);
                    killMoveText.setText(killedText);


                }
            }

            public void onFinish() {
                kill_button_counter = 0;
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    String points_str = (String) points_score.getText();
                    int points_int = Integer.parseInt(points_str);
                    points_int += 100;
                    points_str = Integer.toString(points_int);
                    points_score.setText(points_str);
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }

    public void changeTarget(LatLng Target1, LatLng Target2){
        if (CURRENT_TARGET != Target1){
            CURRENT_TARGET = Target1;
        }
        else{
            CURRENT_TARGET = Target2;
        }

        if (markerTarget == null && circleTarget == null){
            markerTarget = mMap.addMarker(new MarkerOptions().position(CURRENT_TARGET).title("TARGET"));
            circleTarget = mMap.addCircle(new CircleOptions()
                    .center(CURRENT_TARGET)
                    .radius(5)
                    .strokeColor(Color.RED));
        }
        else{
            assert markerTarget != null;
            markerTarget.remove();
            circleTarget.remove();

            markerTarget = mMap.addMarker(new MarkerOptions().position(CURRENT_TARGET).title("TARGET"));
            circleTarget = mMap.addCircle(new CircleOptions()
                    .center(CURRENT_TARGET)
                    .radius(5)
                    .strokeColor(Color.RED));

        }

        locations.addTargetLocation(CURRENT_TARGET);
    }

    public void zoombutton(View view) {
        Log.i(TAG, "clicked!");
//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//        Log.i(TAG, String.valueOf(location));
        if (loc != null) {
            Log.i(TAG, "moving camera");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlevel));
        }
    }

    public void targetbutton(View view) {
        Log.i(TAG, "Targetbutton pressed.");
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(loc);
        boundsBuilder.include(CURRENT_TARGET);
// pan to see all markers on map:
        LatLngBounds bounds = boundsBuilder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        TextView points_score = (TextView) findViewById(R.id.points_score);
        String points_str = (String) points_score.getText();
        int mCurrentScore = Integer.parseInt(points_str);
        savedInstanceState.putInt(STATE_SCORE, mCurrentScore);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    public void requestLocationUpdate(){
        Log.i(TAG,"requestLocationUpdate");
        JSONObject data = new JSONObject();
        try{
            data.put("sender", myusername);
            data.put("receiver",TargetID);
            data.put("category",locationUpdate);
            data.put("message",getNewLocation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mServercomm.sendMessage(data);
    }

    public void sendPriority(String sender){
        Log.i(TAG,"Send priority");
        TextView points_score = (TextView) findViewById(R.id.points_score);
        String points_str = (String) points_score.getText();
        int points_int = Integer.parseInt(points_str);
        double priority = 2*Math.log10(points_int);
        priority -= huntedby;
        JSONObject data = new JSONObject();
        try{
            data.put("sender", myusername);
            data.put("receiver",sender);
            data.put("category",priorityCategory);
            data.put("message",Double.toString(priority));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mServercomm.sendMessage(data);
    }

    public void gotEliminated(String sender, String message){
        Log.i(TAG,"gotEliminated");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aww.");
        builder.setMessage("You got killed by +"+sender+" for "+message+" points.");
        builder.setPositiveButton("Continue playing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

                // halveer punten ofzo
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
        }

    public void sendLocation(String sender){
        Log.i(TAG,"sending location");
        LatLng myloc = locations.getLastLocation();
        JSONObject data = new JSONObject();
        try{
            data.put("sender", myusername);
            data.put("receiver",sender);
            data.put("category",locationUpdate);
            data.put("message",giveNewLocation);
            data.put("latitude",Double.toString(myloc.latitude));
            data.put("longitude",Double.toString(myloc.longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mServercomm.sendMessage(data);
    }

    public void updateTargetLocation(LatLng location){
        Log.i(TAG,"updating target location");
        CURRENT_TARGET = location;
        if (markerTarget == null && circleTarget == null){
            markerTarget = mMap.addMarker(new MarkerOptions().position(CURRENT_TARGET).title("TARGET"));
            circleTarget = mMap.addCircle(new CircleOptions()
                    .center(CURRENT_TARGET)
                    .radius(5)
                    .strokeColor(Color.RED));
        }
        else{
            assert markerTarget != null;
            markerTarget.remove();
            circleTarget.remove();

            markerTarget = mMap.addMarker(new MarkerOptions().position(CURRENT_TARGET).title("TARGET"));
            circleTarget = mMap.addCircle(new CircleOptions()
                    .center(CURRENT_TARGET)
                    .radius(5)
                    .strokeColor(Color.RED));

        }

        locations.addTargetLocation(CURRENT_TARGET);
        }



    public void respondToMessage() {
        Log.i(TAG,"starting respondtomessage");
        List receivedmessage = mServercomm.getLastMessage();
        String receiver = (String) receivedmessage.get(0);
        String sender = (String) receivedmessage.get(1);
        String category = (String) receivedmessage.get(2);
        String message = (String) receivedmessage.get(3);

        if (receivedmessage.size()>4) {
            String Latitudestring = (String) receivedmessage.get(4);
            String Longitudestring = (String) receivedmessage.get(5);
            Double Latitude = Double.parseDouble(Latitudestring);
            Double Longitude = Double.parseDouble(Longitudestring);
            targetLocation = new LatLng(Latitude, Longitude);
        }
        if (receiver == ""){
            if (message == NotifyOffline && sender == TargetID){
                changeTarget();
            } else if (message == getPriorities) {
                sendPriority(sender);
            }
        else if (receiver == myusername){
            if (category == eliminated) {
                gotEliminated(sender, message);
            } else if (category == pickedTarget){
                huntedby += 1;
            } else if (category == droppedTarget){
                huntedby -= 1;
            } else if (category == locationUpdate) {
                if (message == getNewLocation){
                    sendLocation(sender);
                }
                else if (message == giveNewLocation){
                    updateTargetLocation(targetLocation);
                }
            } else if (category == priorityCategory){
                if (Double.parseDouble(message) > prioritylevel){
                    prioritylevel = Double.parseDouble(message);
                    priorityID = sender;
                }
            }
        }

        }
    }
    private Runnable targetLocationRequest = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG,"running targetLocationRequest");
            requestLocationUpdate();
            mHandler.postDelayed(targetLocationRequest,30000);
        }};}




