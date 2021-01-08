package com.pingpong.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pingpong.DetailsActivity;
import com.pingpong.GetStarted;
import com.pingpong.GlideApp;
import com.pingpong.HelpActivity;
import com.pingpong.MainActivity;
import com.pingpong.ProfileActivity;
import com.pingpong.R;
import com.pingpong.SettingsActivity;
import com.pingpong.ShareActivity;
import com.pingpong.SubscriptionActivity;
import com.pingpong.TermsActivity;
import com.pingpong.adapters.CommonGridAdapter;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.network.model.User;
import com.pingpong.utils.ApiResources;
import com.pingpong.utils.Constants;
import com.pingpong.utils.NetworkInst;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.SpacingItemDecoration;
import com.pingpong.utils.ToastMsg;
import com.pingpong.utils.Tools;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private MainActivity activity;
    private String id;
    private TextView name,status,help,settings,notify,profile,logout,affiliate,subscription,vipswallet,vipsludo;
    private TextView page_title_tv;
    private User user;
    private ActiveStatus getStatus;
    private NestedScrollView scrollView;
    private LinearLayout searchRootLayout;
    private ImageView menuIv;
    private CircleImageView user_iv;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();

        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity == null){
                    return;
                }
                activity.openDrawer();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        id = PreferenceUtils.getUserId(getContext());
        user = PreferenceUtils.getUser(getContext());
        getStatus = PreferenceUtils.getStatus(getContext());
        initComponent(view);
    }

    private void initComponent(View view) {
        user_iv = view.findViewById(R.id.user_iv);
        searchRootLayout    = view.findViewById(R.id.search_root_layout);
        page_title_tv = view.findViewById(R.id.page_title_tv);
        name = view.findViewById(R.id.name);
        status = view.findViewById(R.id.sbstatus);
        help = view.findViewById(R.id.help);
        settings = view.findViewById(R.id.settings);
        logout = view.findViewById(R.id.logout);
        profile = view.findViewById(R.id.profile);
        notify = view.findViewById(R.id.notify);
        scrollView  = view.findViewById(R.id.scrollView);
        menuIv              = view.findViewById(R.id.bt_menu);
        affiliate = view.findViewById(R.id.affiliate);
        subscription = view.findViewById(R.id.subscription);
        vipswallet = view.findViewById(R.id.vipswallet);
        vipsludo = view.findViewById(R.id.vipsludo);

        affiliate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(getContext(),TermsActivity.class);
//                i.putExtra("title", "Affiliate");
//                i.putExtra("url",getString(R.string.affiliate_link));
//                startActivity(i);
//                Intent affLink = new Intent(Intent.ACTION_VIEW);
//                affLink.setData(Uri.parse(getContext().getString(R.string.affiliate_link)));
//                startActivity(affLink);
                Tools.launchVipsAff(activity);
            }
        });

        vipswallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity == null){
                    return;
                }
                Tools.launchVipsShopping(activity);
//                Intent wallet = new Intent(Intent.ACTION_VIEW);
//                wallet.setData(Uri.parse(getContext().getString(R.string.wallet_link)));
//                startActivity(wallet);
            }
        });

        vipsludo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ludo = new Intent(getContext(), TermsActivity.class);
                ludo.putExtra("title", "Vips Ludo");
                ludo.putExtra("url",getString(R.string.ludogame_link));
                //ludo.setData(Uri.parse(getContext().getString(R.string.ludogame_link)));
                //if(ludo.resolveActivity(getContext().getPackageManager()) != null){
                   startActivity(ludo);
                //}else{
                    //new ToastMsg(getContext()).toastIconError("No app found..");
                //}
            }
        });

        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SubscriptionActivity.class);
                startActivity(intent);
            }
        });

//        GlideApp
//                .with(getContext())
//                .load(user.getImageUrl())
//                .error(R.drawable.avatar)
//                .placeholder(R.drawable.avatar)
//                .into(user_iv);

        name.setText(user.getName());

        status.setText(getStatus.getPackageTitle());


        page_title_tv.setText("My Profile");
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });

        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ShareActivity.class);
                startActivity(intent);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), HelpActivity.class);
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext()).setMessage("Are you sure to logout ?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null){
                                    FirebaseAuth.getInstance().signOut();
                                }

                                SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                                editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
                                editor.apply();
                                editor.commit();
                                PreferenceUtils.deleteAllData(getContext(),true, true);
                                Intent intent = new Intent(getContext(), GetStarted.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
            }
        });


        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY < oldScrollY) { // up
                    animateSearchBar(false);
                }
                if (scrollY > oldScrollY) { // down
                    animateSearchBar(true);
                }
            }
        });
    }


    boolean isSearchBarHide = false;

    private void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * searchRootLayout.getHeight()) : 0;
        searchRootLayout.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

}
