package com.pingpong;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private EditText etName, etEmail, etPhone, genderSpinner,etLname;
    private EditText etPass, etCurrentPassword;
    private Button btnUpdate, deactivateBt, setPasswordBtn;
    private ProgressDialog dialog;
    private String URL = "", strGender;
    private CircleImageView userIv;
    private static final int GALLERY_REQUEST_CODE = 1;
    private Uri imageUri;
    private ProgressBar progressBar;
    private String id;
    private boolean isDark;
    private String selectedGender = "Male";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);

//        if (!isDark) {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "profile_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etPhone = findViewById(R.id.phone);
        etPass = findViewById(R.id.password);
        etLname = findViewById(R.id.lname);
        etCurrentPassword = findViewById(R.id.currentPassword);
        btnUpdate = findViewById(R.id.signup);
        userIv = findViewById(R.id.user_iv);
        progressBar = findViewById(R.id.progress_bar);
        deactivateBt = findViewById(R.id.deactive_bt);
        genderSpinner = findViewById(R.id.gender_spinner);
        setPasswordBtn = findViewById(R.id.setPasswordBtn);

        id = PreferenceUtils.getUserId(ProfileActivity.this);


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEmail.getText().toString().equals("")) {
                    Toast.makeText(ProfileActivity.this, "Please enter valid email.", Toast.LENGTH_LONG).show();
                    return;
                } else if (etName.getText().toString().equals("")) {
                    Toast.makeText(ProfileActivity.this, "Please enter name.", Toast.LENGTH_LONG).show();
                    return;
                } else if (etCurrentPassword.getText().toString().equals("")) {
                    new ToastMsg(ProfileActivity.this).toastIconError("Current password must not be empty.");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                String email = etEmail.getText().toString();
                String phone = etPhone.getText().toString();
                String pass = etPass.getText().toString();
                String currentPass = etCurrentPassword.getText().toString();
                String name = etName.getText().toString();
                String lname = etLname.getText().toString();

                updateProfile(id, email, phone, name, pass, currentPass,lname);

            }
        });

        setPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSetPasswordDialog();
            }
        });

        //gender spinner setup
        final String[] genderArray = new String[2];
        genderArray[0] = "Male";
        genderArray[1] = "Female";
        genderSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Select Gender");
                builder.setSingleChoiceItems(genderArray, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((TextView) v).setText(genderArray[i]);
                        selectedGender = genderArray[i];
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        getProfile();
    }


    @Override
    protected void onStart() {
        super.onStart();

        userIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openGallery();
            }
        });

        deactivateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeactivationDialog();
            }
        });
    }

    private void showDeactivationDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_deactivate, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        final EditText passEt = view.findViewById(R.id.pass_et);
        final EditText reasonEt = view.findViewById(R.id.reason_et);
        final Button okBt = view.findViewById(R.id.ok_bt);
        Button cancelBt = view.findViewById(R.id.cancel_bt);
        ImageView closeIv = view.findViewById(R.id.close_iv);
        final ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        LinearLayout topLayout = view.findViewById(R.id.top_layout);
        if (isDark) {
            topLayout.setBackgroundColor(getResources().getColor(R.color.overlay_dark_30));
        }

        okBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = passEt.getText().toString();
                String reason = reasonEt.getText().toString();

                if (TextUtils.isEmpty(pass)) {
                    new ToastMsg(ProfileActivity.this).toastIconError("Please enter your password");
                    return;
                } else if (TextUtils.isEmpty(reason)) {
                    new ToastMsg(ProfileActivity.this).toastIconError("Please enter your reason");
                    return;
                }
                deactivateAccount(pass, reason, alertDialog, progressBar);
            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


    }

    private void deactivateAccount(String pass, String reason, final AlertDialog alertDialog, final ProgressBar progressBar) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        DeactivateAccountApi api = retrofit.create(DeactivateAccountApi.class);
        Call<ResponseStatus> call = api.deactivateAccount(id, pass, reason, Config.API_KEY);
        call.enqueue(new Callback<ResponseStatus>() {
            @Override
            public void onResponse(Call<ResponseStatus> call, retrofit2.Response<ResponseStatus> response) {
                if (response.code() == 200) {
                    ResponseStatus resStatus = response.body();
                    if (resStatus.getStatus().equalsIgnoreCase("success")) {
                        logoutUser();

                        new ToastMsg(ProfileActivity.this).toastIconSuccess(resStatus.getData());

                        if (PreferenceUtils.isMandatoryLogin(ProfileActivity.this)) {
                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        alertDialog.dismiss();
                        finish();
                    } else {
                        new ToastMsg(ProfileActivity.this).toastIconError(resStatus.getData());
                        alertDialog.dismiss();
                    }

                } else {
                    new ToastMsg(ProfileActivity.this).toastIconError("Something went wrong");
                    alertDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseStatus> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(ProfileActivity.this).toastIconError("Something went wrong");
                alertDialog.dismiss();
            }
        });
    }

    public void logoutUser() {
        DatabaseHelper databaseHelper = new DatabaseHelper(ProfileActivity.this);
        databaseHelper.deleteAllDownloadData();
        databaseHelper.deleteUserData();
        databaseHelper.deleteAllActiveStatusData();

        SharedPreferences.Editor sp = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
        sp.putBoolean(Constants.USER_LOGIN_STATUS, false);
        sp.apply();
        sp.commit();
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    if(selectedImage == null){
                        return;
                    }
                    Glide.with(ProfileActivity.this)
                            .load(selectedImage)
                            .apply(new RequestOptions()
                                    .error(R.drawable.avatar)
                                    .placeholder(R.drawable.avatar))
                            .into(userIv);
                    imageUri = selectedImage;
                }
                break;
        }
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
                        //Log.e(TAG, "onResponse:"+user.getImageUrl());
                        //Log.e(TAG, "onResponse:"+user.getPhone());
                       /* Picasso.get()
                                .load(user.getImageUrl())
                                .placeholder(R.drawable.ic_account_circle_black)
                                .error(R.drawable.ic_account_circle_black)
                                .into(userIv);*/

//                        Glide.with(ProfileActivity.this)
//                                .load(user.getImageUrl())
//                                .apply(new RequestOptions()
//                                .error(R.drawable.avatar)
//                                .placeholder(R.drawable.avatar))
//                                .into(userIv);

                        if(user != null){
                            if(!TextUtils.isEmpty(user.getName())){
                                if(user.getName().contains(" ")){
                                    String arr[] = user.getName().split(" ");
                                    if(arr.length == 1){
                                        etName.setText(user.getName());
                                    }else{
                                        etName.setText(arr[0]);
                                        etLname.setText(arr[1]);
                                    }
                                }else{
                                    etName.setText(user.getName());
                                }
                            }
                            etEmail.setText(user.getEmail());
                            etPhone.setText(user.getPhone());

                            etPhone.setEnabled(false);
                            etPhone.setFocusableInTouchMode(false);
                            etPhone.setFocusable(false);

//                            if (user.getGender() != null) {
//                                genderSpinner.setText(R.string.male);
//                            } else {
//                                genderSpinner.setText(user.getGender());
//                                selectedGender = user.getGender();
//                            }

                            //if (!user.isPasswordAvailable()) {
                            //btnUpdate.setVisibility(View.VISIBLE);
                            etCurrentPassword.setVisibility(View.VISIBLE);
                            etPass.setVisibility(View.VISIBLE);
                            setPasswordBtn.setVisibility(View.GONE);
                            //}
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

    private void updateProfile(String idString, String emailString, String phoneString, String nameString, String passString, String currentPassString, String lname) {
        File file = null;
        RequestBody requestFile = null;
        MultipartBody.Part multipartBody = null;
        try {
            if (imageUri != null) {
                file = FileUtil.from(ProfileActivity.this, imageUri);
                requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),
                        file);

                multipartBody = MultipartBody.Part.createFormData("photo",
                        file.getName(), requestFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), emailString);
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), idString);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), nameString+" "+lname);
        RequestBody password = RequestBody.create(MediaType.parse("text/plain"), passString);
        RequestBody phone = RequestBody.create(MediaType.parse("text/plain"), phoneString);
        RequestBody currentPass = RequestBody.create(MediaType.parse("text/plain"), currentPassString);
        RequestBody gender = RequestBody.create(MediaType.parse("text/plain"), selectedGender);

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ProfileApi api = retrofit.create(ProfileApi.class);
        Call<ResponseStatus> call = api.updateProfile(Config.API_KEY, id, name, email, phone, password, currentPass, multipartBody, gender);
        call.enqueue(new Callback<ResponseStatus>() {
            @Override
            public void onResponse(Call<ResponseStatus> call, retrofit2.Response<ResponseStatus> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        new ToastMsg(ProfileActivity.this).toastIconSuccess(response.body().getData());
                        getProfile();
                    } else {
                        new ToastMsg(ProfileActivity.this).toastIconError(response.body().getData());
                    }
                } else {
                    new ToastMsg(ProfileActivity.this).toastIconError(getString(R.string.something_went_wrong));
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseStatus> call, Throwable t) {
                new ToastMsg(ProfileActivity.this).toastIconError(getString(R.string.something_went_wrong));
                progressBar.setVisibility(View.GONE);
                //Log.e(TAG, t.getLocalizedMessage());
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

    private void showSetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.password_entry_layout, null);
        builder.setView(view);
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        EditText passEt = view.findViewById(R.id.passwordEt);
        EditText confirmPassEt = view.findViewById(R.id.confirmPasswordEt);
        Button setButton = view.findViewById(R.id.setButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = passEt.getText().toString();
                String confirmPass = confirmPassEt.getText().toString();
                if (!password.isEmpty() && !confirmPass.isEmpty()) {
                    if (password.equals(confirmPass)) {
                        // send password to server
                        alertDialog.dismiss();
                        setPassword(password);
                    } else {
                        confirmPassEt.setError("Password mismatch.");
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    private void setPassword(String password) {
        ProgressDialog dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setMessage("Please wait..");
        dialog.setCancelable(false);
        dialog.show();
        //get UID from firebase auth
        String uid = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            uid = user.getUid();
        else {
            dialog.dismiss();
            new ToastMsg(ProfileActivity.this).toastIconError(getString(R.string.something_went_text));
            return;
        }

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SetPasswordApi api = retrofit.create(SetPasswordApi.class);
        Call<ResponseStatus> call = api.setPassword(Config.API_KEY, id, password, uid);
        call.enqueue(new Callback<ResponseStatus>() {
            @Override
            public void onResponse(Call<ResponseStatus> call, Response<ResponseStatus> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        ResponseStatus status = response.body();
                        if (status.getStatus().equalsIgnoreCase("success")) {
                            new ToastMsg(ProfileActivity.this).toastIconSuccess("Password set successfully.");
                            //password set successfully.
                            //visible hidden buttons
                            setPasswordBtn.setVisibility(View.GONE);
                            btnUpdate.setVisibility(View.VISIBLE);
                            etCurrentPassword.setVisibility(View.VISIBLE);
                            etPass.setVisibility(View.VISIBLE);
                            getProfile();

                        } else {
                            new ToastMsg(ProfileActivity.this).toastIconError(getString(R.string.something_went_text));
                        }
                    } else {
                        new ToastMsg(ProfileActivity.this).toastIconError(getString(R.string.something_went_text));
                    }
                } else {
                    new ToastMsg(ProfileActivity.this).toastIconError(getString(R.string.something_went_text));
                }

                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseStatus> call, Throwable t) {
                new ToastMsg(ProfileActivity.this).toastIconError(getString(R.string.something_went_text));
                //Log.e("ProfileActivity", t.getLocalizedMessage());
                dialog.dismiss();
            }
        });
    }

}
