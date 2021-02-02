package com.pingpong;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.pingpong.R;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.DeactivateAccountApi;
import com.pingpong.network.apis.ProfileApi;
import com.pingpong.network.apis.SetPasswordApi;
import com.pingpong.network.apis.UserDataApi;
import com.pingpong.network.model.ResponseStatus;
import com.pingpong.network.model.User;

import com.pingpong.utils.Constants;
import com.pingpong.utils.FileUtil;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.ToastMsg;
import com.pingpong.utils.Tools;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = ShareActivity.class.getSimpleName();
    private boolean isDark;
    private String id;
    private ProgressBar progressBar;
    private TextView referralNumber;
    private Uri longLink;
    private Uri shortLink;
    private FrameLayout shareLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);
        id = PreferenceUtils.getUserId(ShareActivity.this);
        //Log.e(TAG, "onCreate: "+id);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        referralNumber = findViewById(R.id.referralNumber);
        shareLink = findViewById(R.id.shareLink);

//        if (!isDark) {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Share");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "share_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        getProfile();


        referralNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (longLink != null){
                    FirebaseDynamicLinks.getInstance().createDynamicLink()
                            .setLongLink(longLink)
                            .buildShortDynamicLink()
                            .addOnCompleteListener(ShareActivity.this, new OnCompleteListener<ShortDynamicLink>() {
                                @Override
                                public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                    if(task.isSuccessful()){
                                        if(task.getResult() != null){
                                            shortLink = task.getResult().getShortLink();
                                            //Log.e(TAG, "onComplete: "+shortLink.toString());
                                            Tools.shareLink(ShareActivity.this, shortLink);
                                        }
                                    }
                                }
                            });
                }
            }
        });

        shareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (longLink != null){
                    FirebaseDynamicLinks.getInstance().createDynamicLink()
                            .setLongLink(longLink)
                            .buildShortDynamicLink()
                            .addOnCompleteListener(ShareActivity.this, new OnCompleteListener<ShortDynamicLink>() {
                                @Override
                                public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                    if(task.isSuccessful()){
                                        if(task.getResult() != null){
                                            shortLink = task.getResult().getShortLink();
                                            //Log.e(TAG, "onComplete: "+shortLink.toString());
                                            Tools.shareLink(ShareActivity.this, shortLink);
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private void getProfile() {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        UserDataApi api = retrofit.create(UserDataApi.class);
        User user = PreferenceUtils.getUser(this);
        Call<User> call = api.getUserData(Config.API_KEY, user.getUserId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        progressBar.setVisibility(View.GONE);
                        User user = response.body();
                        if (user != null){
                            Log.e(TAG, "onResponse: "+user.getData());
                            referralNumber.setText(user.getPhone());
                            String url = Config.BASE_URL+"?ref="+referralNumber.getText().toString();
                            longLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                    .setLink(Uri.parse(url))
                                    .setDomainUriPrefix("https://pingpongentertainment.page.link")
                                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                                    .buildDynamicLink().getUri();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
