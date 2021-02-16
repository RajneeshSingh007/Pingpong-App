package com.pingpong;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.PlayerMessage;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.ads.AdPlaybackState;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.common.images.WebImage;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pingpong.adapters.CastCrewAdapter;
import com.pingpong.adapters.EpisodeAdapter;
import com.pingpong.adapters.DownloadAdapter;
import com.pingpong.adapters.HomePageAdapter;
import com.pingpong.adapters.ProgramAdapter;
import com.pingpong.adapters.ServerAdapter;
import com.pingpong.models.CastCrew;
import com.pingpong.models.GetCommentsModel;
import com.pingpong.models.CommonModels;
import com.pingpong.models.EpiModel;
import com.pingpong.models.PostCommentModel;
import com.pingpong.models.Program;
import com.pingpong.models.SubtitleModel;
import com.pingpong.models.single_details.Cast;
import com.pingpong.models.single_details.Director;
import com.pingpong.models.single_details.DownloadLink;
import com.pingpong.models.single_details.Episode;
import com.pingpong.models.single_details.Genre;
import com.pingpong.models.single_details.RelatedMovie;
import com.pingpong.models.single_details.Season;
import com.pingpong.models.single_details.SingleDetails;
import com.pingpong.models.single_details.Subtitle;
import com.pingpong.models.single_details.Video;
import com.pingpong.models.single_details_tv.AdditionalMediaSource;
import com.pingpong.models.single_details_tv.AllTvChannel;
import com.pingpong.models.single_details_tv.ProgramGuide;
import com.pingpong.models.single_details_tv.SingleDetailsTV;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.CommentApi;
import com.pingpong.network.apis.FavouriteApi;
import com.pingpong.network.apis.SingleDetailsApi;
import com.pingpong.network.apis.SingleDetailsTVApi;
import com.pingpong.network.apis.SubscriptionApi;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.network.model.FavoriteModel;
import com.pingpong.service.DownloadWorkManager;
import com.pingpong.utils.Connectivity;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.Constants;
import com.pingpong.utils.ToastMsg;
import com.pingpong.utils.Tools;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DetailsActivity extends AppCompatActivity implements SessionAvailabilityListener, ProgramAdapter.OnProgramClickListener, EpisodeAdapter.OnTVSeriesEpisodeItemClickListener,
        Runnable,View.OnTouchListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PRELOAD_TIME_S = 20;
    public static final String TAG = DetailsActivity.class.getSimpleName();
    private TextView tvName, tvDirector, tvRelease, tvCast, tvDes, tvGenre, tvRelated,eptxt;
    private RecyclerView rvDirector, rvServer, rvRelated, rvComment, castRv;
    private Spinner seasonSpinner;
    private LinearLayout seasonSpinnerContainer;
    public static RelativeLayout lPlay;
    private RelativeLayout contentDetails;
    private LinearLayout topbarLayout,episodeLayout;
    private RelativeLayout subscriptionLayout;
    private Button subscribeBt;
    private ImageView backIv;

    private ServerAdapter serverAdapter;
    private DownloadAdapter internalDownloadAdapter, externalDownloadAdapter;
    private HomePageAdapter relatedAdapter;
    private CastCrewAdapter castCrewAdapter;

    int start = 0;
    private List<CommonModels> listServer = new ArrayList<>();
    private List<CommonModels> trailerServer = new ArrayList<>();
    private List<CommonModels> listRelated = new ArrayList<>();
    private List<GetCommentsModel> listComment = new ArrayList<>();
    private List<CommonModels> listDownload = new ArrayList<>();
    private List<CommonModels> listInternalDownload = new ArrayList<>();
    private List<CommonModels> listExternalDownload = new ArrayList<>();
    private List<CastCrew> castCrews = new ArrayList<>();
    private String strDirector = "", strCast = "", strGenre = "";
    public static LinearLayout llBottom, llBottomParent;
    public static RelativeLayout llcomment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String type = "", id = "",cloneType="";
    private ImageView imgAddFav, shareIv2;
    public static ImageView imgBack, serverIv;
    private Button watchNowBt, downloadBt;
    private ImageView thumbIv;
    private ImageView posterIv;
    private String V_URL = "",Img_URL="",IMAGE_THUMB="";
    public static WebView webView;
    public static ProgressBar progressBar;
    private boolean isFav = false;
    private TextView chromeCastTv;
    private ShimmerFrameLayout shimmerFrameLayout;
    private Button btnComment;
    private EditText etComment;
    private RelativeLayout adView;

    public static SimpleExoPlayer player;
    public static PlayerView simpleExoPlayerView;
    public PlayerControlView castControlView;
    public static SubtitleView subtitleView;

    public static ImageView imgFull;
    public ImageView aspectRatioIv, externalPlayerIv, volumControlIv;
    private LinearLayout volumnControlLayout;
    private SeekBar volumnSeekbar;
    private TextView volumnTv;
    public MediaRouteButton mediaRouteButton;
    private CastContext castContext;

    public static boolean isPlaying, isFullScr = false;
    public static View playerLayout;

    private int playerHeight;
    public static boolean isVideo = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String strSubtitle = "Null";
    public static MediaSource mediaSource = null;
    public static ImageView imgSubtitle;
    public static ImageView imgAudioTrack;
    public ImageButton exo_play;
    private List<SubtitleModel> listSub = new ArrayList<>();
    private AlertDialog alertDialog;
    private String mediaUrl;
    private boolean tv = false;
    private String download_check = "";

    private String season;
    private String episod;
    private String movieTitle;
    private String seriesTitle;

    private CastPlayer castPlayer;
    private boolean castSession;
    private String title;
    String castImageUrl;
    private String isPaid="";

    private LinearLayout tvLayout, sheduleLayout, tvTopLayout;
    private TextView tvTitleTv, watchStatusTv, timeTv, programTv, proGuideTv, watchLiveTv, cloneName, cloneGenre, cloneFullName;
    private ProgramAdapter programAdapter;
    List<Program> programs = new ArrayList<>();
    private RecyclerView programRv;
    private ImageView tvThumbIv, shareIv;

    private LinearLayout exoRewind, exoForward, seekbarLayout;
    ImageView exoDownloadIv;
    private TextView liveTv;


    boolean isDark;
    private OrientationEventListener myOrientationEventListener;
    private String serverType;

    private boolean fullScreenByClick;
    private String currentProgramTime;
    private String currentProgramTitle;
    private String userId;

    private String youtubeDownloadUr;
    private String urlType = "";
    private RelativeLayout descriptionLayout;
    private MaterialRippleLayout descriptionContatainer;
    private TextView dGenryTv, dGenryTv1;
    private RecyclerView internalServerRv, externalServerRv, serverRv;
    private LinearLayout internalDownloadLayout, externalDownloadLayout;
    private boolean activeMovie = false;

    private TextView sereisTitleTv;
    private RelativeLayout seriestLayout;
    private ImageView favIv;

    private RelativeLayout mRlTouch;
    private MaterialRippleLayout cloneLayout;
    private boolean intLeft, intRight;
    private int sWidth, sHeight;
    private long diffX, diffY;
    private Display display;
    private Point size;
    private float downX, downY;
    private AudioManager mAudioManager;
    private int aspectClickCount = 1,nextPos = -1;
    private String paid = "";
    private ProgressDialog dialog;
    private Button trailerwatch;
    private String episodeID = "",ogID="";
    private ImageButton exo_rew,exo_ffwd;
    private EpiModel nextEpiModel;
    private DefaultTimeBar defaultTimeBar;
    private EpisodeAdapter episodeAdapter;
    private boolean firstTimeSkip = true;
    private Handler handler;
    private LinearLayout adsView;

    private Button skipIntroBtn;
    private String skipTimer = "";
    private String free_time = "0";
    private boolean clikedSkip = true;
    private int backClickedCount = -1;

    private ImaAdsLoader adsLoader;


    private Dialog volumeDialog, brightnessDialog;
    private int mScreenWidth, mScreenHeight;
    private int mGestureDownVolume, rotation = 0;
    private float mGestureDownBrightness;
    private ImageView dialogVolImageView, dialogBrightnessImageView;
    private TextView dialogVolProgressText, dialogBrightnessProgressText;
    private ProgressBar dialogVolProgressBar, dialogBrightnessProgressBar;
    private boolean isVol = false, isBrightness = false;
    private LayerDrawable volumeDrawable, brightnessDrawable;
    private float mDownX, mDownX2, mDownY, mDownY2;
    private final int THRESHOLD = 80;
    private RelativeLayout controllerlayout;
    private String adsUrl = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);


        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "details_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        adView = findViewById(R.id.adView);
        llBottom = findViewById(R.id.llbottom);
        tvDes = findViewById(R.id.tv_details);
        tvCast = findViewById(R.id.tv_cast);
        tvRelease = findViewById(R.id.tv_release_date);
        tvName = findViewById(R.id.text_name);
        tvDirector = findViewById(R.id.tv_director);
        tvGenre = findViewById(R.id.tv_genre);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        imgAddFav = findViewById(R.id.add_fav);
        imgBack = findViewById(R.id.img_back);
        cloneFullName = findViewById(R.id.text_namefull);
        //exo_play = findViewById(R.id.exo_play);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        llBottomParent = findViewById(R.id.llbottomparent);
        lPlay = findViewById(R.id.play);
        rvRelated = findViewById(R.id.rv_related);
        tvRelated = findViewById(R.id.tv_related);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        btnComment = findViewById(R.id.btn_comment);
        etComment = findViewById(R.id.et_comment);
        rvComment = findViewById(R.id.recyclerView_comment);
        llcomment = findViewById(R.id.llcomments);
        simpleExoPlayerView = findViewById(R.id.video_view);
        subtitleView = findViewById(R.id.subtitle);
        playerLayout = findViewById(R.id.player_layout);
        imgFull = findViewById(R.id.img_full_scr);
        aspectRatioIv = findViewById(R.id.aspect_ratio_iv);
        externalPlayerIv = findViewById(R.id.external_player_iv);
        volumControlIv = findViewById(R.id.volumn_control_iv);
        volumnControlLayout = findViewById(R.id.volumn_layout);
        volumnSeekbar = findViewById(R.id.volumn_seekbar);
        volumnTv = findViewById(R.id.volumn_tv);
        rvServer = findViewById(R.id.rv_server_list);
        seasonSpinner = findViewById(R.id.season_spinner);
        seasonSpinnerContainer = findViewById(R.id.spinner_container);
        eptxt = findViewById(R.id.eptxt);
        episodeLayout = findViewById(R.id.episodeLayout);
        imgSubtitle = findViewById(R.id.img_subtitle);
        imgAudioTrack = findViewById(R.id.audio_track);
        mediaRouteButton = findViewById(R.id.media_route_button);
        chromeCastTv = findViewById(R.id.chrome_cast_tv);
        castControlView = findViewById(R.id.cast_control_view);
        tvLayout = findViewById(R.id.tv_layout);
        sheduleLayout = findViewById(R.id.p_shedule_layout);
        tvTitleTv = findViewById(R.id.tv_title_tv);
        programRv = findViewById(R.id.program_guide_rv);
        tvTopLayout = findViewById(R.id.tv_top_layout);
        tvThumbIv = findViewById(R.id.tv_thumb_iv);
        shareIv = findViewById(R.id.share_iv);
        watchStatusTv = findViewById(R.id.watch_status_tv);
        timeTv = findViewById(R.id.time_tv);
        programTv = findViewById(R.id.program_type_tv);
        exoRewind = findViewById(R.id.rewind_layout);
        exoForward = findViewById(R.id.forward_layout);
        seekbarLayout = findViewById(R.id.seekbar_layout);
        liveTv = findViewById(R.id.live_tv);
        castRv = findViewById(R.id.cast_rv);
        proGuideTv = findViewById(R.id.pro_guide_tv);
        watchLiveTv = findViewById(R.id.watch_live_tv);

        contentDetails = findViewById(R.id.content_details);
        subscriptionLayout = findViewById(R.id.subscribe_layout);
        subscribeBt = findViewById(R.id.subscribe_bt);
        backIv = findViewById(R.id.des_back_iv);
        topbarLayout = findViewById(R.id.topbar);

        descriptionLayout = findViewById(R.id.description_layout);
        //descriptionContatainer = findViewById(R.id.lyt_parent);
        watchNowBt = findViewById(R.id.watch_now_bt);
        downloadBt = findViewById(R.id.download_bt);
        posterIv = findViewById(R.id.poster_iv);
        thumbIv = findViewById(R.id.image_thumb);
        dGenryTv = findViewById(R.id.genre_tv);
        dGenryTv1 = findViewById(R.id.genre_tv1);
        serverIv = findViewById(R.id.img_server);

        seriestLayout = findViewById(R.id.series_layout);
        favIv = findViewById(R.id.add_fav2);
        sereisTitleTv = findViewById(R.id.seriest_title_tv);
        shareIv2 = findViewById(R.id.share_iv2);
        cloneLayout = findViewById(R.id.cloneLayout);
        cloneName = findViewById(R.id.text_name1);
        cloneGenre = findViewById(R.id.genre_tv2);
        trailerwatch = findViewById(R.id.trailerwatch);
        skipIntroBtn = findViewById(R.id.skipIntro);
        adsView = findViewById(R.id.adsview);
        controllerlayout = findViewById(R.id.controllerlayout);

        exo_rew = findViewById(R.id.exo_rew);
        exo_ffwd = findViewById(R.id.exo_ffwd);
        defaultTimeBar = findViewById(R.id.exo_progress);

        skipIntroBtn.setVisibility(GONE);
        seriestLayout.setVisibility(GONE);
        dGenryTv1.setVisibility(GONE);
        cloneLayout.setVisibility(GONE);
        chromeCastTv.setVisibility(GONE);
        serverIv.setVisibility(GONE);
        aspectRatioIv.setVisibility(VISIBLE);
        adsView.setVisibility(GONE);

        Uri uri = Uri.parse("");
        adsLoader = new ImaAdsLoader(this,uri);

        handler = new Handler();

        defaultTimeBar.setBufferedColor(ContextCompat.getColor(this, R.color.grey_40));
        defaultTimeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                if(player != null){
                    PreferenceUtils.addWatchHistory(DetailsActivity.this, ogID, player.getCurrentPosition(),type, episodeID, IMAGE_THUMB,false);
                }
            }
        });
//        if (isDark) {
//            tvTopLayout.setBackgroundColor(getResources().getColor(R.color.black_window_light));
//            sheduleLayout.setBackground(getResources().getDrawable(R.drawable.rounded_black_transparent));
//            etComment.setBackground(getResources().getDrawable(R.drawable.round_grey_transparent));
//            btnComment.setTextColor(getResources().getColor(R.color.grey_20));
//            topbarLayout.setBackgroundColor(getResources().getColor(R.color.dark));
//            subscribeBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
//
//            descriptionContatainer.setBackground(getResources().getDrawable(R.drawable.gradient_black_transparent));
//        }
        // chrome cast
        exo_rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBackward();
            }
        });
        exo_ffwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekForward();
            }
        });
//        exo_play.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(player != null){
//                    player.setPlayWhenReady(true);
//                }
//            }
//        });
        //CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        //castContext = CastContext.getSharedInstance(this);
        //castPlayer = new CastPlayer(castContext);
        //castPlayer.setSessionAvailabilityListener(this);

        // cast button will show if the cast device will be available
        //if (castContext.getCastState() != CastState.NO_DEVICES_AVAILABLE)
        //mediaRouteButton.setVisibility(View.VISIBLE);
        // start the shimmer effect
        shimmerFrameLayout.startShimmer();
        playerHeight = lPlay.getLayoutParams().height;
        progressBar.setMax(100); // 100 maximum value for the progress value
        progressBar.setProgress(50);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        type = getIntent().getStringExtra("vType");
        cloneType = type;
        id = getIntent().getStringExtra("id");
        ogID = id;
        castSession = getIntent().getBooleanExtra("castSession", false);

//        System.out.println(type);
//        System.out.println(id);
//        System.out.println(castSession);

        // getting user login info for favourite button visibility
        userId = PreferenceUtils.getUserId(DetailsActivity.this);
        if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
            imgAddFav.setVisibility(VISIBLE);
        } else {
            imgAddFav.setVisibility(GONE);
        }
        rvComment.setVisibility(GONE);
        imgFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                controlFullScreenPlayer();

            }
        });
        imgAudioTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        imgSubtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubtitleDialog(DetailsActivity.this, listSub);
            }
        });
//        btnComment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
//                    startActivity(new Intent(DetailsActivity.this, GetStarted.class));
//                    new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.login_first));
//                } else if (etComment.getText().toString().equals("")) {
//                    new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.comment_empty));
//                } else {
//                    String comment = etComment.getText().toString();
//                    addComment( id, PreferenceUtils.getUserId(DetailsActivity.this), comment);
//                }
//            }
//        });

//        imgAddFav.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isFav) {
//                    removeFromFav();
//                } else {
//                    addToFav();
//                }
//            }
//        });

        // its for tv series only when description layout visibility gone.
//        favIv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isFav) {
//                    removeFromFav();
//                } else {
//                    addToFav();
//                }
//            }
//        });


        if (!isNetworkAvailable()) {
            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_internet));
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clear_previous();
                initGetData();
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        skipIntroBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player != null) {
                    long convertedTime = Tools.convertTimeToMill(skipTimer);
                    //Log.e(TAG, "convertedTime: " + convertedTime);
                    player.seekTo(convertedTime);
                    skipIntroBtn.setVisibility(GONE);
                }
            }
        });

        controllerlayout.setOnTouchListener(this);
    }


    private void seekForward(){
        if(player != null){
            if(player.getCurrentPosition() >= 0 && player.getCurrentPosition() <= player.getDuration()){
                long pos = player.getCurrentPosition()+10000;
                player.seekTo(pos);
                PreferenceUtils.addWatchHistory(DetailsActivity.this, ogID, pos,type, episodeID, IMAGE_THUMB,false);
            }
        }
    }

    private void seekBackward(){
        if(player != null){
            if(player.getCurrentPosition() <= player.getDuration() && player.getCurrentPosition() > 0) {
                long pos = player.getCurrentPosition()-10000;
                player.seekTo(pos);
                PreferenceUtils.addWatchHistory(DetailsActivity.this, ogID, pos,type, episodeID, IMAGE_THUMB,false);
            }
        }

    }

    private void hideSystemUI() {
        new Handler(Looper.myLooper()).post(new Runnable() {
            @Override
            public void run() {
                View decorView = getWindow().getDecorView();
                int uiOptions = decorView.getSystemUiVisibility();
                int newUiOptions = uiOptions;
                newUiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
                newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(newUiOptions);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });
        hideBrightnessDialog();
        hideVolDialog();
    }

    private void showSystemUI() {
        new Handler(Looper.myLooper()).post(new Runnable() {
            @Override
            public void run() {
                View decorView = getWindow().getDecorView();
                int uiOptions = decorView.getSystemUiVisibility();
                int newUiOptions = uiOptions;
                newUiOptions &= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
                newUiOptions &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
                newUiOptions &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                newUiOptions &= ~View.SYSTEM_UI_FLAG_IMMERSIVE;
                newUiOptions &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(newUiOptions);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
        });
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            hideSystemUI();
//        }
//    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void controlFullScreenPlayer() {
        if(nextEpiModel != null && episodeAdapter != null){
            episodeAdapter.changeHolder(episodeAdapter.getNextPos());
        }
        if (isFullScr) {
            showSystemUI();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            fullScreenByClick = false;
            isFullScr = false;
            swipeRefreshLayout.setVisibility(VISIBLE);
            cloneFullName.setVisibility(GONE);
            //aspectRatioIv.setVisibility(GONE);
            simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            //player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            if (isVideo) {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));
            } else {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));
            }
            backClickedCount = 1;
        } else {
            backClickedCount = 2;
            cloneFullName.setVisibility(VISIBLE);
            //imgFull.setVisibility(GONE);
            hideSystemUI();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

            //aspectRatioIv.setVisibility(VISIBLE);
            //simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            //player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            fullScreenByClick = true;
            isFullScr = true;
            swipeRefreshLayout.setVisibility(GONE);
            if (isVideo) {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            } else {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        //if (!Config.ENABLE_EXTERNAL_PLAYER){
        externalPlayerIv.setVisibility(GONE);
        //}
        clear_previous();
        initGetData();

        if (mAudioManager != null) {
            volumnSeekbar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            int currentVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            volumnSeekbar.setProgress(currentVolumn);
        }

        volumnSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    //volumnTv.setText(i+"");
                    //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                    mAudioManager.setStreamVolume(player.getAudioStreamType(), i, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        volumControlIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                volumnControlLayout.setVisibility(VISIBLE);

            }
        });

        aspectRatioIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aspectClickCount == 1) {
                    simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    player.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    aspectClickCount = 2;
                } else if (aspectClickCount == 2) {
                    simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    player.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    aspectClickCount = 3;
                } else if (aspectClickCount == 3) {
                    simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    player.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    aspectClickCount = 4;
                } else if (aspectClickCount == 4) {
                    simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
                    player.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    aspectClickCount = 5;
                } else if (aspectClickCount == 5) {
                    simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                    player.setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    aspectClickCount = 1;
                }


            }
        });

        externalPlayerIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaUrl != null) {
                    if (!tv) {
                        // set player normal/ potrait screen if not tv
                        descriptionLayout.setVisibility(VISIBLE);
                        setPlayerNormalScreen();
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(mediaUrl), "video/*");
                    startActivity(Intent.createChooser(intent, "Complete action using"));
                }

            }
        });

        trailerwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playTrailers();
            }
        });
        watchNowBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new ToastMsg(DetailsActivity.this).toastIconError("Coming Soon!!");
                if (!listServer.isEmpty()) {
//                    if(checkActivePlan()){
//                        if(type.equalsIgnoreCase("tvseries")){
//                            if(listServer.size() > 0 && listServer.get(0).getListEpi().size() > 0){
//                                seasonSpinnerContainer.setVisibility(VISIBLE);
//                                episodeLayout.setVisibility(VISIBLE);
//                            }else{
//                                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_video_found));
//                                seasonSpinnerContainer.setVisibility(GONE);
//                                episodeLayout.setVisibility(GONE);
//                            }
//                        }else {
//                            preparePlayer(listServer.get(0));
//                            descriptionLayout.setVisibility(GONE);
//                            lPlay.setVisibility(VISIBLE);
//                        }
//                    }else{
//                        startPlanActivity();
//                    }

                    //free_time or paid based
                    if(type.equalsIgnoreCase("tvseries")){
                        if(listServer.size() > 0 && listServer.get(0).getListEpi().size() > 0){
                            seasonSpinnerContainer.setVisibility(VISIBLE);
                            episodeLayout.setVisibility(VISIBLE);
                        }else{
                            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_video_found));
                            seasonSpinnerContainer.setVisibility(GONE);
                            episodeLayout.setVisibility(GONE);
                        }
                    }else {
                        paid = listServer.get(0).getPaid();
                        free_time = listServer.get(0).getFree_time();
                        if(free_time.equalsIgnoreCase("0")){
                            if(checkActivePlan()){
                                preparePlayer(listServer.get(0));
                                descriptionLayout.setVisibility(GONE);
                                lPlay.setVisibility(VISIBLE);
                            }else{
                                startPlanActivity();
                            }
                        }else{
                            preparePlayer(listServer.get(0));
                            descriptionLayout.setVisibility(GONE);
                            lPlay.setVisibility(VISIBLE);
                        }
                    }

                    ////Log.e(TAG, "onClick: "+paid);
//                    if (!TextUtils.isEmpty(paid)){
//                        if(paid.equalsIgnoreCase("1")){
//                            ActiveStatus activeStatus = PreferenceUtils.getStatus(DetailsActivity.this);
//                            if(activeStatus == null){
//                                startPlanActivity();
//                            }else if(!TextUtils.isEmpty(activeStatus.getStatus()) && activeStatus.getStatus().equalsIgnoreCase("active") && activeStatus.getPackageTitle().equalsIgnoreCase("Trial")){
//                                startPlanActivity();
//                            }else if(!TextUtils.isEmpty(activeStatus.getStatus()) && activeStatus.getStatus().equalsIgnoreCase("active")){
//                                if(Tools.compareDates(PreferenceUtils.getCurrentDate(), activeStatus.getExpireDate())){
//
//                                }else{
//                                    startPlanActivity();
//                                }
//                            }else{
//                                startPlanActivity();
//                            }
//                        }else{
//
//                        }
//                    }else{
//                        new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_video_found));
//                    }
                }else{
                    new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_video_found));
                }
            }
        });

        downloadBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listInternalDownload.isEmpty() || !listExternalDownload.isEmpty()) {
                    if(checkActivePlan()){
                        openDownloadServerDialog();
                    }
                } else {
                    Toast.makeText(DetailsActivity.this, R.string.no_download_server_found, Toast.LENGTH_SHORT).show();
                }
            }
        });

//        watchLiveTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hideExoControlForTv();
//                initMoviePlayer(mediaUrl, serverType, DetailsActivity.this,true);
//
//                watchStatusTv.setText(getString(R.string.watching_on) + " " + getString(R.string.app_name));
//                watchLiveTv.setVisibility(GONE);
//
//                timeTv.setText(currentProgramTime);
//                programTv.setText(currentProgramTitle);
//            }
//        });

//        shareIv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Tools.share(DetailsActivity.this, title);
//            }
//        });
//
//        shareIv2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (title == null) {
//                    new ToastMsg(DetailsActivity.this).toastIconError("Title should not be empty.");
//                    return;
//                }
//                Tools.share(DetailsActivity.this, title);
//            }
//        });

//        castPlayer.addListener(new Player.DefaultEventListener() {
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//
//                if (playWhenReady && playbackState == CastPlayer.STATE_READY) {
//                    progressBar.setVisibility(View.GONE);
//
//                    //Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
//
//                } else if (playbackState == CastPlayer.STATE_READY) {
//                    progressBar.setVisibility(View.GONE);
//                    //Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
//                } else if (playbackState == CastPlayer.STATE_BUFFERING) {
//                    progressBar.setVisibility(VISIBLE);
//
//                    //Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
//                } else {
//                    //Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
//                }
//
//            }
//        });

//        serverIv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openServerDialog();
//            }
//        });

        simpleExoPlayerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == 0) {
                    imgBack.setVisibility(VISIBLE);

//                    if (type.equals("tv") || type.equals("tvseries")) {
//                        imgFull.setVisibility(VISIBLE);
//                    } else {
//                        imgFull.setVisibility(GONE);
//                    }

//                    if (type.equals("tv") || type.equals("tvseries")) {
//                        imgFull.setVisibility(VISIBLE);
//                    } else {
                    //imgFull.setVisibility(VISIBLE);
                    //}

                    // invisible download icon for live tv
//                    if (!TextUtils.isEmpty(download_check) && download_check.equals("1")) {
//                        if (!tv) {
//                            if (activeMovie) {
//                                serverIv.setVisibility(VISIBLE);
//                            }
//                        } else {
//                        }
//                    } else {
//                        //
//                    }

                    if (listSub.size() != 0) {
                        imgSubtitle.setVisibility(VISIBLE);
                    }
                    //visible top name
                    //cloneFullName.setVisibility(GONE);
                    //imgSubtitle.setVisibility(VISIBLE);
                } else {
                    //imgBack.setVisibility(GONE);
                    //imgFull.setVisibility(GONE);

                    // imgSubtitle.setVisibility(GONE);

                    volumnControlLayout.setVisibility(GONE);
                    //cloneFullName.setVisibility(GONE);
                }
            }
        });

        subscribeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlanActivity();
            }
        });
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MobileAds.initialize(DetailsActivity.this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    private void playTrailers(){
        if(type.equalsIgnoreCase("tvseries")){
            if (!listServer.isEmpty()) {
                cloneFullName.setVisibility(GONE);
                playMovieTrailer();
            }else{
                //   new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_trailer_found));
            }
        }else{
            if (!trailerServer.isEmpty()) {
                cloneFullName.setVisibility(GONE);
                playMovieTrailer();
            }else{
                // new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_trailer_found));
            }
        }
    }

    private void startPlanActivity(){
        if (userId == null) {
            new ToastMsg(DetailsActivity.this).toastIconError(getResources().getString(R.string.subscribe_error));
            startActivity(new Intent(DetailsActivity.this, GetStarted.class));
            finish();
        } else {
            new ToastMsg(DetailsActivity.this).toastIconInfo("Please, Subscribe to watch full content");
            startActivity(new Intent(DetailsActivity.this, PurchasePlanActivity.class));
            finish();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setPlayerNormalScreen() {
        swipeRefreshLayout.setVisibility(VISIBLE);
        lPlay.setVisibility(GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //close embed link playing
        if (webView.getVisibility() == VISIBLE){
            if (webView != null){
                Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                intent.putExtra("vType",type);
                intent.putExtra("id",id);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
        if (isVideo) {
            lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));

        } else {
            lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));
        }
    }

    private void playMovieTrailer(){
        releasePlayer();
        //resetCastPlayer();
        if(type.equalsIgnoreCase("tvseries")){
            if(listServer.size() == 1){
                episodeID = listServer.get(0).getId();
                preparePlayer(listServer.get(0));
                descriptionLayout.setVisibility(GONE);
                lPlay.setVisibility(VISIBLE);
                cloneLayout.setVisibility(VISIBLE);
            }else{
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_trailer_found));
            }
        }else{
            if(trailerServer.size() == 1){
                episodeID = trailerServer.get(0).getId();
                preparePlayer(trailerServer.get(0));
                descriptionLayout.setVisibility(GONE);
                lPlay.setVisibility(VISIBLE);
                cloneLayout.setVisibility(VISIBLE);
            }else{
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_trailer_found));
            }
        }

    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setPlayerFullScreen() {
        swipeRefreshLayout.setVisibility(GONE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (isVideo) {
            lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        } else {
            lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        }
    }

    private void openDownloadServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_download_server_dialog, null);
        internalDownloadLayout = view.findViewById(R.id.internal_download_layout);
        externalDownloadLayout = view.findViewById(R.id.external_download_layout);
        if (listExternalDownload.isEmpty()) {
            externalDownloadLayout.setVisibility(GONE);
        }
        if (listInternalDownload.isEmpty()) {
            internalDownloadLayout.setVisibility(GONE);
        }
        internalServerRv = view.findViewById(R.id.internal_download_rv);
        externalServerRv = view.findViewById(R.id.external_download_rv);
        internalDownloadAdapter = new DownloadAdapter(this, listInternalDownload, true);
        internalServerRv.setLayoutManager(new LinearLayoutManager(this));
        //internalServerRv.setHasFixedSize(true);
        internalServerRv.setAdapter(internalDownloadAdapter);

        externalDownloadAdapter = new DownloadAdapter(this, listExternalDownload, true);
        externalServerRv.setLayoutManager(new LinearLayoutManager(this));
        externalServerRv.setHasFixedSize(true);
        externalServerRv.setAdapter(externalDownloadAdapter);

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

//    private void openServerDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View view = LayoutInflater.from(this).inflate(R.layout.layout_server_dialog, null);
//        serverRv = view.findViewById(R.id.serverRv);
//        serverAdapter = new ServerAdapter(this, listServer, "movie");
//        serverRv.setLayoutManager(new LinearLayoutManager(this));
//        serverRv.setHasFixedSize(true);
//        serverRv.setAdapter(serverAdapter);
//
//        ImageView closeIv = view.findViewById(R.id.close_iv);
//        builder.setView(view);
//
//        final AlertDialog dialog = builder.create();
//        dialog.show();
//
//        closeIv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
//        serverAdapter.setOnItemClickListener(new ServerAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, CommonModels obj, int position, ServerAdapter.OriginalViewHolder holder) {
//                releasePlayer();
//                //resetCastPlayer();
//                skipTimer = obj.getSkipIntro();
//                free_time = obj.getFree_time();
//                Log.e(TAG, "onItemClick: "+skipTimer);
//                preparePlayer(obj);
//                descriptionLayout.setVisibility(GONE);
//                lPlay.setVisibility(VISIBLE);
//                dialog.dismiss();
//
//                //releasePlayer();
//                //resetCastPlayer();
//                //preparePlayer(obj);
//
//                serverAdapter.chanColor(viewHolder[0], position);
//                holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
//                viewHolder[0] = holder;
//            }
//
//            @Override
//            public void getFirstUrl(String url) {
//                mediaUrl = url;
//            }
//
//            @Override
//            public void hideDescriptionLayout() {
//                descriptionLayout.setVisibility(GONE);
//                lPlay.setVisibility(VISIBLE);
//                dialog.dismiss();
//
//            }
//        });
//
//    }

    public void preparePlayer(CommonModels obj){
        //setPlayerFullScreen();
        mediaUrl = obj.getStremURL();
        skipTimer = obj.getSkipIntro();
        free_time = obj.getFree_time();
        adsUrl = obj.getAdsUrl();
        //Log.e(TAG, "preparePlayer: "+skipTimer);

        //if (!castSession) {
        initMoviePlayer(obj.getStremURL(), obj.getServerType(), DetailsActivity.this, true);

        listSub.clear();
        if (obj.getListSub() != null) {
            listSub.addAll(obj.getListSub());
        }

        if (listSub.size() != 0) {
            imgSubtitle.setVisibility(VISIBLE);
        }else {
            // imgSubtitle.setVisibility(GONE);
        }

//        } else {
//            if (obj.getServerType().toLowerCase().equals("embed")) {
//
//                castSession = false;
//                castPlayer.setSessionAvailabilityListener(null);
//                castPlayer.release();
//
//                // invisible control ui of exoplayer
//                player.setPlayWhenReady(true);
//                simpleExoPlayerView.setUseController(true);
//
//                // invisible control ui of casting
//                castControlView.setVisibility(GONE);
//                chromeCastTv.setVisibility(GONE);
//
//
//            } else {
//                showQueuePopup(DetailsActivity.this, null, getMediaInfo());
//            }
//        }
    }

    void clear_previous() {
        backClickedCount = -1;
        if(nextEpiModel != null && episodeAdapter != null){
            episodeAdapter.changeHolder(-1);
        }
        releasePlayer();
        strCast = "";
        strDirector = "";
        strGenre = "";
        listSub.clear();
        trailerServer.clear();
        listServer.clear();
        listRelated.clear();
        listComment.clear();
        listDownload.clear();
        listInternalDownload.clear();
        listExternalDownload.clear();
        programs.clear();
        castCrews.clear();
        cloneLayout.setVisibility(GONE);
        skipIntroBtn.setVisibility(GONE);
        lPlay.setVisibility(GONE);
        cloneFullName.setVisibility(GONE);
        //aspectRatioIv.setVisibility(GONE);
        descriptionLayout.setVisibility(VISIBLE);
        swipeRefreshLayout.setVisibility(VISIBLE);
    }


    public void showSubtitleDialog(Context context, List<SubtitleModel> list) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_subtitle, viewGroup, false);
        ImageView cancel = dialogView.findViewById(R.id.cancel);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        SubtitleAdapter adapter = new SubtitleAdapter(context, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        alertDialog = builder.create();
        alertDialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

    }

    public void showAudioTrackDialog(Context context, List<SubtitleModel> list) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_subtitle, viewGroup, false);
        ImageView cancel = dialogView.findViewById(R.id.cancel);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        SubtitleAdapter adapter = new SubtitleAdapter(context, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        alertDialog = builder.create();
        alertDialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

    }

    @Override
    public void onCastSessionAvailable() {
//        castSession = true;
//
//        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
//        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
//        //movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
//        movieMetadata.addImage(new WebImage(Uri.parse(castImageUrl)));
//        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl)
//                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//                .setContentType(MimeTypes.VIDEO_UNKNOWN)
//                .setMetadata(movieMetadata).build();
//
//        //array of media sources
//        final MediaQueueItem[] mediaItems = {new MediaQueueItem.Builder(mediaInfo).build()};
//
//        castPlayer.loadItems(mediaItems, 0, 3000, Player.REPEAT_MODE_OFF);
//
//        // visible control ui of casting
//        castControlView.setVisibility(VISIBLE);
//        castControlView.setPlayer(castPlayer);
//        castControlView.addVisibilityListener(new PlayerControlView.VisibilityListener() {
//            @Override
//            public void onVisibilityChange(int visibility) {
//                if (visibility == GONE) {
//                    castControlView.setVisibility(VISIBLE);
//                    chromeCastTv.setVisibility(VISIBLE);
//                }
//            }
//        });
//        castControlView.setVisibilityListener(new PlaybackControlView.VisibilityListener() {
//            @Override
//            public void onVisibilityChange(int visibility) {
//                if (visibility == GONE) {
//                    castControlView.setVisibility(VISIBLE);
//                    chromeCastTv.setVisibility(VISIBLE);
//                }
//            }
//        });

        // invisible control ui of exoplayer
        //player.setPlayWhenReady(false);
        //simpleExoPlayerView.setUseController(false);
    }

    @Override
    public void onCastSessionUnavailable() {
        // make cast session false
//        castSession = false;
//        // invisible control ui of exoplayer
//        player.setPlayWhenReady(true);
//        simpleExoPlayerView.setUseController(true);
//
//        // invisible control ui of casting
//        castControlView.setVisibility(GONE);
//        chromeCastTv.setVisibility(GONE);
    }

    public void initServerTypeForTv(String serverType) {
        this.serverType = serverType;
    }

    @Override
    public void onProgramClick(Program program) {
        if (program.getProgramStatus().equals("onaired")) {
            showExoControlForTv();
            initMoviePlayer(program.getVideoUrl(), "tv", this,true);
            timeTv.setText(program.getTime());
            programTv.setText(program.getTitle());
        } else {
            new ToastMsg(DetailsActivity.this).toastIconError("Not Yet");
        }
    }


    public boolean checkActivePlan(){
        if(TextUtils.isEmpty(paid)){
            return false;
        }
        if(paid.equalsIgnoreCase("1")){
            ActiveStatus activeStatus = PreferenceUtils.getStatus(DetailsActivity.this);
            if(activeStatus == null){
                return false;
            }else if(!TextUtils.isEmpty(activeStatus.getStatus()) && activeStatus.getStatus().equalsIgnoreCase("active") && activeStatus.getPackageTitle().equalsIgnoreCase("Trial")){
                return false;
            }else if(!TextUtils.isEmpty(activeStatus.getStatus()) && activeStatus.getStatus().equalsIgnoreCase("active")){
                if(Tools.compareDates(PreferenceUtils.getCurrentDate(), activeStatus.getExpireDate())){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }else{
            return true;
        }
    }

    @Override
    public void onEpisodeItemClickTvSeries(String type, View view, EpiModel obj, int position, EpisodeAdapter.OriginalViewHolder holder,EpiModel nextObj, int newpos) {
        episodesplay(true, type, obj);
    }

    private void episodesplay(boolean release, String type, EpiModel obj){
        if(obj != null){
            if(release){
                releasePlayer();
            }
            id = obj.getId();
            paid = obj.getPaid();
            skipTimer = obj.getSkipIntro();
            free_time = obj.getFree_time();
            cloneLayout.setVisibility(VISIBLE);
            episodeID = obj.getId();
            adsUrl = obj.getAdsUrl();
            CommonModels model = new CommonModels();
            model.setStremURL(obj.getStreamURL());
            model.setServerType(obj.getServerType());
            model.setSkipIntro(obj.getSkipIntro());
            listSub.clear();
            if(obj.getSubtitleList() != null && obj.getSubtitleList().size() > 0){
                model.setListSub(obj.getSubtitleList());
                listSub.addAll(obj.getSubtitleList());
            }else{
                model.setListSub(Collections.emptyList());
            }
            //free time based play
            if(free_time.equalsIgnoreCase("0")){
                if(checkActivePlan()){
                    if (type.equalsIgnoreCase("embed")){
                        preparePlayer(model);
                    }else {
                        initMoviePlayer(obj.getStreamURL(), obj.getServerType(), DetailsActivity.this, !isFullScr);
                    }
                }else{
                    startPlanActivity();
                }
            }else{
                if (type.equalsIgnoreCase("embed")){
                    preparePlayer(model);
                }else {
                    initMoviePlayer(obj.getStreamURL(), obj.getServerType(), DetailsActivity.this, !isFullScr);
                }
            }

//            if (type.equalsIgnoreCase("embed")){
//                CommonModels model = new CommonModels();
//                model.setStremURL(obj.getStreamURL());
//                model.setServerType(obj.getServerType());
//                model.setSkipIntro(obj.getSkipIntro());
//                if(obj.getSubtitleList() != null && obj.getSubtitleList().size() > 0){
//                    model.setListSub(obj.getSubtitleList());
//                }else{
//                    model.setListSub(Collections.emptyList());
//                }
//                releasePlayer();
//                preparePlayer(model);
//            }else {
//              initMoviePlayer(obj.getStreamURL(), obj.getServerType(), DetailsActivity.this, !isFullScr);
//            }
        }
    }

    @Override
    public void run() {
        if(player != null && isPlaying && player.isPlaying()){
            PreferenceUtils.addWatchHistory(DetailsActivity.this, ogID, player.getCurrentPosition(),type, episodeID, IMAGE_THUMB,false);
        }
        saveCurrentPosition();
    }

    private class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.OriginalViewHolder> {
        private List<SubtitleModel> items = new ArrayList<>();
        private Context ctx;

        public SubtitleAdapter(Context context, List<SubtitleModel> items) {
            this.items = items;
            ctx = context;
        }

        @Override
        public SubtitleAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SubtitleAdapter.OriginalViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_subtitle, parent, false);
            vh = new SubtitleAdapter.OriginalViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(SubtitleAdapter.OriginalViewHolder holder, final int position) {
            final SubtitleModel obj = items.get(position);
            holder.name.setText(obj.getLanguage());

            holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedSubtitle(mediaSource, obj.getUrl(), ctx);
                    alertDialog.cancel();
                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class OriginalViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            private View lyt_parent;

            public OriginalViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.name);
                lyt_parent = v.findViewById(R.id.lyt_parent);
            }
        }

    }


    private void initGetData() {
        strGenre = "";
        if (!type.equals("tv")) {

            //----related rv----------
            relatedAdapter = new HomePageAdapter(this, listRelated);
            rvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                    false));
            rvRelated.setHasFixedSize(true);
            rvRelated.setAdapter(relatedAdapter);

            if (type.equals("tvseries")) {

                seasonSpinnerContainer.setVisibility(GONE);

                episodeLayout.setVisibility(GONE);

                rvServer.setVisibility(VISIBLE);
                serverIv.setVisibility(GONE);

                rvRelated.removeAllViews();
                listRelated.clear();
                rvServer.removeAllViews();
                listServer.clear();

                downloadBt.setVisibility(GONE);
                watchNowBt.setVisibility(VISIBLE);
                trailerwatch.setVisibility(VISIBLE);
                playTrailers();

                castCrewAdapter = new CastCrewAdapter(this, castCrews);
                castRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                castRv.setHasFixedSize(true);
                castRv.setAdapter(castCrewAdapter);

                getSeriesData(type, id);

//                if (listSub.size() == 0) {
//                    imgSubtitle.setVisibility(GONE);
//                }

            } else {
                // imgFull.setVisibility(GONE);
                listServer.clear();
                rvRelated.removeAllViews();
                listRelated.clear();

//                if (listSub.size() == 0) {
//                    imgSubtitle.setVisibility(GONE);
//                }
                castCrewAdapter = new CastCrewAdapter(this, castCrews);
                castRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                castRv.setHasFixedSize(true);
                castRv.setAdapter(castCrewAdapter);
                getMovieData(type, id);

                //final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
            }

        }
//        else {
//            tv = true;
//            imgSubtitle.setVisibility(GONE);
//            llcomment.setVisibility(GONE);
//            serverIv.setVisibility(GONE);
//
//            rvServer.setVisibility(VISIBLE);
//            descriptionLayout.setVisibility(GONE);
//            lPlay.setVisibility(VISIBLE);
//
//            // hide exo player some control
//            hideExoControlForTv();
//
//            tvLayout.setVisibility(VISIBLE);
//
//            // hide program guide if its disable from api
//            if (!PreferenceUtils.isProgramGuideEnabled(DetailsActivity.this)) {
//                proGuideTv.setVisibility(GONE);
//                programRv.setVisibility(GONE);
//
//            }
//
//            watchStatusTv.setText(getString(R.string.watching_on) + " " + getString(R.string.app_name));
//
//            tvRelated.setText(getString(R.string.all_tv_channel));
//
//            rvServer.removeAllViews();
//            listServer.clear();
//            rvRelated.removeAllViews();
//            listRelated.clear();
//
//            programAdapter = new ProgramAdapter(programs, this);
//            programAdapter.setOnProgramClickListener(this);
//            programRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//            programRv.setHasFixedSize(true);
//            programRv.setAdapter(programAdapter);
//
//            imgAddFav.setVisibility(GONE);
//
//            serverAdapter = new ServerAdapter(this, listServer, "tv");
//            rvServer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//            rvServer.setHasFixedSize(true);
//            rvServer.setAdapter(serverAdapter);
//            llBottom.setVisibility(GONE);
//
//            final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
//            serverAdapter.setOnItemClickListener(new ServerAdapter.OnItemClickListener() {
//                @Override
//                public void onItemClick(View view, CommonModels obj, int position, ServerAdapter.OriginalViewHolder holder) {
//                    mediaUrl = obj.getStremURL();
//
//
//                    //if (!castSession) {
//                    initMoviePlayer(obj.getStremURL(), obj.getServerType(), DetailsActivity.this, true);
//
////                    } else {
////
////                        if (obj.getServerType().toLowerCase().equals("embed")) {
////
////                            castSession = false;
////                            castPlayer.setSessionAvailabilityListener(null);
////                            castPlayer.release();
////
////                            // invisible control ui of exoplayer
////                            player.setPlayWhenReady(true);
////                            simpleExoPlayerView.setUseController(true);
////
////                            // invisible control ui of casting
////                            castControlView.setVisibility(GONE);
////                            chromeCastTv.setVisibility(GONE);
////                        } else {
////                          //  showQueuePopup(DetailsActivity.this, null, getMediaInfo());
////                        }
////                    }
//
//                    serverAdapter.chanColor(viewHolder[0], position);
//                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
//                    viewHolder[0] = holder;
//                }
//
//                @Override
//                public void getFirstUrl(String url) {
//                    mediaUrl = url;
//                }
//
//                @Override
//                public void hideDescriptionLayout() {
//
//                }
//            });
//
//
//        }
    }

    private void openWebActivity(String s, Context context, String videoType) {

        if (isPlaying) {
            player.release();
        }
        progressBar.setVisibility(GONE);
        playerLayout.setVisibility(GONE);

        webView.loadUrl(s);
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setVisibility(VISIBLE);

    }

    public void initMoviePlayer(String url, String servertype, Context context, boolean enablefull) {
        if(enablefull){
            isFullScr = true;
            controlFullScreenPlayer();
        }
        urlType = servertype;
        if (servertype.equals("embed") || servertype.equals("vimeo") || servertype.equals("gdrive") || servertype.equals("youtube-live")) {
            isVideo = false;
            activeMovie = true;
            openWebActivity(url, context, servertype);
        } else {
            isVideo = true;
            initVideoPlayer(url, context, servertype);
        }
        //PreferenceUtils.insertRecent(DetailsActivity.this, IMAGE_THUMB,type, ogID);
        PreferenceUtils.addWatchHistory(DetailsActivity.this, ogID, 0,type, episodeID, IMAGE_THUMB,true);
    }

    public void initVideoPlayer(String url, Context context, String type) {

//        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
//
//        DefaultTrackSelector trackSelector = new
//                DefaultTrackSelector(videoTrackSelectionFactory);

        //DefaultAllocator allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
        //DefaultLoadControl loadControl = new DefaultLoadControl(allocator, 360000, 600000, 2500, 5000, -1, true);

        //CacheDataSourceFactory dataSourceFactory = new CacheDataSourceFactory(context, defaultBandwidthMeter, -1, -1);

        //TrackSelector trackSelectorDef = new DefaultTrackSelector(parametersBuilder.build(),factory);

        //DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(this);

//        ExoPlayer.Builder builder = new ExoPlayer.Builder(this, factory);
//        builder.setBandwidthMeter(defaultBandwidthMeter)
//                .setTrackSelector(selector)
//                .setUseLazyPreparation(true);
//        FrameworkMediaDrm frameworkMediaDrm = null;
//        try {
//            frameworkMediaDrm = FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID);
//        } catch (UnsupportedDrmException e) {
//            e.printStackTrace();
//        }

//        HttpMediaDrmCallback mediaDrmCallback = new HttpMediaDrmCallback("https://configpingpongkeys.s3.ap-south-1.amazonaws.com/keyaes.key", new DefaultHttpDataSourceFactory("Pingpong"));
//        mediaDrmCallback.setKeyRequestProperty("EXT-X-KEY","METHOD=AES-128,URI=\"data:text/plain;charset=utf-8,50696e67506f6e67456e746572524b53");
//        DefaultDrmSessionManager<FrameworkMediaCrypto> defaultDrmSessionManager = new DefaultDrmSessionManager<>(C.WIDEVINE_UUID,frameworkMediaDrm,mediaDrmCallback,null);

        //player = ExoPlayerFactory.newSimpleInstance(context,factory, trackSelector,loadControl, null, defaultBandwidthMeter);
        //player.setPlayWhenReady(true);
        //simpleExoPlayerView.setPlayer(player);

        progressBar.setVisibility(VISIBLE);
        if (player != null){
            player.stop();
            player.release();
        }

        webView.setVisibility(GONE);
        playerLayout.setVisibility(VISIBLE);

        DefaultTrackSelector.ParametersBuilder parametersBuilder = new DefaultTrackSelector.ParametersBuilder();
        int speedCheck = Connectivity.isConnectedFast(this);
        int HI_BITRATE = 2097152;
        int MI_BITRATE = 1048576;
        int LO_BITRATE = 524288;
        //parametersBuilder.setExceedVideoConstraintsIfNecessary(true);
        //parametersBuilder.setForceHighestSupportedBitrate(true);
        if(speedCheck == 0){
            parametersBuilder.setMaxVideoBitrate(LO_BITRATE);
            parametersBuilder.setMaxVideoSize(852, 480);
        }else if(speedCheck == 1){
            parametersBuilder.setMaxVideoBitrate(MI_BITRATE);
            parametersBuilder.setMaxVideoSize(1280, 720);
        }else if(speedCheck == 2){
            parametersBuilder.setMaxVideoBitrate(LO_BITRATE);
            parametersBuilder.setMaxVideoSize(1280, 720);
        }else if(speedCheck == 3){
            parametersBuilder.setMaxVideoBitrate(LO_BITRATE);
            parametersBuilder.setMaxVideoSize(480, 360);
        }

        DefaultTrackSelector selector = new DefaultTrackSelector(this);
        selector.setParameters(parametersBuilder.build());


        LoadControl loadControl = new DefaultLoadControl();

        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder(context)
                .build();

        RenderersFactory factory = new DefaultRenderersFactory(this);


        // player = ExoPlayerFactory.newSimpleInstance(context,factory, selector,loadControl, null, defaultBandwidthMeter);

        DataSource.Factory dataSourceFactory = null;

        //Uri uri = Uri.parse("https://optimizedhlsvideos.s3.ap-south-1.amazonaws.com/Nasha/s1ep6drm/NASHA+-+Ep+6.m3u8");
        Uri uri = Uri.parse(url);

        if (type.equals("hls") || url.contains("m3u8")) {
            dataSourceFactory = defaultFactory(this);
            mediaSource = returnMediasource(dataSourceFactory, uri, context, 1);
        } else if (type.equals("youtube")) {
            extractYoutubeUrl(url, context, 18);
        } else if (type.equals("youtube-live")) {
            extractYoutubeUrl(url, context, 133);
        } else if (type.equals("rtmp")) {
            dataSourceFactory = new RtmpDataSourceFactory();
            mediaSource = returnMediasource(dataSourceFactory, uri, context, 0);
        }else{
            dataSourceFactory = defaultFactory(this);
            mediaSource = returnMediasource(dataSourceFactory, uri, context, -1);
        }

        player = new SimpleExoPlayer.Builder(this,factory)
                .setBandwidthMeter(defaultBandwidthMeter)
                .setTrackSelector(selector)
                .setLoadControl(loadControl)
                .build();

//        MediaItem.Builder builder = new MediaItem.Builder()
//                .setAdTagUri(getString(R.string.ad_tag_url))
//                .setUri(uri)
//                .setMimeType(MimeTypes.APPLICATION_M3U8)
//                .setDrmLicenseUri("https://configpingpongkeys.s3.ap-south-1.amazonaws.com/keyaes.key")
//                .setDrmUuid(C.WIDEVINE_UUID);
//
//        mediaItemsList.add(builder.build());

        //Log.e(TAG, "adsUrl: "+adsUrl);

        player.setMediaSource(mediaSource, true);

//        if(checkActivePlan()){
//            player.setMediaSource(mediaSource, true);
//        }else{
////            long minutes = C.MILLIS_PER_SECOND * 300;
////            AdPlaybackState adPlaybackState = new AdPlaybackState(0, minutes);
////            if(!TextUtils.isEmpty(adsUrl)){
////                String[] splits = adsUrl.split(",");
////                int index = 0, secondindex= 0;
////                for (String ads : splits) {
////                    //adPlaybackState.withAdUri(index,secondindex, Uri.parse(splits[0]));
////                    index++;
////                    secondindex++;
////                }
////            }
//            AdsLoader.AdViewProvider adViewProvider = new AdsLoader.AdViewProvider() {
//                @Override
//                public ViewGroup getAdViewGroup() {
//                    return simpleExoPlayerView.getAdViewGroup();
//                }
//
//                @Override
//                public List<AdsLoader.OverlayInfo> getAdOverlayInfos() {
//                    return simpleExoPlayerView.getAdOverlayInfos();
//                }
//            };

//            MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);
//
//            AdsMediaSource adsMediaSource = new AdsMediaSource(mediaSource,null,mediaSourceFactory,adsLoader, adViewProvider);
//            player.setMediaSource(adsMediaSource, true);
//            adsLoader.start(new AdsLoader.EventListener() {
//                @Override
//                public void onAdPlaybackState(AdPlaybackState adPlaybackState) {
//
//                }
//            }, adViewProvider);
//        }

        player.prepare();

        simpleExoPlayerView.setPlayer(player);

        simpleExoPlayerView.setControllerHideDuringAds(true);

        //adsLoader.setPlayer(player);

        long savedDur = PreferenceUtils.getWatchedDuration(DetailsActivity.this, ogID,cloneType, episodeID);

        player.setPlayWhenReady(true);

        activeMovie = true;

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    isPlaying = true;
                    progressBar.setVisibility(View.GONE);
                    if(firstTimeSkip){
                        player.seekTo(savedDur);
                        firstTimeSkip = false;
                    }
                    saveCurrentPosition();
                    introSkipCallback();
                    freeTimEpCallback();
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    isPlaying = false;
                    saveCurrentPosition();
                    introSkipCallback();
                    freeTimEpCallback();
                } else if (playbackState == Player.STATE_BUFFERING) {
                    isPlaying = false;
                    progressBar.setVisibility(VISIBLE);
                } else if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false;
                    progressBar.setVisibility(VISIBLE);
                    if(cloneType.equalsIgnoreCase("tvseries")){
                        if(episodeAdapter != null){
                            nextEpiModel = episodeAdapter.nextEpModel();
                            if(nextEpiModel != null){
                                episodesplay(false, nextEpiModel.getServerType(), nextEpiModel);
                                episodeAdapter.changeHolder(nextPos);
                            }
                        }
                    }
                } else {
                    isPlaying = false;
                }
            }
        });
//        player.addListener(new Player.DefaultEventListener() {
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                if (playWhenReady && playbackState == Player.STATE_READY) {
//
//                    isPlaying = true;
//                    progressBar.setVisibility(View.GONE);
//                } else if (playbackState == Player.STATE_READY) {
//                    progressBar.setVisibility(View.GONE);
//                    isPlaying = false;
//                } else if (playbackState == Player.STATE_BUFFERING) {
//                    isPlaying = false;
//                    progressBar.setVisibility(VISIBLE);
//                } else {
//                    // player paused in any state
//                    isPlaying = false;
//                }
//            }
//        });

    }

    @SuppressLint("StaticFieldLeak")
    private void extractYoutubeUrl(String url, final Context context, final int tag) {
        new YouTubeExtractor(context) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = tag;
                    String downloadUrl = ytFiles.get(itag).getUrl();
                    youtubeDownloadUr = downloadUrl;
                    try {
                        DataSource.Factory factory = defaultFactory(context);
                        MediaSource mediaSource = returnMediasource(factory, Uri.parse(downloadUrl), context, -1);
                        player.prepare(mediaSource, true, false);
                        if (Config.YOUTUBE_VIDEO_AUTO_PLAY) {
                            player.setPlayWhenReady(true);
                        } else {
                            player.setPlayWhenReady(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }.extract(url, true, true);
    }

    /**
     * skip intro
     */
    private void introSkipCallback(){
        if(player == null || TextUtils.isEmpty(skipTimer) || skipTimer.equalsIgnoreCase("0")){
            return;
        }
        PlayerMessage playerMessage = player.createMessage(new PlayerMessage.Target() {
            @Override
            public void handleMessage(int messageType, @Nullable Object payload) throws ExoPlaybackException {
                skipIntroBtn.setVisibility(VISIBLE);
            }
        });
        long skipPosition = 10000;
        playerMessage
                .setHandler(handler)
                .setPosition(skipPosition)
                .setDeleteAfterDelivery(true)
                .send();
    }

    /**
     * free time then show plan
     */
    private void freeTimEpCallback(){
        if(player == null || TextUtils.isEmpty(free_time) || free_time.equalsIgnoreCase("0")){
            return;
        }
        PlayerMessage playerMessage = player.createMessage(new PlayerMessage.Target() {
            @Override
            public void handleMessage(int messageType, @Nullable Object payload) throws ExoPlaybackException {
                if(checkActivePlan()){

                }else{
                    startPlanActivity();
                }
            }
        });
        long freeTime = Tools.convertTimeToMill(free_time);
        playerMessage
                .setHandler(handler)
                .setPosition(freeTime)
                .setDeleteAfterDelivery(true)
                .send();
    }

    private DataSource.Factory defaultFactory(Context context){
        DefaultBandwidthMeter.Builder builder = new DefaultBandwidthMeter.Builder(this);
        builder.setResetOnNetworkTypeChange(true);
        DefaultBandwidthMeter defaultBandwidthMeter = builder.build();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Pingpong"), defaultBandwidthMeter);
        return dataSourceFactory;
    }

    /**
     * mediaSource
     * @param uri
     * @param context
     * @param type
     * @return
     */
    private MediaSource returnMediasource(DataSource.Factory factory, Uri uri, Context context, int type){
        MediaSource mediaSource = null;
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(uri)
                .setMimeType(type == 1 ? MimeTypes.APPLICATION_M3U8 : MimeTypes.APPLICATION_MP4)
                .setDrmLicenseUri("https://configpingpongkeys.s3.ap-south-1.amazonaws.com/keyaes.key")
                .setDrmUuid(C.WIDEVINE_UUID)
                .build();
        if (type == 0){
            mediaSource = new ProgressiveMediaSource.Factory(factory)
                    .createMediaSource(mediaItem);
        }else if (type == 1){
            mediaSource =  new HlsMediaSource.Factory(new DefaultHlsDataSourceFactory(factory))
                    //.setDrmUserAgent("Mozilla/5.0 (Linux; Android 10; V1936A; wv) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36")
                    .createMediaSource(mediaItem);
        }else {
            mediaSource = new ProgressiveMediaSource.Factory(factory)
                    .createMediaSource(mediaItem);
        }

        return mediaSource;
    }

    public void setSelectedSubtitle(MediaSource mediaSource, String subtitle, Context context) {
        MergingMediaSource mergedSource;
        if (subtitle != null) {
            Uri subtitleUri = Uri.parse(subtitle);

            Format subtitleFormat = Format.createTextSampleFormat(
                    null, // An identifier for the track. May be null.
                    MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                    Format.NO_VALUE, // Selection flags for the track.
                    "en"); // The subtitle language. May be null.

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, getString(R.string.app_name)), new DefaultBandwidthMeter());


            MediaSource subtitleSource = new SingleSampleMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(subtitleUri, subtitleFormat, C.TIME_UNSET);


            mergedSource = new MergingMediaSource(mediaSource, subtitleSource);
            player.prepare(mergedSource, false, false);
            player.setPlayWhenReady(true);
        } else {
            Toast.makeText(context, "No Subtitle Available...", Toast.LENGTH_SHORT).show();
        }
    }

    private void bannerAds(String paid){
        if(TextUtils.isEmpty(paid)){
            return;
        }
        if(paid.equalsIgnoreCase("0")){
            AdView adView = new AdView(DetailsActivity.this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getString(R.string.admob_banner_id));
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
            adsView.setVisibility(VISIBLE);
            adsView.addView(adView);
        }else{
            adsView.setVisibility(GONE);
        }
    }

    private void getSeriesData(String vtype, String vId) {
        final List<String> seasonList = new ArrayList<>();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SingleDetailsApi api = retrofit.create(SingleDetailsApi.class);
        Call<SingleDetails> call = api.getSingleDetails(Config.API_KEY, vtype, vId);
        call.enqueue(new Callback<SingleDetails>() {
            @Override
            public void onResponse(Call<SingleDetails> call, retrofit2.Response<SingleDetails> response) {
                if (response.code() == 200){
                    swipeRefreshLayout.setRefreshing(false);

                    SingleDetails singleDetails = response.body();
                    paid = singleDetails.getIsPaid();
                    //show ads
                    bannerAds(paid);

                    title = singleDetails.getTitle();
                    sereisTitleTv.setText(title);
                    sereisTitleTv.setVisibility(GONE);
                    cloneName.setText(title);
                    cloneFullName.setText(title);
                    castImageUrl = singleDetails.getThumbnailUrl();
                    seriesTitle = title;
                    tvName.setText(title);
                    tvRelease.setText("Released On " + singleDetails.getRelease());
                    tvDes.setText(singleDetails.getDescription());
                    Img_URL = singleDetails.getPosterUrl();
                    IMAGE_THUMB = singleDetails.getThumbnailUrl();
                    if (!isFinishing() && !isDestroyed()) {
                        GlideApp.with(DetailsActivity.this).load(singleDetails.getPosterUrl()).into(posterIv);
                    }

//                    Glide
//                            .with(DetailsActivity.this)
//                            .load(singleDetails.getPosterUrl())
//                            .apply(new RequestOptions()
//                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                    .format(DecodeFormat.PREFER_ARGB_8888)
//                            .placeholder(R.drawable.album_art_placeholder_large))
//                            .into(posterIv);
//                    Picasso.get().load(singleDetails.getPosterUrl()).placeholder(R.drawable.album_art_placeholder_large)
//                            .into(posterIv);
                    if (!isFinishing() && !isDestroyed()) {
                        GlideApp.with(DetailsActivity.this).load(singleDetails.getThumbnailUrl()).into(thumbIv);
                    }
//                    Picasso.get().load(singleDetails.getThumbnailUrl()).placeholder(R.drawable.poster_placeholder)
//                            .into(thumbIv);

                    download_check = singleDetails.getEnableDownload();

                    //----director---------------
                    if(singleDetails.getDirector() != null && singleDetails.getDirector().size() > 0){
                        for (int i = 0; i < singleDetails.getDirector().size(); i++) {
                            Director director = singleDetails.getDirector().get(i);
                            if (i == singleDetails.getDirector().size() - 1) {
                                strDirector = strDirector + director.getName();
                            } else {
                                strDirector = strDirector + director.getName() + ", ";
                            }
                        }
                        tvDirector.setText(strDirector);
                    }

                    //----cast---------------
                    if(singleDetails.getCast() != null && singleDetails.getCast().size() > 0) {
                        for (int i = 0; i < singleDetails.getCast().size(); i++) {
                            Cast cast = singleDetails.getCast().get(i);

                            CastCrew castCrew = new CastCrew();
                            castCrew.setId(cast.getStarId());
                            castCrew.setName(cast.getName());
                            castCrew.setUrl(cast.getUrl());
                            castCrew.setImageUrl(cast.getImageUrl());
                            castCrews.add(castCrew);
                        }
                    }
                    if(castCrews.size() > 0){
                        castCrewAdapter.notifyDataSetChanged();
                        tvCast.setVisibility(VISIBLE);
                    }else{
                        tvCast.setVisibility(GONE);
                        castRv.setVisibility(GONE);
                    }
                    //---genre---------------
                    if(singleDetails.getGenre() != null && singleDetails.getGenre().size() > 0) {

                        for (int i = 0; i < singleDetails.getGenre().size(); i++) {
                            Genre genre = singleDetails.getGenre().get(i);
                            if (i == singleDetails.getCast().size() - 1) {
                                strGenre = strGenre + genre.getName();
                            } else {
                                if (i == singleDetails.getGenre().size() - 1) {
                                    strGenre = strGenre + genre.getName();
                                } else {
                                    strGenre = strGenre + genre.getName() + "  |  ";
                                }
                            }
                        }
                    }
                    setGenreText();

                    //----related tv series---------------
                    if(singleDetails.getRelatedTvseries() != null && singleDetails.getRelatedTvseries().size() > 0) {

                        for (int i = 0; i < singleDetails.getRelatedTvseries().size(); i++) {
                            RelatedMovie relatedTvSeries = singleDetails.getRelatedTvseries().get(i);

                            CommonModels models = new CommonModels();
                            models.setTitle(relatedTvSeries.getTitle());
                            models.setImageUrl(relatedTvSeries.getThumbnailUrl());
                            models.setId(relatedTvSeries.getVideosId());
                            models.setVideoType("tvseries");
                            models.setIsPaid(relatedTvSeries.getIsPaid());
                            listRelated.add(models);
                        }
                    }
                    if (listRelated.size() == 0) {
                        tvRelated.setVisibility(GONE);
                    }
                    relatedAdapter.notifyDataSetChanged();

                    listServer.clear();

                    //----seasson------------
                    if(singleDetails.getSeason() != null && singleDetails.getSeason().size() > 0) {

                        for (int i = 0; i < singleDetails.getSeason().size(); i++) {
                            Season season = singleDetails.getSeason().get(i);

                            CommonModels models = new CommonModels();
                            String season_name = season.getSeasonsName();
                            models.setTitle(season.getSeasonsName());
                            if (i == 0) {
                                if (singleDetails.getSeason().get(0).getEpisodes().size() > 0) {
                                    Episode episode = singleDetails.getSeason().get(0).getEpisodes().get(0);
                                    models.setStremURL(episode.getFileUrl());
                                    models.setImageUrl(episode.getImageUrl());
                                    models.setServerType(episode.getFileType());
                                    models.setSkipIntro(episode.getSkipIntro());
                                    models.setPaid(episode.getPaid());
                                    models.setFree_time(episode.getFree_time());
                                    models.setAdsUrl(episode.getAds());
                                }
                            }
                            seasonList.add("Season: " + season.getSeasonsName());

                            //----episode------
                            List<EpiModel> epList = new ArrayList<>();
                            epList.clear();
                            for (int j = 0; j < singleDetails.getSeason().get(i).getEpisodes().size(); j++) {
                                Episode episode = singleDetails.getSeason().get(i).getEpisodes().get(j);
                                EpiModel model = new EpiModel();
                                model.setSeson(season_name);
                                //Log.e(TAG, "onResponse: "+episode.getFileUrl());
                                model.setEpi(episode.getEpisodesName());
                                model.setStreamURL(episode.getFileUrl());
                                model.setServerType(episode.getFileType());
                                model.setImageUrl(episode.getImageUrl());
                                model.setSubtitleList(episode.getSubtitle());
                                model.setId(episode.getEpisodesId());
                                model.setSkipIntro(episode.getSkipIntro());
                                model.setPaid(episode.getPaid());
                                model.setFree_time(episode.getFree_time());
                                model.setAdsUrl(episode.getAds());
                                if (i == 0 && j == 0) {

                                } else {
                                    epList.add(model);
                                }
                            }
                            models.setListEpi(epList);
                            listServer.add(models);

                            setSeasonData(seasonList);
                        }
                    }

                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(GONE);

                    if(!listServer.isEmpty() && listServer.size() > 0){
                        trailerwatch.setVisibility(VISIBLE);
                        playTrailers();
                    }else{
                        trailerwatch.setVisibility(GONE);
                    }

//                    if(seasonList.size() > 0){
//                        if(listServer.get(0).getListEpi().size() > 0){
//                            seasonSpinnerContainer.setVisibility(VISIBLE);
//                            eptxt.setVisibility(VISIBLE);
//                        }else{
//                            seasonSpinnerContainer.setVisibility(GONE);
//                            eptxt.setVisibility(GONE);
//                        }
//                    }else{
//                        seasonSpinnerContainer.setVisibility(GONE);
//                        eptxt.setVisibility(GONE);
//                    }
                }
            }

            @Override
            public void onFailure(Call<SingleDetails> call, Throwable t) {

            }
        });
    }



    public void setSeasonData(List<String> seasonData) {

        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, seasonData);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        seasonSpinner.setAdapter(aa);

        seasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                rvServer.removeAllViewsInLayout();
                rvServer.setLayoutManager(new LinearLayoutManager(DetailsActivity.this,
                        RecyclerView.HORIZONTAL, false));
                rvServer.setHasFixedSize(true);
                episodeAdapter = new EpisodeAdapter(DetailsActivity.this,
                        listServer.get(i).getListEpi());
                rvServer.setAdapter(episodeAdapter);
                episodeAdapter.setOnEmbedItemClickListener(DetailsActivity.this);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void setGenreText() {
        tvGenre.setText("");
        tvGenre.setText(strGenre);
        dGenryTv.setText("");
        dGenryTv.setText(strGenre);
        dGenryTv1.setText("");
        dGenryTv1.setText(strGenre);
        cloneGenre.setText("");
        cloneGenre.setText(strGenre);
    }

    private void getMovieData(String vtype, String vId) {
        strCast = "";
        strDirector = "";
        strGenre = "";
        trailerServer.clear();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SingleDetailsApi api = retrofit.create(SingleDetailsApi.class);
        Call<SingleDetails> call = api.getSingleDetails(Config.API_KEY, vtype, vId);
        call.enqueue(new Callback<SingleDetails>() {
            @Override
            public void onResponse(Call<SingleDetails> call, retrofit2.Response<SingleDetails> response) {
                if (response.code() == 200){
                    swipeRefreshLayout.setRefreshing(false);

                    SingleDetails singleDetails = response.body();
                    paid = singleDetails.getIsPaid();
                    //show ads
                    bannerAds(paid);

                    download_check = singleDetails.getEnableDownload();
                    castImageUrl = singleDetails.getThumbnailUrl();

                    if (!TextUtils.isEmpty(download_check) && download_check.equals("1")) {
                        downloadBt.setVisibility(VISIBLE);
                    } else {
                        downloadBt.setVisibility(GONE);
                    }

                    title = singleDetails.getTitle();
                    movieTitle = title;

                    tvName.setText(title);
                    cloneName.setText(title);
                    cloneFullName.setText(title);
                    tvRelease.setText("Released On " + singleDetails.getRelease());
                    tvDes.setText(singleDetails.getDescription());
                    Img_URL = singleDetails.getPosterUrl();
                    IMAGE_THUMB = singleDetails.getThumbnailUrl();

                    if (!isFinishing() && !isDestroyed()) {
                        GlideApp.with(DetailsActivity.this).load(singleDetails.getPosterUrl()).into(posterIv);
                    }


//                    Picasso.get().load(singleDetails.getPosterUrl()).placeholder(R.drawable.album_art_placeholder_large)
//                            .into(posterIv);
                    if (!isFinishing() && !isDestroyed()) {
                        GlideApp.with(DetailsActivity.this).load(singleDetails.getThumbnailUrl()).into(thumbIv);
                    }
//                    Picasso.get().load(singleDetails.getThumbnailUrl()).placeholder(R.drawable.poster_placeholder)
//                            .into(thumbIv);

                    //----director---------------
                    if(singleDetails.getDirector() != null && singleDetails.getDirector().size() > 0){
                        for (int i = 0; i < singleDetails.getDirector().size(); i++) {
                            Director director = singleDetails.getDirector().get(i);
                            if (i == singleDetails.getDirector().size() - 1) {
                                strDirector = strDirector + director.getName();
                            } else {
                                strDirector = strDirector + director.getName() + ", ";
                            }
                        }
                        tvDirector.setText(strDirector);
                    }
                    //----cast---------------
                    if(singleDetails.getCast() != null && singleDetails.getCast().size() > 0) {

                        for (int i = 0; i < singleDetails.getCast().size(); i++) {
                            Cast cast = singleDetails.getCast().get(i);

                            CastCrew castCrew = new CastCrew();
                            castCrew.setId(cast.getStarId());
                            castCrew.setName(cast.getName());
                            castCrew.setUrl(cast.getUrl());
                            castCrew.setImageUrl(cast.getImageUrl());

                            castCrews.add(castCrew);

                        }
                    }
                    // cast & crew adapter
                    if(castCrews.size() > 0){
                        castCrewAdapter.notifyDataSetChanged();
                        tvCast.setVisibility(VISIBLE);
                    }else{
                        tvCast.setVisibility(GONE);
                        castRv.setVisibility(GONE);
                    }

                    //---genre---------------
                    if(singleDetails.getGenre() != null && singleDetails.getGenre().size() > 0) {

                        for (int i = 0; i < singleDetails.getGenre().size(); i++) {
                            Genre genre = singleDetails.getGenre().get(i);
                            if (i == singleDetails.getCast().size() - 1) {
                                strGenre = strGenre + genre.getName();
                            } else {
                                if (i == singleDetails.getGenre().size() - 1) {
                                    strGenre = strGenre + genre.getName();
                                } else {
                                    strGenre = strGenre + genre.getName() + "  |  ";
                                }
                            }
                        }
                    }
//                    tvGenre.setText(strGenre);
//                    cloneGenre.setText(strGenre);
//                    dGenryTv.setText(strGenre);
//                    dGenryTv1.setText(strGenre);

                    setGenreText();
                    //-----server----------
                    if(singleDetails.getVideos() != null && singleDetails.getVideos().size() > 0){
                        List<Video> serverList = new ArrayList<>();
                        serverList.addAll(singleDetails.getVideos());

                        for (int i = 0; i < serverList.size(); i++){
                            Video video = serverList.get(i);

                            CommonModels models = new CommonModels();
                            models.setTitle(video.getLabel());
                            models.setStremURL(video.getFileUrl());
                            models.setServerType(video.getFileType());
                            models.setSkipIntro(video.getSkipIntro());
                            models.setFree_time(video.getFree_time());
                            models.setPaid(video.getPaid());
                            models.setAdsUrl(video.getAds());
                            adsUrl = video.getAds();

                            if (video.getFileType().equals("mp4")) {
                                V_URL = video.getFileUrl();
                            }

                            //----subtitle-----------
                            List<Subtitle> subArray = new ArrayList<>();

//                            List<SubtitleModel> list = new ArrayList<>();
//                            SubtitleModel subtitleModel = new SubtitleModel();
//                            subtitleModel.setUrl("http://www.elsubtitle.com/tmp_searcher/The-Avengers_2012_English-ELSUBTITLE.COM-ST_25062135.srt");
//                            subtitleModel.setLanguage("en");
//                            list.add(subtitleModel);
//                            listSub.addAll(list);
//                            models.setListSub(list);

                            subArray.addAll(singleDetails.getVideos().get(i).getSubtitle());
                            if (subArray.size() != 0) {
                                List<SubtitleModel> list = new ArrayList<>();
                                for (int j = 0; j < subArray.size(); j++) {
                                    Subtitle subtitle = subArray.get(j);
                                    SubtitleModel subtitleModel = new SubtitleModel();
                                    subtitleModel.setUrl(subtitle.getUrl());
                                    subtitleModel.setLanguage(subtitle.getLanguage());
                                    list.add(subtitleModel);
                                }
                                listSub.addAll(list);
                                models.setListSub(list);
                            } else {
                                //models.setSubtitleURL(strSubtitle);
                            }

                            listServer.add(models);
                        }

                        if(serverList.size() > 1){
                            Video video = serverList.get(0);

                            CommonModels models = new CommonModels();
                            models.setTitle(video.getLabel());
                            models.setStremURL(video.getFileUrl());
                            models.setServerType(video.getFileType());
                            models.setSkipIntro(video.getSkipIntro());
                            models.setAdsUrl(video.getAds());

                            if (video.getFileType().equals("mp4")) {
                                V_URL = video.getFileUrl();
                            }
                            listServer.remove(0);
                            trailerServer.add(models);
                            if(video.getFileUrl().contains(".mp4") || video.getFileUrl().contains(".m3u8") || video.getFileUrl().contains(".mkv")){
                                trailerwatch.setVisibility(VISIBLE);
                                playTrailers();
                            }else{
                                trailerwatch.setVisibility(GONE);
                            }
                        }else{
                            trailerwatch.setVisibility(GONE);
                        }

//                        if(serverList.size() == 2){
//                            Video video = serverList.get(0);
//
//                            CommonModels models = new CommonModels();
//                            models.setTitle(video.getLabel());
//                            models.setStremURL(video.getFileUrl());
//                            models.setServerType(video.getFileType());
//                            models.setSkipIntro(video.getSkipIntro());
//                            models.setFree_time(video.getFree_time());
//                            models.setPaid(video.getPaid());
//
//                            if (video.getFileType().equals("mp4")) {
//                                V_URL = video.getFileUrl();
//                            }
//                            listServer.remove(0);
//                            trailerServer.add(models);
//                        }else{
//                            trailerwatch.setVisibility(GONE);
//                        }

                        if (serverAdapter != null) {
                            serverAdapter.notifyDataSetChanged();
                        }

                    }

                    //----related post---------------
                    if(singleDetails.getRelatedMovie() != null && singleDetails.getRelatedMovie().size() > 0) {

                        for (int i = 0; i < singleDetails.getRelatedMovie().size(); i++) {
                            RelatedMovie relatedMovie = singleDetails.getRelatedMovie().get(i);
                            CommonModels models = new CommonModels();
                            models.setTitle(relatedMovie.getTitle());
                            models.setImageUrl(relatedMovie.getThumbnailUrl());
                            models.setId(relatedMovie.getVideosId());
                            models.setVideoType("movie");
                            models.setIsPaid(relatedMovie.getIsPaid());
                            //models.setIsPaid(relatedMovie.getIsPaid());
                            listRelated.add(models);
                        }
                    }

                    if (listRelated.size() == 0) {
                        tvRelated.setVisibility(GONE);
                    }
                    relatedAdapter.notifyDataSetChanged();

                    //----download list---------
                    listExternalDownload.clear();
                    listInternalDownload.clear();
                    if(singleDetails.getDownloadLinks() != null && singleDetails.getDownloadLinks().size() > 0) {
                        for (int i = 0; i < singleDetails.getDownloadLinks().size(); i++) {
                            DownloadLink downloadLink = singleDetails.getDownloadLinks().get(i);

                            CommonModels models = new CommonModels();
                            models.setTitle(downloadLink.getLabel());
                            models.setStremURL(downloadLink.getDownloadUrl());
                            models.setFileSize(downloadLink.getFileSize());
                            models.setResulation(downloadLink.getResolution());
                            models.setInAppDownload(downloadLink.isInAppDownload());
                            if (downloadLink.isInAppDownload()) {
                                listInternalDownload.add(models);
                            } else {
                                listExternalDownload.add(models);
                            }
                        }
                    }
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(GONE);
                }else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<SingleDetails> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void hideDescriptionLayout() {
        descriptionLayout.setVisibility(GONE);
        lPlay.setVisibility(VISIBLE);
    }

    public void showSeriesLayout() {
        seriestLayout.setVisibility(VISIBLE);
    }

    public void showDescriptionLayout() {
        descriptionLayout.setVisibility(VISIBLE);
        lPlay.setVisibility(GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausevideoads();
    }

    private void  pausevideoads(){
        if (isPlaying && player != null) {
            isPlaying = false;
            player.setPlayWhenReady(false);
        }
        if(adsLoader != null){
            adsLoader.pauseAd();
        }
    }

    private void videoAdsresume(){
        if (player != null && isPlaying == false) {
            player.setPlayWhenReady(true);
            descriptionLayout.setVisibility(GONE);
            lPlay.setVisibility(VISIBLE);
        }
//        if(adsLoader != null){
//            adsLoader.resumeAd();
//        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        //castManager.removeProgressWatcher(this);
//
//        //Log.e("onStop", "isPlaying: " + isPlaying);
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // resetCastPlayer();
        releasePlayer();
        if(handler != null){
            handler.removeCallbacks(DetailsActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        if(backClickedCount == -1) {
            releasePlayer();
            super.onBackPressed();
        }else if(isFullScr){
            controlFullScreenPlayer();
        }else{
            pausevideoads();
            if(nextEpiModel != null && episodeAdapter != null){
                episodeAdapter.changeHolder(-1);
            }
            cloneLayout.setVisibility(GONE);
            skipIntroBtn.setVisibility(GONE);
            lPlay.setVisibility(GONE);
            cloneFullName.setVisibility(GONE);
            //aspectRatioIv.setVisibility(GONE);
            descriptionLayout.setVisibility(VISIBLE);
            swipeRefreshLayout.setVisibility(VISIBLE);
            backClickedCount = -1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoAdsresume();
    }

    public void releasePlayer() {
        if (player != null) {
            if(adsLoader != null) {
                adsLoader.setPlayer(null);
                adsLoader.release();
            }
            simpleExoPlayerView.setPlayer(null);
            player.setPlayWhenReady(true);
            player.stop();
            player.release();
            player = null;
        }
    }

    public void setMediaUrlForTvSeries(String url, String season, String episod, String skipTimer,String freeTime) {
        mediaUrl = url;
        this.skipTimer = skipTimer;
        this.free_time = freeTime;
        this.season = season;
        this.episod = episod;
    }

    public boolean getCastSession() {
        return castSession;
    }

    public void resetCastPlayer() {
        if (castPlayer != null) {
            castPlayer.setPlayWhenReady(false);
            castPlayer.release();
        }
    }

//    public void showQueuePopup(final Context context, View view, final MediaInfo mediaInfo) {
//        CastSession castSession =
//                CastContext.getSharedInstance(context).getSessionManager().getCurrentCastSession();
//        if (castSession == null || !castSession.isConnected()) {
//            //Log.w(TAG, "showQueuePopup(): not connected to a cast device");
//            return;
//        }
//        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
//        if (remoteMediaClient == null) {
//            //Log.w(TAG, "showQueuePopup(): null RemoteMediaClient");
//            return;
//        }
//        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
//                true).setPreloadTime(PRELOAD_TIME_S).build();
//        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
//        remoteMediaClient.queueLoad(newItemArray, 0,
//                MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
//
//    }

//    public void playNextCast(MediaInfo mediaInfo) {
//
//        //simpleExoPlayerView.setPlayer(castPlayer);
//        simpleExoPlayerView.setUseController(false);
//        castControlView.setVisibility(VISIBLE);
//        castControlView.setPlayer(castPlayer);
//        //simpleExoPlayerView.setDefaultArtwork();
//        castControlView.addVisibilityListener(new PlaybackControlView.VisibilityListener() {
//            @Override
//            public void onVisibilityChange(int visibility) {
//                if (visibility == GONE) {
//                    castControlView.setVisibility(VISIBLE);
//                    chromeCastTv.setVisibility(VISIBLE);
//                }
//            }
//        });
//        CastSession castSession =
//                CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession();
//
//        if (castSession == null || !castSession.isConnected()) {
//            //Log.w(TAG, "showQueuePopup(): not connected to a cast device");
//            return;
//        }
//
//        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
//
//        if (remoteMediaClient == null) {
//            //Log.w(TAG, "showQueuePopup(): null RemoteMediaClient");
//            return;
//        }
//        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
//                true).setPreloadTime(PRELOAD_TIME_S).build();
//        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
//
//        remoteMediaClient.queueLoad(newItemArray, 0,
//                MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
//        castPlayer.setPlayWhenReady(true);
//
//    }

    public MediaInfo getMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        //movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
        movieMetadata.addImage(new WebImage(Uri.parse(castImageUrl)));
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.VIDEO_UNKNOWN)
                .setMetadata(movieMetadata).build();

        return mediaInfo;

    }

    public void downloadVideo(final String url) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        downloadFile(url);
                    }
                };
                handler.post(runnable);

            } else {
                requestPermission(); // Code for permission
            }
        } else {

            // Code for Below 23 API Oriented Device
            // Do next code

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                public void run() {
                    downloadFile(url);
                }
            };
            handler.post(runnable);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new ToastMsg(DetailsActivity.this).toastIconSuccess("Now You can download.");
                    //Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    //Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    public void downloadFile(String url) {
        String fileName = "";
        int notificationId = new Random().nextInt(100 - 1) - 1;
        //Log.d("id:", notificationId + "");

        if (url == null || url.isEmpty()) {
            return;
        }

        if (type.equals("movie")) {
            fileName = tvName.getText().toString();
        } else {
            fileName = seriesTitle + "_" + season + "_" + episod;
        }

        String path = Constants.getDownloadDir(DetailsActivity.this);

        String fileExt = url.substring(url.lastIndexOf('.')); // output like .mkv
        fileName = fileName + fileExt;

        fileName = fileName.replaceAll(" ", "_");
        fileName = fileName.replaceAll(":", "_");

        File file = new File(path, "e_" + fileName); // e_ for encode
        if (file.exists()) {
            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.file_already_downloaded));
            return;
        }

        //download with workManager
        String dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
        Data data = new Data.Builder()
                .putString("url", url)
                .putString("dir", dir)
                .putString("fileName", fileName)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                .setInputData(data)
                .build();

        String workId = request.getId().toString();
        Constants.workId = workId;
        WorkManager.getInstance().enqueue(request);
    }

    public void hideExoControlForTv() {
        exoRewind.setVisibility(GONE);
        exoForward.setVisibility(GONE);
        liveTv.setVisibility(VISIBLE);
        seekbarLayout.setVisibility(GONE);
    }

    public void showExoControlForTv() {
        exoRewind.setVisibility(VISIBLE);
        exoForward.setVisibility(VISIBLE);
        liveTv.setVisibility(GONE);
        seekbarLayout.setVisibility(VISIBLE);
        watchLiveTv.setVisibility(VISIBLE);
        liveTv.setVisibility(GONE);
        watchStatusTv.setText(getResources().getString(R.string.watching_catch_up_tv));
    }

    private void getScreenSize() {
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;
        //Toast.makeText(this, "fjiaf", Toast.LENGTH_SHORT).show();
    }

    public void saveCurrentPosition(){
        if(handler != null){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(player != null && isPlaying && player.isPlaying()){
                        PreferenceUtils.addWatchHistory(DetailsActivity.this, ogID, player.getCurrentPosition(),type, episodeID, IMAGE_THUMB,false);
                    }
                }
            }, 1000);
        }
    }


    /**
     * brightness Dialog
     *
     * @param brightness
     */
    private void showBrightnessDialog(int brightness) {
        if (brightnessDialog == null) {
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View localView = LayoutInflater.from(this).inflate(R.layout.video_brightness,viewGroup, false);
            dialogBrightnessImageView = (ImageView) localView.findViewById(R.id.brightness_image_tip);
            dialogBrightnessProgressText = (TextView) localView.findViewById(R.id.tv_brightness);
            dialogBrightnessProgressBar = (ProgressBar) localView.findViewById(R.id.brightness_progressbar);
            brightnessDialog = createDialogWithView(localView);
        }
        if (!brightnessDialog.isShowing()) {
            brightnessDialog.show();
        }
        brightnessDrawable = (LayerDrawable) ContextCompat.getDrawable(this, R.drawable.volume);
        brightnessDrawable.findDrawableByLayerId(android.R.id.progress).setTint(getResources().getColor(R.color.secondaycolor));
        dialogBrightnessProgressBar.setProgressDrawable(brightnessDrawable);
        if (brightness <= 0) {
            dialogBrightnessImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_brightness_low));
        } else {
            dialogBrightnessImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_brightness_medium));
        }
        if (brightness > 100) {
            brightness = 100;
        } else if (brightness < 0) {
            brightness = 0;
        }
        dialogBrightnessProgressText.setText(brightness + "%");
        dialogBrightnessProgressBar.setProgress(brightness);
    }

    /**
     * dismiss vol dialog
     */
    public void hideVolDialog() {
        if (volumeDialog != null) {
            volumeDialog.dismiss();
        }
    }

    /**
     * dismiss brightness dialog
     */
    public void hideBrightnessDialog() {
        if (brightnessDialog != null) {
            brightnessDialog.dismiss();
        }
    }

    /**
     * create Dialog
     *
     * @param view
     * @return
     */
    public Dialog createDialogWithView(View view) {
        Dialog dialog = new Dialog(this, R.style.dialog_theme);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        window.addFlags(Window.FEATURE_ACTION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        window.setLayout(-2, -2);
        WindowManager.LayoutParams localLayoutParams = window.getAttributes();
        localLayoutParams.gravity = Gravity.CENTER;
        window.setAttributes(localLayoutParams);
        return dialog;
    }

    /**
     * volume Dialog
     *
     * @param volumePercent
     */
    private void volumeDialog(int volumePercent) {
        if (volumeDialog == null) {
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View localView = LayoutInflater.from(this).inflate(R.layout.video_volume, viewGroup, false);
            dialogVolImageView = (ImageView) localView.findViewById(R.id.volume_image_tip);
            dialogVolProgressText = (TextView) localView.findViewById(R.id.tv_volume);
            dialogVolProgressBar = (ProgressBar) localView.findViewById(R.id.volume_progressbar);
            volumeDialog = createDialogWithView(localView);
        }
        if (!volumeDialog.isShowing()) {
            volumeDialog.show();
        }
        volumeDrawable = (LayerDrawable) ContextCompat.getDrawable(this, R.drawable.volume);
        volumeDrawable.findDrawableByLayerId(android.R.id.progress).setTint(getResources().getColor(R.color.secondaycolor));
        dialogVolProgressBar.setProgressDrawable(volumeDrawable);
        if (volumePercent <= 0) {
            dialogVolImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_volume_off));
        } else {
            dialogVolImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_volume_up));
        }
        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }
        dialogVolProgressText.setText(volumePercent + "%");
        dialogVolProgressBar.setProgress(volumePercent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        switch (v.getId()) {
            case R.id.controllerlayout:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isVol = false;
                        isBrightness = false;
                        mDownX = x;
                        mDownY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = x - mDownX;
                        float deltaY = y - mDownY;
                        float absDeltaX = Math.abs(deltaX);
                        float absDeltaY = Math.abs(deltaY);
                        if (!isVol && !isBrightness) {
                            if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                                if (absDeltaX <= THRESHOLD) {
                                    if (mDownX < mScreenWidth / 2 * 0.5f) {
                                        isBrightness = true;
                                        WindowManager.LayoutParams lp = getWindow().getAttributes();
                                        if (lp.screenBrightness < 0) {
                                            try {
                                                mGestureDownBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                            } catch (Settings.SettingNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            mGestureDownBrightness = lp.screenBrightness * 255;
                                        }
                                    } else {
                                        isVol = true;
                                        mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                    }
                                }
                            }
                        }
                        if (isVol) {
                            deltaY = -deltaY;
                            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
                            int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
                            volumeDialog(volumePercent);
                        }
                        if (isBrightness) {
                            deltaY = -deltaY;
                            int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
                            WindowManager.LayoutParams params = getWindow().getAttributes();
                            if (((mGestureDownBrightness + deltaV) / 255) >= 1) {
                                params.screenBrightness = 1;
                            } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                                params.screenBrightness = 0.01f;
                            } else {
                                params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
                            }
                            getWindow().setAttributes(params);
                            int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
                            showBrightnessDialog(brightnessPercent);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mDownX2 = motionEvent.getX();
                        mDownY2 = motionEvent.getY();
                        float deltaXx = mDownX - mDownX2;
                        float deltaYy = mDownY - mDownY2;
                        if (Math.abs(deltaXx) > Math.abs(deltaYy)) {
                            if (Math.abs(deltaXx) > THRESHOLD) {
                                if (deltaXx < 0) {
                                    seekForward();
                                    return true;
                                }
                                if (deltaXx > 0) {
                                    seekBackward();
                                    return true;
                                }
                            }
                        } else {
                            if (Math.abs(deltaYy) > THRESHOLD) {
                                hideSystemUI();
                            }
                        }
                        break;
                }
                break;
        }
        return false;
    }
}
