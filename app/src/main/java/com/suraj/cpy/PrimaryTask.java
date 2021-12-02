package com.suraj.cpy;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PrimaryTask extends AppCompatActivity {
    FirebaseUser fuser;
    boolean is_right_version = true;
    private AdView mAdView;
    SharedPreferences login_details;
    SharedPreferences.Editor editor;
    SharedPreferences groupId;
    SharedPreferences.Editor groupIdEditor;
    int adClicks = 0;
    private static final String base_version = "1";
    private static final String local_version = "3";
    private static final String notification = "no";

    LoadProgressBar progressBar;
    InterstitialAd mInterstitialAd = null;

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        else
            super.onBackPressed();
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        login_details = getSharedPreferences("Login", MODE_PRIVATE);
        editor = login_details.edit();

        groupId = getSharedPreferences("GroupId", MODE_PRIVATE);
        groupIdEditor = groupId.edit();

        progressBar = new LoadProgressBar(PrimaryTask.this);


        getSupportFragmentManager().beginTransaction().replace(R.id.group_container, new login()).commit();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        load_interstitialAd();
        load_bannerAd();

        /*
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Version version = new Version("1", "1", "http://google.com/");
        db.collection("Version").document("version").set(version);*/


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Version").document("version").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Version version = documentSnapshot.toObject(Version.class);
                        if(! version.getBase_version().equals(base_version)) {
                            is_right_version = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "New version of app is available, " +
                                            "please install new version, Redirecting to new version of app",
                                            Toast.LENGTH_SHORT).show();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent("android.intent.action.VIEW",
                                                    Uri.parse(version.getWebsite()));
                                            startActivity(intent);
                                        }
                                    }, 4000);
                                }
                            });
                        }
                        else if(! version.getLocal_version().equals(local_version)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Please update your app, Redirecting to updated version of app...",
                                            Toast.LENGTH_SHORT).show();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent("android.intent.action.VIEW",
                                                    Uri.parse(version.getWebsite()));
                                            startActivity(intent);
                                        }
                                    }, 4000);
                                }
                            });
                        }
                        if(! version.getNotification().equals(notification)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Notification: " +
                                            version.getNotification(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
    }



    private void load_interstitialAd() {
        AdRequest interstitialReq = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-3568641157298310/9482666133", interstitialReq,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                load_interstitialAd();
                            }
                        }, 60000);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        // Handle the error
                        Log.d("hello", loadAdError.getMessage());
                        mInterstitialAd = null;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                load_interstitialAd();
                            }
                        }, 1000);
                    }
                });
    }

    private void load_bannerAd() {
        mAdView = findViewById(R.id.bannerAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                super.onAdFailedToLoad(adError);
                Log.d("error", adError.getMessage());
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdView.loadAd(adRequest);
                    }
                }, 1000);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                /*
                // Code to be executed when the user clicks on an ad.
                Log.d("hello", "clicked");
                adClicks++;
                if(adClicks >=1 ) {
                    Log.d("error", "max ad clicks, blocking ads");
                    mAdView.destroy();
                    mAdView.setVisibility(View.GONE);
                    int prev_clicks = login_details.getInt("adClicks", -1);
                    long prev_time = login_details.getLong("adclickTime", 0);
                    if(prev_clicks == -1) {
                        editor.putLong("adclickTime", System.currentTimeMillis());
                        editor.putInt("adClicks", 1);
                        editor.commit();
                        return;
                    }
                    if(System.currentTimeMillis() - prev_time < 86400000) {
                        prev_clicks++;
                        if(prev_clicks == 1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Please stop intentionally clicking ads, next time we will block you...",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        else if(prev_clicks > 1)
                            block_user = true;
                        editor.putLong("adclickTime", System.currentTimeMillis());
                        editor.putInt("adClicks", prev_clicks);
                        editor.commit();
                    }
                    else {
                        editor.putLong("adclickTime", System.currentTimeMillis());
                        editor.putInt("adClicks", 0);
                        editor.commit();
                    }
                }*/
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }
}