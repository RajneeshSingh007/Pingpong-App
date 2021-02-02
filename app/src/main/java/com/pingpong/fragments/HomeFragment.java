package com.pingpong.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cbr.gradienttextview.GradientTextView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.islamkhsh.CardSliderIndicator;
import com.github.islamkhsh.CardSliderViewPager;
import com.pingpong.Config;
import com.pingpong.ItemMovieActivity;
import com.pingpong.ItemSeriesActivity;
import com.pingpong.MainActivity;
import com.pingpong.R;
import com.pingpong.adapters.CountryAdapter;
import com.pingpong.adapters.GenreAdapter;
import com.pingpong.adapters.GenreHomeAdapter;
import com.pingpong.adapters.HomePageAdapter;
import com.pingpong.adapters.SliderAdapter;
import com.pingpong.models.CommonModels;
import com.pingpong.models.GenreModel;
import com.pingpong.models.home_content.FeaturesGenreAndMovie;
import com.pingpong.models.home_content.HomeContent;
import com.pingpong.models.home_content.LatestMovie;
import com.pingpong.models.home_content.LatestTvseries;
import com.pingpong.models.home_content.Slider;
import com.pingpong.models.home_content.Video;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.HomeContentApi;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.NetworkInst;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;


public class HomeFragment extends Fragment {

    CardSliderViewPager cViewPager;
    private ArrayList<CommonModels> listSlider = new ArrayList<>();
    private Timer timer;

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerViewMovie, recyclerViewTvSeries, recyclerViewGenre;
    private RecyclerView genreRv;
    private RecyclerView countryRv,recentRv;
    private GenreAdapter genreAdapter;
    private CountryAdapter countryAdapter;
    private RelativeLayout genreLayout, countryLayout,recentLayout;
    private HomePageAdapter adapterMovie, adapterSeries,recentWatched;
    private List<CommonModels> listMovie = new ArrayList<>();
    private List<CommonModels> listSeries = new ArrayList<>();
    private List<CommonModels> genreList = new ArrayList<>();
    private List<CommonModels> countryList = new ArrayList<>();
    private List<CommonModels> recentlyWatchedList = new ArrayList<>();
    private Button btnMoreMovie, btnMoreSeries;

    private TextView tvNoItem;
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView scrollView;

    private RelativeLayout adView, adView1;
    private List<GenreModel> listGenre = new ArrayList<>();

    private GenreHomeAdapter genreHomeAdapter;
    private View sliderLayout;

    private MainActivity activity;
    private LinearLayout searchRootLayout;

    private CardView searchBar;
    private ImageView menuIv;
            //searchIv;
    private GradientTextView pageTitle;
    private CardSliderIndicator cardSliderIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adView              = view.findViewById(R.id.adView);
        adView1             = view.findViewById(R.id.adView1);
        btnMoreSeries       = view.findViewById(R.id.btn_more_series);
        btnMoreMovie        = view.findViewById(R.id.btn_more_movie);
        shimmerFrameLayout  = view.findViewById(R.id.shimmer_view_container);
        tvNoItem            = view.findViewById(R.id.tv_noitem);
        coordinatorLayout   = view.findViewById(R.id.coordinator_lyt);
        swipeRefreshLayout  = view.findViewById(R.id.swipe_layout);
        scrollView          = view.findViewById(R.id.scrollView);
        sliderLayout        = view.findViewById(R.id.slider_layout);
        genreRv             = view.findViewById(R.id.genre_rv);
        countryRv           = view.findViewById(R.id.country_rv);
        genreLayout         = view.findViewById(R.id.genre_layout);
        countryLayout       = view.findViewById(R.id.country_layout);
        recentLayout = view.findViewById(R.id.recent_layout);
        cViewPager          = view.findViewById(R.id.c_viewPager);
        searchRootLayout    = view.findViewById(R.id.search_root_layout);
        searchBar           = view.findViewById(R.id.search_bar);
        menuIv              = view.findViewById(R.id.bt_menu);
        pageTitle           = view.findViewById(R.id.page_title_tv);
       // searchIv           = view.findViewById(R.id.search_iv);
        recentRv = view.findViewById(R.id.recent_rv);
        cardSliderIndicator = view.findViewById(R.id.indicator);


//        if (db.getConfigurationData().getAppConfig().getGenreVisible()) {
//            genreLayout.setVisibility(View.VISIBLE);
//        }
//        if (db.getConfigurationData().getAppConfig().getCountryVisible()) {
//            countryLayout.setVisibility(View.VISIBLE);
//        }

        pageTitle.setText(getResources().getString(R.string.app_name));

//        if (activity.isDark) {
//            pageTitle.setTextColor(activity.getResources().getColor(R.color.white));
//            searchBar.setCardBackgroundColor(activity.getResources().getColor(R.color.black_window_light));
//            menuIv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_menu));
//            searchIv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_search_white));
//        }

        //----init timer slider--------------------
        timer = new Timer();

//        PreferenceUtils.updateSubscriptionStatus(getContext());

        //----btn click-------------
        btnClick();

        // --- genre recycler view ---------
        genreRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        genreRv.setHasFixedSize(true);
        genreRv.setNestedScrollingEnabled(false);
        genreAdapter = new GenreAdapter(getContext(), genreList, "genre", "home");
        genreRv.setAdapter(genreAdapter);

        // --- country recycler view ---------
        countryRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        countryRv.setHasFixedSize(true);
        countryRv.setNestedScrollingEnabled(false);
        countryAdapter = new CountryAdapter(getContext(), countryList, "home");
        countryRv.setAdapter(countryAdapter);

        //----movie's recycler view-----------------
        recyclerViewMovie = view.findViewById(R.id.recyclerView);
        recyclerViewMovie.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewMovie.setHasFixedSize(true);
        recyclerViewMovie.setNestedScrollingEnabled(false);
        adapterMovie = new HomePageAdapter(getContext(), listMovie);
        recyclerViewMovie.setAdapter(adapterMovie);

        //----series's recycler view-----------------
        recyclerViewTvSeries = view.findViewById(R.id.recyclerViewTvSeries);
        recyclerViewTvSeries.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTvSeries.setHasFixedSize(true);
        recyclerViewTvSeries.setNestedScrollingEnabled(false);
        adapterSeries = new HomePageAdapter(getContext(), listSeries);
        recyclerViewTvSeries.setAdapter(adapterSeries);

        // --- recent recycler view ---------
        recentRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recentRv.setHasFixedSize(true);
        recentRv.setNestedScrollingEnabled(false);
        recentWatched = new HomePageAdapter(getContext(), recentlyWatchedList);
        recentRv.setAdapter(recentWatched);
        refreshRecent();


        //----genre's recycler view--------------------
        recyclerViewGenre = view.findViewById(R.id.recyclerView_by_genre);
        recyclerViewGenre.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGenre.setHasFixedSize(true);
        recyclerViewGenre.setNestedScrollingEnabled(false);
        genreHomeAdapter = new GenreHomeAdapter(getContext(), listGenre);
        recyclerViewGenre.setAdapter(genreHomeAdapter);

        shimmerFrameLayout.startShimmer();

        if (new NetworkInst(getContext()).isNetworkAvailable()) {

            getHomeContent();

        } else {
            tvNoItem.setText(getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
              //  PreferenceUtils.updateSubscriptionStatus(getContext());

                recyclerViewMovie.removeAllViews();
                recyclerViewTvSeries.removeAllViews();
                recyclerViewGenre.removeAllViews();
                genreRv.removeAllViews();
                countryRv.removeAllViews();

                genreList.clear();
                countryList.clear();
                listMovie.clear();
                listSeries.clear();
                listSlider.clear();
                listGenre.clear();

                refreshRecent();

                if (new NetworkInst(getContext()).isNetworkAvailable()) {

                    getHomeContent();

                } else {
                    tvNoItem.setText(getString(R.string.no_internet));
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    coordinatorLayout.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                }
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

        //getAdDetails();
    }


    private void refreshRecent(){
        recentlyWatchedList.clear();
        recentlyWatchedList = PreferenceUtils.getRecentlyWatched(getContext());
        if(recentlyWatchedList.size() > 0){
            recentLayout.setVisibility(View.VISIBLE);
            recentWatched.setData(recentlyWatchedList);
        }else{
            recentLayout.setVisibility(View.GONE);
        }
    }

    private void getHomeContent() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        HomeContentApi api = retrofit.create(HomeContentApi.class);
        Call<HomeContent> call = api.getHomeContent(Config.API_KEY);
        call.enqueue(new Callback<HomeContent>() {
            @Override
            public void onResponse(Call<HomeContent> call, retrofit2.Response<HomeContent> response) {
                 if (response.code() == 200){
                    swipeRefreshLayout.setRefreshing(false);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                    coordinatorLayout.setVisibility(View.GONE);

                    //slider data
                    Slider slider = response.body().getSlider();
                    if (slider.getSliderType().equalsIgnoreCase("disable")) {
                        sliderLayout.setVisibility(View.GONE);
                    }else if (slider.getSliderType().equalsIgnoreCase("movie")){

                    }else if (slider.getSliderType().equalsIgnoreCase("image")){

                    }
                    if(activity == null){
                        return;
                    }
                    SliderAdapter sliderAdapter = new SliderAdapter(slider.getSlide(), activity);
                    cardSliderIndicator.setIndicatorsToShow(slider.getSlide().size());
                    cViewPager.setAdapter(sliderAdapter);
                    sliderAdapter.notifyDataSetChanged();

                     //latest movies data
                     for (int i = 0; i < response.body().getLatestMovies().size(); i++){
                         LatestMovie movie = response.body().getLatestMovies().get(i);
                         CommonModels models = new CommonModels();
                         models.setImageUrl(movie.getThumbnailUrl());
                         models.setTitle(movie.getTitle());
                         models.setVideoType("movie");
                         models.setReleaseDate(movie.getRelease());
                         models.setQuality(movie.getVideoQuality());
                         models.setId(movie.getVideosId());
                         models.setIsPaid(movie.getIsPaid());
                         listMovie.add(models);
                     }
                     adapterMovie.notifyDataSetChanged();

                     //latest tv series
                     for (int i = 0; i < response.body().getLatestTvseries().size(); i++){
                         LatestTvseries tvSeries = response.body().getLatestTvseries().get(i);
                         CommonModels models = new CommonModels();
                         models.setImageUrl(tvSeries.getThumbnailUrl());
                         models.setTitle(tvSeries.getTitle());
                         models.setVideoType("tvseries");
                         models.setReleaseDate(tvSeries.getRelease());
                         models.setQuality(tvSeries.getVideoQuality());
                         models.setId(tvSeries.getVideosId());
                         models.setIsPaid(tvSeries.getIsPaid());
                         listSeries.add(models);
                     }
                     adapterSeries.notifyDataSetChanged();

                     //get data by genre
                     for (int i = 0; i < response.body().getFeaturesGenreAndMovie().size(); i++){
                         FeaturesGenreAndMovie genreAndMovie = response.body().getFeaturesGenreAndMovie().get(i);
                         GenreModel models = new GenreModel();

                         models.setName(genreAndMovie.getName());
                         models.setId(genreAndMovie.getGenreId());
                         List<CommonModels> listGenreMovie = new ArrayList<>();
                         for (int j = 0; j < genreAndMovie.getVideos().size(); j++){
                             Video video = genreAndMovie.getVideos().get(j);
                             CommonModels commonModels = new CommonModels();

                             commonModels.setId(video.getVideosId());
                             commonModels.setTitle(video.getTitle());
                             commonModels.setIsPaid(video.getIsPaid());

                             if (video.getIsTvseries().equals("0")) {
                                 commonModels.setVideoType("movie");
                             } else {
                                 commonModels.setVideoType("tvseries");
                             }

                             commonModels.setReleaseDate(video.getRelease());
                             commonModels.setQuality(video.getVideoQuality());
                             commonModels.setImageUrl(video.getThumbnailUrl());

                             listGenreMovie.add(commonModels);
                         }
                         models.setList(listGenreMovie);

                         listGenre.add(models);
                         genreHomeAdapter.notifyDataSetChanged();
                     }

                }else {
                     swipeRefreshLayout.setRefreshing(false);
                     shimmerFrameLayout.stopShimmer();
                     shimmerFrameLayout.setVisibility(View.GONE);
                     coordinatorLayout.setVisibility(View.VISIBLE);
                     scrollView.setVisibility(View.GONE);
                 }

            }

            @Override
            public void onFailure(Call<HomeContent> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                coordinatorLayout.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);

            }
        });
    }

    private void btnClick() {

        btnMoreMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ItemMovieActivity.class);
                intent.putExtra("title", "Movies");
                startActivity(intent);
            }
        });

        btnMoreSeries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ItemSeriesActivity.class);
                intent.putExtra("title", "TV Series");
                startActivity(intent);
            }
        });

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

        shimmerFrameLayout.startShimmer();
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmer();
        timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRecent();
    }

    boolean isSearchBarHide = false;

    private void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * searchRootLayout.getHeight()) : 0;
        searchRootLayout.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }


}
