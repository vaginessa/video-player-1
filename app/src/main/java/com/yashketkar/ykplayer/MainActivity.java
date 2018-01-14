package com.yashketkar.ykplayer;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,VideosFragment.OnFragmentInteractionListener, TorrentsFragment.OnFragmentInteractionListener, LiveTVFragment.OnFragmentInteractionListener  {

    private static final String PREF_USER_LEARNED_TORRENT = "torrent_learned";

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Toolbar toolbar;

    private String versioncode;
    private String valert_title;
    private String valert_message;
    private String valert_message_big;
    private String downloadurl;
    private boolean mUserLearnedTorrents;
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        makeJsonObjectRequest();

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        switchfragments(VideosFragment.newInstance());
        navigationView.setCheckedItem(R.id.nav_videos);
        getSupportActionBar().setTitle("Videos");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // videos-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_videos:
                fragment = new VideosFragment().newInstance();
                switchfragments(fragment);
                mTitle="Videos";
                restoreActionBar();
                break;
            case R.id.nav_network_stream:
                AlertDialog.Builder nwalert = new AlertDialog.Builder(this);
                nwalert.setTitle(getString(R.string.nw_alert_title));
                nwalert.setMessage(getString(R.string.nw_alert_message));
                final EditText nwinput = new EditText(this);
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    // only for gingerbread and newer versions
                    nwinput.setTextColor(Color.WHITE);
                }
                nwinput.setSingleLine();
                nwalert.setView(nwinput);
                nwalert.setPositiveButton(getString(R.string.nw_alert_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                playvideo(nwinput.getText().toString()
                                        .replaceAll("[\\t\\n\\r]", ""));
                            }
                        });
                nwalert.setNegativeButton(getString(R.string.nw_alert_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // Canceled.
                            }
                        });
                nwalert.show();
                break;
            case R.id.nav_torrent_stream:
                fragment = new TorrentsFragment().newInstance();
                switchfragments(fragment);
                SharedPreferences sp = this.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                mUserLearnedTorrents = sp.getBoolean(PREF_USER_LEARNED_TORRENT, false);
                if (mUserLearnedTorrents == false) {
                    Intent intent = new Intent(this,
                            TorrentsHelpActivity.class);
                    startActivity(intent);
                }
                mTitle="Torrent Stream";
                restoreActionBar();
                break;
            case R.id.nav_live_tv:
                fragment = new LiveTVFragment().newInstance();
                switchfragments(fragment);
                mTitle="Live TV";
                restoreActionBar();
                break;
            case R.id.nav_share:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_message));
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_title));
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser)));
                break;
            case R.id.nav_website:
                Uri webpage = Uri.parse(getString(R.string.website_link));
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            default:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onVideosFragmentInteraction(String id) {
        playvideo(id);
    }

    public void onLiveTVFragmentInteraction(String id) {
        playvideo(id);
    }

    public void onTorrentsFragmentInteraction(String id) {
        playvideo(id);
    }

    public void onSectionAttached(int number) {
        mTitle = getResources().getStringArray(R.array.nav_drawer_items)[number];
    }

    public void restoreActionBar() {
        toolbar.setTitle(mTitle);
    }

    public void playvideo(String id) {
        Intent intent = new Intent(MainActivity.this,
                VideoPlayerActivity.class);
        intent.putExtra("EXTRA_URL", id);
        startActivity(intent);
    }

    public void switchfragments(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void makeJsonObjectRequest() {

        String urlJsonObj = getString(R.string.version_link);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, response.toString());

                try {
                    // Parsing json object response
                    // response will be a json object
                    versioncode = response.getString("versioncode");
                    valert_title = response.getString("valert_title");
                    valert_message = response.getString("valert_message");
                    valert_message_big = response.getString("valert_message_big");
                    downloadurl = response.getString("downloadurl");

                    if (BuildConfig.VERSION_CODE < Integer.parseInt(versioncode)) {
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(MainActivity.this)
                                        .setSmallIcon(R.drawable.ic_update)
                                        .setContentTitle(valert_title)
                                        .setContentText(valert_message)
                                        .setColor(getResources().getColor(R.color.colorPrimary));
                        Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadurl));

                        // Because clicking the notification opens a new ("special") activity, there's
                        // no need to create an artificial back stack.
                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        MainActivity.this,
                                        0,
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );

                        mBuilder.setContentIntent(resultPendingIntent);
                        mBuilder.setAutoCancel(true);
                        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                        mBuilder.setDefaults(Notification.DEFAULT_ALL);
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(valert_message_big));

                        Notification note = mBuilder.build();
                        note.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

                        // Sets an ID for the notification
                        int mNotificationId = 1;
                        // Gets an instance of the NotificationManager service
                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        // Builds the notification and issues it.
                        mNotifyMgr.notify(mNotificationId, note);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    /*Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();*/
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                /*Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();*/
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }
}
