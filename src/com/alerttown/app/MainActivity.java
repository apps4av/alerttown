package com.alerttown.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import android.widget.TextView;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * 
 * @author zkhan
 *
 * Main app view
 */
public class MainActivity extends Activity implements Observer, OnMarkerClickListener, OnMapClickListener, ClusterManager.OnClusterClickListener<MyItem>, ClusterManager.OnClusterItemClickListener<MyItem> {

    private GoogleMap map;
    
    private AlertDialog mAlertDialogCrime;
    private AlertDialog mAlertDialogOK;
    private View mCrimeView;
    private StorageService mService;
    private boolean mInit;
    private Button mButtonSearchCancel;

    private Button mReportButton;
    private Button mHelpButton;
    HashMap<String, MyItem> mEventMapItem;
    private AddressToGps mAddressResolver;
    List<Address> mAList;
    private Address mNotifyAddress;
    
    private CrimeAdapter mCrimeAdapter;
    private ListView mCrimeReportList;
    private ListView mListSearch;
    
    private String mEmail;
    private Location mLocation;
    
    // Declare a variable for the cluster manager.
    ClusterManager<MyItem> mClusterManager;

    EditText mSearchText;
    
    private static final int SEARCH_MIN_LENGTH = 4;
    
    
    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

        mEmail = PossibleEmail.get(getApplicationContext());
        /*
         * Registering our receiver
         * Bind now.
         */
        Intent intent = new Intent(this, StorageService.class);
        startService(intent);

        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        
        /*
         * Clean up on pause that was started in on resume
         */
        getApplicationContext().unbindService(mConnection);
        
        if(mService != null) {
            mService.setEventListener(null);
        }

    }

    /**
     * 
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * Kill dialogs
         */
        try {
            mAlertDialogCrime.dismiss();
        }
        catch (Exception e) {
            
        }
        try {
            mAlertDialogOK.dismiss();
        }
        catch (Exception e) {
            
        }
        
    }

    /**
     * 
     */
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.activity_main, null);
        setContentView(view);
        
        mCrimeView = layoutInflater.inflate(R.layout.report, null);
        
        mInit = true;
        
        String[] list = getResources().getStringArray(R.array.crime_type);
        mCrimeReportList = (ListView)mCrimeView.findViewById(R.id.report_list);
        mCrimeAdapter = new CrimeAdapter(MainActivity.this, list);
        mCrimeReportList.setAdapter(mCrimeAdapter);
        mCrimeReportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                
                Put put = new Put();
                Report c = new Report();
                c.username = mEmail;
                c.lat = "" + mLocation.getLatitude();
                c.lon = "" + mLocation.getLongitude();
                c.type = ((TextView)view.findViewById(R.id.crime_list_text)).getText().toString();
                c.uuid = UUID.randomUUID().toString();
                c.notes = Util.getPhoneNumber(getApplicationContext());
                if(c.notes == null) {
                    c.notes = "";
                }
                put.addObserver(MainActivity.this);
                put.putIt(c);
                ReportFilter.submit(MainActivity.this);
                try {
                    mAlertDialogCrime.dismiss();
                }
                catch (Exception e) {
                }
            }
        });

        
        mAddressResolver = new AddressToGps();
        mAddressResolver.addObserver(this);
        
        mEventMapItem = new HashMap<String, MyItem>();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.clear();
        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(this, map);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setRenderer(new ItemRenderer(getApplicationContext(), map, mClusterManager));

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        map.setOnCameraChangeListener(mClusterManager);

        /*
         * When address item clicked
         */
        mListSearch = (ListView)view.findViewById(R.id.map_search_list);
        mListSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                if(null != mAList) {
                    mNotifyAddress = mAList.get(position);
                    LatLng loc = new LatLng(mNotifyAddress.getLatitude(), mNotifyAddress.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                    mListSearch.setVisibility(View.INVISIBLE);
                    mSearchText.setText("");
                    hideKeyboard();

                }
            }
        });


        /*
         * Cancel search and set text to ""
         */
        mButtonSearchCancel = (Button)view.findViewById(R.id.location_button_cancel);
        mButtonSearchCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListSearch.setVisibility(View.INVISIBLE);
                mSearchText.setText("");
                hideKeyboard();
            }
            
        });

        /*
         * Show help
         */
        mHelpButton = (Button)view.findViewById(R.id.location_button_help);
        mHelpButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                startActivity(intent);
            }    
        });

        
        /*
         * 
         */
        map.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
                if(mInit) {
                    // start up condition
                    LatLng loc = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 10));

                    mInit = false;
                }                
            }           
        });
       
        mAlertDialogCrime = new AlertDialog.Builder(this).create();
        mAlertDialogCrime.setTitle(getString(R.string.action_report));
        mAlertDialogCrime.setView(mCrimeView);
        mAlertDialogCrime.setCanceledOnTouchOutside(false);
        mAlertDialogCrime.setCancelable(false);
        mAlertDialogCrime.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            /* (non-Javadoc)
             * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
             */
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        /*
         * Deal with reporting
         */
        mReportButton = (Button)view.findViewById(R.id.location_button_report);
        mReportButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                
                if(mEmail == null || mEmail.equals("")) {
                    makeOKDialog(getString(R.string.NoAccount));
                    return;
                }
                
                if(!ReportFilter.canSubmit(MainActivity.this)) {
                    
                    if(Put.getLastReport() != null) {
                        makeOKDialog(getString(R.string.ReceivedReport) + " \""
                                + Put.getLastReport().type + "\", " + 
                                Util.getTimeAgo(getApplicationContext(), Put.getLastReport().reported) + 
                                ". " + getString(R.string.TooManyReports));
                    }
                    return;                    
                }
                
                /*
                 * Get location of report
                 */
                mLocation = null;
                if(map.getMyLocation() != null) {
                    mLocation = map.getMyLocation();
                }
                else if(mService.getLocation() != null) {
                    mLocation = mService.getLocation();
                }
                if(mLocation == null) {
                    makeOKDialog(getString(R.string.NoLocation));
                    return;
                }

                mAlertDialogCrime.show();
            }
            
        });


        /*
         * Search bar
         */
        mSearchText = (EditText)view.findViewById(R.id.location_text_search);
        mSearchText.addTextChangedListener(new TextWatcher() { 
            @Override
            public void afterTextChanged(Editable arg0) {
            }
    
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
    
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int after) {
                if(s.length() > SEARCH_MIN_LENGTH) {
                    mAddressResolver.get(getApplicationContext(), s.toString());
                }
                else {
                    mListSearch.setVisibility(View.INVISIBLE);
                }
            }
        });
        
        /**
         * Handle intent from notification bar or others
         */
        handleIntent(getIntent());

    }

    /**
     * Intent from the notification bar when user clicks an item there
     * @param intent
     */
    private void handleIntent(Intent intent) {
        
        /*
         * Get intent from notification
         */
        String location = intent.getStringExtra("location");
        if(location != null) {
            Integer id = intent.getIntExtra("id", 0);
            /*
             * Remove from notification bar
             */
            (new NotificationBar(getApplicationContext())).cancelNotification(id);
            
            /*
             * Go to where intent is drawn
             */
            String token[] = location.split(",");
            LatLng l = new LatLng(Double.parseDouble(token[1]), Double.parseDouble(token[0]));
            if(map != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(l, 20));
            }
        }      
    }
    
    /**
     * Intent from notification bar
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    
    /**
     * 
     */
    @Override
    public void update(Observable observable, Object data) {
        
        if(data instanceof Report) {
            /*
             * result of Put
             */
            if(((Report)data).submitted) {
                makeOKDialog(getString(R.string.SuccessSubmit));
            }
            else {
                makeOKDialog(getString(R.string.NoInternet));
                mService.backupReport((Report)data);
            }
        }
        else if (data instanceof List<?>) {
            
            mAList = (List<Address>)data; 
            final ArrayList<String> list = new ArrayList<String>();
            for(Address a : mAList) {
                list.add(a.getAddressLine(0));
            }

            final ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                    android.R.layout.simple_list_item_1, list);
            mListSearch.setAdapter(adapter);
            if(mSearchText.getText().toString().length() > SEARCH_MIN_LENGTH) {
                mListSearch.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     * 
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
         */
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            /* 
             * We've bound to LocalService, cast the IBinder and get LocalService instance
             */
            StorageService.LocalBinder binder = (StorageService.LocalBinder)service;
            mService = binder.getService();
            mService.setEventListener(new EventInterface() {

                @Override
                public void getCallback(HashMap<Long, Event> event) {
                    draw(event);
                }

                @Override
                public void locationCallback(Location location) {
                }
            });

        }

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    /**
     * Draw markers
     */
    private void draw(HashMap<Long, Event> events) {
        // clear everything and draw new
        mClusterManager.clearItems();
        mEventMapItem.clear();
        
        // draw all
        Object[] list = events.values().toArray();
        for(int i = 0; i < list.length; i++) {
            Event e = (Event)list[i];
            Bitmap b = mCrimeAdapter.getBitmap(e.type);
            MyItem it = new MyItem(e.lat, e.lon, b, e.type, e.reported, e.id, e.count);
            
            if(e.isFar()) {
                continue;
            }
            
            mClusterManager.addItem(it);
        }
        
        /*
         * Redraw the map
         */
        
        mClusterManager.cluster();                    
    }
    
    /**
     * Hide the soft keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }
    
    /**
     * 
     * @param message
     */
    private void makeOKDialog(String message) {
        
        /*
         * Just a dilog with a given message and OK 
         */
        mAlertDialogOK = new AlertDialog.Builder(MainActivity.this).create();
        mAlertDialogOK.setCanceledOnTouchOutside(false);
        mAlertDialogOK.setCancelable(false);
        mAlertDialogOK.setTitle(getString(R.string.message));
        mAlertDialogOK.setMessage(message);
        mAlertDialogOK.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
            
        });

        mAlertDialogOK.show();
    }
    

    /**
     * 
     */
    @Override
    public boolean onMarkerClick(Marker m) {

        // on marker click, populate its snippet, title
        MyItem item = mEventMapItem.get(m.getId());
        if(null != item) {
            m.setSnippet(Util.getTimeAgo(getApplicationContext(), item.getTime()) + 
            " (" + item.getCount() + " " + getString(R.string.reports) + ")");
            m.setTitle(item.getTitle());
        }
        
        mClusterManager.onMarkerClick(m);
        return false;
    }

    /**
     * 
     */
    @Override
    public void onMapClick(LatLng arg0) {
    }
    

    /**
     * 
     */
    @Override
    public boolean onClusterItemClick(MyItem item) {
        return false;
    }

    /**
     * 
     */
    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) {
        
        /*
         * Calculate geographic area of cluster.
         */
        
        LatLngBounds.Builder builder = LatLngBounds.builder();
        
        Collection<MyItem> ci = cluster.getItems();
        for(MyItem item : ci) {
            builder.include(item.getPosition());
        }
       
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 0);
        map.moveCamera(cameraUpdate);
        
        return false;
    }
    
    
    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class ItemRenderer extends DefaultClusterRenderer<MyItem> {

        /**
         * 
         */
        @Override
        protected void onClusterItemRendered(MyItem item, Marker marker) {
            super.onClusterItemRendered(item, marker);
            mEventMapItem.put(marker.getId(), item);
        }

        /**
         * 
         * @param context
         * @param map
         * @param clusterManager
         */
        public ItemRenderer(Context context, GoogleMap map,
                ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }
        
        
        /**
         * 
         */
        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);
            markerOptions
                .icon(BitmapDescriptorFactory.fromBitmap(item.getBitmap()));
        }
    }


    /**
     * This finds drag and pan by user
     */
    private long mLastTouched;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouched = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                long now = System.currentTimeMillis();
                if (now - mLastTouched > 100) {
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

}

