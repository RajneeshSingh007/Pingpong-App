package com.pingpong.nav_fragments;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.Toast;

import com.pingpong.MainActivity;
import com.pingpong.ProfileActivity;
import com.pingpong.R;
import com.pingpong.SearchResultActivity;
import com.pingpong.fragments.HomeFragment;
import com.pingpong.fragments.MoviesFragment;
import com.pingpong.fragments.ProfileFragment;
import com.pingpong.fragments.TvSeriesFragment;
import com.volcaniccoder.bottomify.BottomifyNavigationView;
import com.volcaniccoder.bottomify.OnNavigationItemChangeListener;

import static android.content.Context.MODE_PRIVATE;

public class MainHomeFragment extends Fragment {
    private MainActivity activity;
    private BottomifyNavigationView bottomifyNavigationViewDark;
    LinearLayout searchRootLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_main_home, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomifyNavigationViewDark = view.findViewById(R.id.bottomify_nav);
        searchRootLayout = view.findViewById(R.id.search_root_layout);
        if(searchRootLayout != null){
            searchRootLayout.setVisibility(View.GONE);
        }

        SharedPreferences sharedPreferences = activity.getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        //Log.e("Theme", "onViewCreated: "+isDark);

        bottomifyNavigationViewDark.setVisibility(View.VISIBLE);
        bottomifyNavigationViewDark.setBackgroundColor(getResources().getColor(R.color.black_window_light));

        bottomifyNavigationViewDark.setActiveNavigationIndex(0);
        bottomifyNavigationViewDark.setOnNavigationItemChangedListener(new OnNavigationItemChangeListener() {
            @Override
            public void onNavigationItemChanged(@NonNull BottomifyNavigationView.NavigationItem navigationItem) {
                switch (navigationItem.getPosition()) {
                    case 0:
                        loadFragment(new HomeFragment(),"Home");
                        break;
                    case 1:
                        //Toast.makeText(getContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getContext(), SearchResultActivity.class));
                        break;
                    case 2:
                        loadFragment(new MoviesFragment(),"Movies");
                        break;
                    case 3:
                        loadFragment(new TvSeriesFragment(),"Tv");
                        break;
                    case 4:
                        loadFragment(new ProfileFragment(), "Profile");
                        //Toast.makeText(getContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getContext(), ProfileActivity.class));
                        break;
                }
            }
        });

        loadFragment(new HomeFragment(), "Home");
    }

    private boolean loadFragment(Fragment fragment, String tag){
        if (fragment!=null){
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,fragment, tag)
                    .commit();

            return true;
        }
        return false;

    }

    public void setHomeFragment(){
        if(bottomifyNavigationViewDark != null){
            bottomifyNavigationViewDark.setActiveNavigationIndex(0);
        }
        loadFragment(new HomeFragment(),"Home");
    }
}