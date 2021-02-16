package com.pingpong;

import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pingpong.R;
import com.pingpong.adapters.SearchAdapter;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.SearchApi;
import com.pingpong.network.model.CommonModel;
import com.pingpong.network.model.SearchModel;
import com.pingpong.network.model.TvModel;
import com.pingpong.utils.ApiResources;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.ToastMsg;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class SearchResultActivity extends AppCompatActivity implements SearchAdapter.OnItemClickListener, SearchView.OnQueryTextListener {
    private TextView tvTitle, movieTitle, tvSeriesTv, searchQueryTv;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView movieRv, tvRv, tvSeriesRv;
    private SearchAdapter movieAdapter, tvSeriesAdapter;
    private List<CommonModel> movieList =new ArrayList<>();
    private List<TvModel> tvList =new ArrayList<>();
    private List<CommonModel> tvSeriesList =new ArrayList<>();

    private ApiResources apiResources;

    private String URL=null;
    private boolean isLoading=false;
    private ProgressBar progressBar;
    private int pageCount=1;
    private LinearLayout movieLayout, tvSeriesLayout, tvLayout;
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private SearchView searchView;

    private String type;
    private String query="";
    private int range_to, range_from, tvCategoryId, genreId, countryId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "search_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        searchView = findViewById(R.id.searchView);
        tvTitle=findViewById(R.id.title);
        tvLayout=findViewById(R.id.tv_layout);
        movieLayout=findViewById(R.id.movie_layout);
        tvSeriesLayout=findViewById(R.id.tv_series_layout);
        tvTitle = findViewById(R.id.tv_title);
        movieTitle= findViewById(R.id.movie_title);
        tvSeriesTv = findViewById(R.id.tv_series_title);
        movieRv = findViewById(R.id.movie_rv);
        tvRv = findViewById(R.id.tv_rv);
        tvSeriesRv = findViewById(R.id.tv_series_rv);
        searchQueryTv = findViewById(R.id.title_tv);
        toolbar = findViewById(R.id.toolbar);


        progressBar=findViewById(R.id.item_progress_bar);
        shimmerFrameLayout=findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.setVisibility(View.GONE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        if (isDark) {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.black_window_light));
//            toolbar.setTitleTextColor(Color.WHITE);
//        }

        searchView.setOnQueryTextListener(this);
        View v = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if(v != null){
            v.setBackgroundColor(Color.TRANSPARENT);
        }

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                shimmerFrameLayout.setVisibility(View.GONE);
                movieLayout.setVisibility(View.GONE);
                tvSeriesLayout.setVisibility(View.GONE);
                coordinatorLayout.setVisibility(View.GONE);
                return true;
            }
        });

        URL=new ApiResources().getSearchUrl()+"&&q="+query+"&&page=";

        coordinatorLayout=findViewById(R.id.coordinator_lyt);
        movieRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //movieRv.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(this, 4), true));
        movieRv.setHasFixedSize(true);
        movieAdapter = new SearchAdapter(movieList, this);
        movieAdapter.setOnItemClickListener(this);
        movieRv.setAdapter(movieAdapter);


        tvRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //tvRv.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(this, 8), true));
        tvRv.setHasFixedSize(true);

        tvSeriesRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //tvSeriesRv.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(this, 4), true));
        tvSeriesRv.setHasFixedSize(true);
        tvSeriesAdapter = new SearchAdapter(tvSeriesList, this);
        tvSeriesAdapter.setOnItemClickListener(this);
        tvSeriesRv.setAdapter(tvSeriesAdapter);

//        if (query != null) {
//            getSearchData();
//        }
    }


    public void getSearchData(String query) {
        //Log.e("TAG", "getSearchData: "+query );
        if(TextUtils.isEmpty(query)){
            shimmerFrameLayout.setVisibility(View.GONE);
            movieLayout.setVisibility(View.GONE);
            tvSeriesLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.GONE);
            return;
        }

//        else if(query.length() < 3){
//            shimmerFrameLayout.setVisibility(View.GONE);
//            movieLayout.setVisibility(View.GONE);
//            tvSeriesLayout.setVisibility(View.GONE);
//            coordinatorLayout.setVisibility(View.GONE);
//            return;
//        }

        //String searchType = "movie&tvseries";
        tvList.clear();
        movieList.clear();
        tvSeriesList.clear();
        type = "movie";
        range_to = 0;
        range_from = 0;
        tvCategoryId = 0;
        genreId = 0;
        countryId = 0;
        movieLayout.setVisibility(View.GONE);
        tvSeriesLayout.setVisibility(View.GONE);
        coordinatorLayout.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SearchApi searchApi = retrofit.create(SearchApi.class);
        Call<SearchModel> call = searchApi.getSearchData(Config.API_KEY, query, type, range_to,range_from,tvCategoryId,genreId,countryId);
        call.enqueue(new Callback<SearchModel>() {
            @Override
            public void onResponse(Call<SearchModel> call, retrofit2.Response<SearchModel> response) {
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (response.code() == 200) {
                    SearchModel searchModel = response.body();
                    movieList.clear();
                    tvList.clear();
                    tvSeriesList.clear();

                    if (searchModel.getMovie() != null &&  searchModel.getMovie().size() > 0) {
                        movieList.addAll(searchModel.getMovie());
                        movieAdapter.updateData(movieList);
                        movieLayout.setVisibility(View.VISIBLE);
                    } else {
                        movieLayout.setVisibility(View.GONE);
                    }

                    if (searchModel.getTvChannels() != null &&  searchModel.getTvChannels().size() > 0) {
                        tvList.addAll(searchModel.getTvChannels());
                        tvLayout.setVisibility(View.GONE);
                    } else {
                        tvLayout.setVisibility(View.GONE);
                    }

                    if (searchModel.getTvseries() != null && searchModel.getTvseries().size() > 0) {
                        tvSeriesList.addAll(searchModel.getTvseries());
                        tvSeriesAdapter.updateData(tvSeriesList);
                        tvSeriesLayout.setVisibility(View.VISIBLE);
                    } else {
                        tvSeriesLayout.setVisibility(View.GONE);
                    }

                    if (movieList.size() == 0 && tvSeriesList.size() == 0) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }


                } else {
                    new ToastMsg(SearchResultActivity.this).toastIconSuccess("Something went wrong.");
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<SearchModel> call, Throwable t) {
                movieLayout.setVisibility(View.GONE);
                tvSeriesLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                coordinatorLayout.setVisibility(View.VISIBLE);
                t.printStackTrace();
                new ToastMsg(SearchResultActivity.this).toastIconSuccess("Something went wrong.");
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

    @Override
    public void onItemClick(CommonModel commonModel) {

        String type="";
        if (commonModel.getIsTvseries().equals("1")) {
            type = "tvseries";
        } else {
            type = "movie";
        }

        Intent intent=new Intent(this,DetailsActivity.class);
        intent.putExtra("vType",type);
        intent.putExtra("id",commonModel.getVideosId());
        startActivity(intent);

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        getSearchData(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        getSearchData(s);
        return true;
    }
}
