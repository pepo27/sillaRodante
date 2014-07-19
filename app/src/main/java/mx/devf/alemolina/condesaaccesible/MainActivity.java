package mx.devf.alemolina.condesaaccesible;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // Definition of the buttons, which will be used in various functions
    private Button buttonGreen;
    private Button buttonBlue;
    private Button buttonYellow;
    private Button buttonRed;
    private View.OnClickListener buttonListener;


    private final String TAG = "CondesaAccesible";

    // Definition of "latlng" which will contain current location information.
    private LatLng latlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        // We link our button objects with the Views in the xml
        buttonGreen = (Button) findViewById(R.id.buttonGreen);
        buttonBlue = (Button) findViewById(R.id.buttonBlue);
        buttonYellow = (Button) findViewById(R.id.buttonYellow);
        buttonRed = (Button) findViewById(R.id.buttonRed);

        // The button listener states that, on click, the function dropColorPin will be called
        buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropColorPin((Button) v);
            }
        };

        // we call the function buttonListener
        buttonGreen.setOnClickListener(buttonListener);
        buttonBlue.setOnClickListener(buttonListener);
        buttonYellow.setOnClickListener(buttonListener);
        buttonRed.setOnClickListener(buttonListener);


        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autocompleteView);
        final PlacesAutoCompleteAdapter adapter = new PlacesAutoCompleteAdapter(this, R.layout.list_item);
        autoCompView.setAdapter(adapter);

        AdapterView.OnItemClickListener itemClickLisener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String placeId = adapter.getItemPlaceId(position);
                //TODO: Use Geocoding API in order to save their lat & lon.

            }
        };
        autoCompView.setOnItemClickListener(itemClickLisener);


        Log.i(TAG, "ADIOS");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    // guess what dropColorPin does? You guessed right! (I DO hope so at least) drops a pin, the color will vary depending on the button that was clicked.
    private void dropColorPin(Button button) {
        String title;
        float hue;
        int score;
        if (button.equals(buttonGreen)) {
            title = "Rampa";
            hue = BitmapDescriptorFactory.HUE_GREEN;
            score = 4;
        } else if (button.equals(buttonBlue)) {
            title = "Camino";
            hue = BitmapDescriptorFactory.HUE_AZURE;
            score = 3;
        } else if (button.equals(buttonYellow)) {
            title = "Alerta";
            hue = BitmapDescriptorFactory.HUE_YELLOW;
            score = 2;
        } else {
            title = "Impasable";
            hue = BitmapDescriptorFactory.HUE_RED;
            score = 1;
        }
        // Drops a marker in the current location.
        mMap.addMarker(new MarkerOptions().position(latlng).title(title).icon(BitmapDescriptorFactory.defaultMarker(hue)));
        // Send score to backend
        sendScore(score, latlng);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));

        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void makeUseOfNewLocation(Location location) {
        latlng = new LatLng(location.getLatitude(), location.getLongitude());
        // mMap.addMarker(new MarkerOptions().position(latlng).title("Aqui estas"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    ///////////////////////envio de datos
    public void sendScore(int score, LatLng latlng) {
        try {
            new HTTPTask().execute("" + latlng.latitude, "" + latlng.longitude, "" + score);
        } catch (Exception e) {
            Log.e(TAG, "error");
            e.printStackTrace();
        }
    }

    private class HTTPTask extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... strings) {
            postData(strings[0], strings[1], strings[2]);
            return null;
        }

        protected void onPostExecute(Double result) {
            //  pb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "UBICACION GUARDADA", Toast.LENGTH_LONG).show();
        }

        protected void onProgressUpdate(Integer... progress) {
            //pb.setProgress(progress[0]);
        }

        public void postData(String latitud, String longitud, String tipo) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://pepo27devf.appspot.com/generarPunto");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("latitud", latitud));
                nameValuePairs.add(new BasicNameValuePair("longitud", longitud));
                nameValuePairs.add(new BasicNameValuePair("tipo", tipo));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }

    }


}
