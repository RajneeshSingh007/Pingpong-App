package com.pingpong;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.pingpong.R;
import com.pingpong.adapters.ActiveSubscriptionAdapter;
import com.pingpong.adapters.InactiveSubscriptionAdapter;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.SubscriptionApi;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.network.model.ActiveSubscription;
import com.pingpong.network.model.SubscriptionHistory;
import com.pingpong.network.model.User;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.NetworkInst;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.ToastMsg;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SubscriptionActivity extends AppCompatActivity implements ActiveSubscriptionAdapter.OnItemClickLiestener {
    private RecyclerView mInactiveRv;
    private CardView activePlanLayout;
    private TextView activeUserName, activeEmail, activeActivePlan, activeExpireDate, activePlanAmt, activePaidAmt, activePaymentMode;
    private LinearLayout mNoActiveLayout, mSubHistoryLayout, mSubRootLayout;
    private Button mUpgradeBt;
    private Toolbar mToolbar;
    private ProgressBar progressBar;
    private TextView mNoHistoryTv;
    private CoordinatorLayout mNoInternetLayout;
    private ShimmerFrameLayout shimmerFrameLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout historyHeaderLayout;
    private RelativeLayout activeTitleLayout, historyTitleLayout;
    private View historyView, activeView;
    private InactiveSubscriptionAdapter inactiveSubscriptionAdapter;

    private List<ActiveSubscription> activeSubscriptions = new ArrayList<>();
    private boolean isDark;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        setContentView(R.layout.activity_subscription);

        intiView();

        // change toolbar color as theme color
//        if (isDark) {
//            mToolbar.setBackgroundColor(getResources().getColor(R.color.black_window_light));
//
//            historyView.setBackgroundColor(getResources().getColor(R.color.black_1_transarent));
//            activeView.setBackgroundColor(getResources().getColor(R.color.black_1_transarent));
//            activeTitleLayout.setBackgroundColor(getResources().getColor(R.color.black_transparent));
//            historyTitleLayout.setBackgroundColor(getResources().getColor(R.color.black_transparent));
//            historyHeaderLayout.setBackgroundColor(getResources().getColor(R.color.black_1_transarent));
//            mUpgradeBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_primary));
//        }

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Subscription");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mInactiveRv.setLayoutManager(new LinearLayoutManager(this));
        mInactiveRv.setHasFixedSize(true);
        mInactiveRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        user = PreferenceUtils.getUser(SubscriptionActivity.this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shimmerFrameLayout.startShimmer();
                shimmerFrameLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.GONE);
                if (new NetworkInst(SubscriptionActivity.this).isNetworkAvailable()) {
                    getSubscriptionHistory();
                    getActiveSubscriptionFromDatabase();
                } else {
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    swipeRefreshLayout.setRefreshing(false);
                    mNoInternetLayout.setVisibility(View.VISIBLE);
                    mSubRootLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        shimmerFrameLayout.startShimmer();
        swipeRefreshLayout.setVisibility(View.GONE);

        if (new NetworkInst(this).isNetworkAvailable()) {
            getSubscriptionHistory();
            getActiveSubscriptionFromDatabase();
        } else {

            shimmerFrameLayout.setVisibility(View.GONE);
            shimmerFrameLayout.stopShimmer();
            swipeRefreshLayout.setRefreshing(false);
            mNoInternetLayout.setVisibility(View.VISIBLE);
            mSubRootLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        mUpgradeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SubscriptionActivity.this, PurchasePlanActivity.class));
            }
        });
    }

    private void getSubscriptionHistory() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<SubscriptionHistory> call = subscriptionApi.getSubscriptionHistory(Config.API_KEY, user.getUserId());
        call.enqueue(new Callback<SubscriptionHistory>() {
            @Override
            public void onResponse(Call<SubscriptionHistory> call, Response<SubscriptionHistory> response) {
                if (response.code() == 200) {

                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    if(response.body() !=  null){
                        SubscriptionHistory subscriptionHistory = response.body();
                        activeSubscriptions = subscriptionHistory.getActiveSubscription();
                        if (subscriptionHistory.getActiveSubscription() != null && subscriptionHistory.getActiveSubscription().size() > 0) {
                            mNoActiveLayout.setVisibility(View.GONE);
                        } else {
                            mNoActiveLayout.setVisibility(View.VISIBLE);
                        }

                        if (subscriptionHistory.getInactiveSubscription() != null && subscriptionHistory.getInactiveSubscription().size() > 0) {
                            mNoHistoryTv.setVisibility(View.GONE);
                            mSubHistoryLayout.setVisibility(View.VISIBLE);
                            inactiveSubscriptionAdapter = new InactiveSubscriptionAdapter(subscriptionHistory.getInactiveSubscription(),
                                    SubscriptionActivity.this);
                            mInactiveRv.setAdapter(inactiveSubscriptionAdapter);

                        } else {
                            mNoHistoryTv.setVisibility(View.VISIBLE);
                            mSubHistoryLayout.setVisibility(View.GONE);
                        }
                    }else{
                        mNoActiveLayout.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<SubscriptionHistory> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void getActiveSubscriptionFromDatabase() {
        activePlanLayout.setVisibility(VISIBLE);
        activeUserName.setText(user.getName());
        activeEmail.setText(user.getEmail());
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<ActiveStatus> subcriptionCall = subscriptionApi.getActiveStatus(Config.API_KEY, user.getUserId());
        subcriptionCall.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if(response.code() == 200){
                    if(response.body() != null){
                        ActiveStatus activeStatus = response.body();
                        if(activeStatus != null){
                            activeActivePlan.setText(activeStatus.getPackageTitle());
                            activeExpireDate.setText(activeStatus.getExpireDate());
                            if(!TextUtils.isEmpty(activeStatus.getPlanamt())){
                                activePlanAmt.setText(activeStatus.getPlanamt()+"₹");
                            }
                            if(!TextUtils.isEmpty(activeStatus.getPaidamt())){
                                activePaidAmt.setText(activeStatus.getPaidamt()+"₹");
                            }
                            if(!TextUtils.isEmpty(activeStatus.getPayment_method())){
                                activePaymentMode.setText(activeStatus.getPayment_method());
                            }
                        }
                    }
                }else{
                    activePlanLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                activePlanLayout.setVisibility(View.GONE);
            }
        });
    }

    private void intiView() {
        mUpgradeBt = findViewById(R.id.upgrade_bt);
        mToolbar = findViewById(R.id.subscription_toolbar);
        mInactiveRv = findViewById(R.id.inactive_sub_rv);
        mNoActiveLayout = findViewById(R.id.no_current_sub_layout);
        progressBar = findViewById(R.id.progress_bar);
        mSubHistoryLayout = findViewById(R.id.sub_history_layout);
        mNoHistoryTv = findViewById(R.id.no_history_tv);
        mNoInternetLayout = findViewById(R.id.coordinator_lyt);
        mSubRootLayout = findViewById(R.id.sub_root_layout);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        historyHeaderLayout = findViewById(R.id.history_header_layout);
        activeTitleLayout = findViewById(R.id.active_layout_title);
        historyTitleLayout = findViewById(R.id.history_layout_title);
        historyView = findViewById(R.id.history_view);
        activeView = findViewById(R.id.active_view);
        activePlanLayout = findViewById(R.id.active_plan_card_view);
        activeUserName = findViewById(R.id.active_user_name);
        activeEmail = findViewById(R.id.active_email);
        activeActivePlan = findViewById(R.id.active_active_plan);
        activeExpireDate = findViewById(R.id.active_expire_date);
        activePlanAmt = findViewById(R.id.active_plan_amount);
        activePaidAmt = findViewById(R.id.active_paid_amount);
        activePaymentMode = findViewById(R.id.payment_mode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick() {

    }

    @Override
    public void onCancelBtClick(final String subscriptionId, final int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(true);
//        builder.setTitle("Warning");
//        builder.setIcon(R.drawable.ic_warning);
//        builder.setMessage("Are you want to cancel this subscription?");
//        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                cancelSubscription(subscriptionId, position);
//                dialog.dismiss();
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
    }

//    public void cancelSubscription(String subscriptionId, int pos) {
//
//        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
//        SubscriptionApi api = retrofit.create(SubscriptionApi.class);
//        Call<ResponseBody> call = api.cancelSubscription(Config.API_KEY, user.getUserId(), subscriptionId);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
//                if (response.isSuccessful()) {
//                    if (response.code() == 200) {
//                        List<ActiveSubscription> temp = activeSubscriptions;
//                        activeSubscriptions.clear();
//                        activeSubscriptions.addAll(temp);
//                        // update subscription active status if there are no more subscription have
//                        if (activeSubscriptions.size() == 0) {
//                            updateActiveStatus();
//                        }
//                        recreate();
//                        Toast.makeText(SubscriptionActivity.this, "Subscription canceled successfully.", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(SubscriptionActivity.this, "Subscription canceled Failed. code:"+ response.code(), Toast.LENGTH_SHORT).show();
//                    }
//
//                } else {
//                    Toast.makeText(SubscriptionActivity.this, response.errorBody().toString(), Toast.LENGTH_SHORT).show();
//                }
//            }
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
//
//    }
//
//    private void updateActiveStatus() {
//       PreferenceUtils.updateSubscriptionStatus(SubscriptionActivity.this);
//    }
}
