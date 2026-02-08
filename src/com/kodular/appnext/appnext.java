package com.kodular.appnext;

import android.content.Context;
import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.appnext.ads.fullscreen.FullscreenVideo;
import com.appnext.ads.fullscreen.RewardedVideo;
import com.appnext.ads.interstitial.Interstitial;
import com.appnext.banners.BannerView;
import com.appnext.banners.BannerAdRequest;
import com.appnext.banners.BannerListener;
import com.appnext.core.AppnextAdCreativeType;
import com.appnext.core.AppnextError;
import com.appnext.core.callbacks.*;
import java.util.HashMap;
import java.util.Map;

@DesignerComponent(
    version = 1,
    description = "Appnext Ads Extension for Kodular",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "aiwebres/icon.png"
)
@SimpleObject(external = true)
@UsesLibraries(libraries = "AppnextAndroidSDKCore.aar, AppnextAndroidSDKAds.aar, AppnextAndroidSDKBanners.aar, AppnextAndroidSDKNativeAds.aar")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE, android.permission.WRITE_EXTERNAL_STORAGE")
public class AppnextExtension extends AndroidNonvisibleComponent {
    
    private Context context;
    private Map<String, Interstitial> interstitials;
    private Map<String, FullscreenVideo> fullscreenVideos;
    private Map<String, RewardedVideo> rewardedVideos;
    private Map<String, BannerView> banners;
    
    public AppnextExtension(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        this.interstitials = new HashMap<>();
        this.fullscreenVideos = new HashMap<>();
        this.rewardedVideos = new HashMap<>();
        this.banners = new HashMap<>();
    }
    
    // ================ INITIALIZATION ================
    
    @SimpleFunction(description = "Initialize Appnext SDK with configuration")
    public void Initialize(String appId) {
        try {
            // Appnext auto-initializes with first ad load
            // Store app ID if needed for future
            EventDispatcher.dispatchEvent(this, "AdInitialized");
        } catch (Exception e) {
            ErrorOccurred("Initialize failed: " + e.getMessage());
        }
    }
    
    // ================ INTERSTITIAL ADS ================
    
    @SimpleFunction(description = "Load interstitial ad")
    public void LoadInterstitial(final String placementId) {
        try {
            Interstitial interstitial = new Interstitial(context, placementId);
            
            interstitial.setListener(new InterstitialListener(placementId));
            
            // Set interstitial configuration
            interstitial.setBackButtonCanClose(true);
            interstitial.setMute(true);
            
            interstitial.loadAd();
            interstitials.put(placementId, interstitial);
            
        } catch (Exception e) {
            ErrorOccurred("LoadInterstitial Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Show interstitial ad")
    public void ShowInterstitial(String placementId) {
        try {
            Interstitial interstitial = interstitials.get(placementId);
            if (interstitial != null && interstitial.isAdLoaded()) {
                interstitial.showAd();
            } else {
                ErrorOccurred("Interstitial not loaded for: " + placementId);
            }
        } catch (Exception e) {
            ErrorOccurred("ShowInterstitial Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Check if interstitial is loaded")
    public boolean IsInterstitialLoaded(String placementId) {
        Interstitial interstitial = interstitials.get(placementId);
        return interstitial != null && interstitial.isAdLoaded();
    }
    
    @SimpleFunction(description = "Destroy interstitial ad")
    public void DestroyInterstitial(String placementId) {
        try {
            Interstitial interstitial = interstitials.get(placementId);
            if (interstitial != null) {
                // No explicit destroy method, just remove reference
                interstitials.remove(placementId);
            }
        } catch (Exception e) {
            ErrorOccurred("DestroyInterstitial Error: " + e.getMessage());
        }
    }
    
    // ================ BANNER ADS ================
    
    @SimpleFunction(description = "Load banner ad")
    public void LoadBanner(String placementId, AndroidViewComponent container) {
        try {
            FrameLayout layout = (FrameLayout) container.getView();
            BannerView banner = new BannerView(context);
            banner.setPlacementId(placementId);
            
            // Configure banner
            BannerAdRequest request = new BannerAdRequest();
            request.setCreativeType(AppnextAdCreativeType.VIDEO);
            request.setAutoPlay(true);
            request.setMute(true);
            
            banner.setBannerListener(new BannerListener() {
                @Override
                public void onAdLoaded(String s) {
                    BannerLoaded(placementId);
                }
                
                @Override
                public void onError(String s, AppnextError appnextError) {
                    BannerError(placementId, appnextError.getErrorMessage());
                }
                
                @Override
                public void onAdClicked() {
                    BannerClicked(placementId);
                }
                
                @Override
                public void onAdOpened() {
                    BannerOpened(placementId);
                }
                
                @Override
                public void onAdClosed() {
                    BannerClosed(placementId);
                }
            });
            
            banner.loadAd(request);
            layout.addView(banner);
            banners.put(placementId, banner);
            
        } catch (Exception e) {
            ErrorOccurred("LoadBanner Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Destroy banner ad")
    public void DestroyBanner(String placementId) {
        try {
            BannerView banner = banners.get(placementId);
            if (banner != null) {
                banner.destroy();
                banners.remove(placementId);
            }
        } catch (Exception e) {
            ErrorOccurred("DestroyBanner Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Hide banner ad (keep loaded)")
    public void HideBanner(String placementId) {
        try {
            BannerView banner = banners.get(placementId);
            if (banner != null) {
                banner.setVisibility(android.view.View.GONE);
            }
        } catch (Exception e) {
            ErrorOccurred("HideBanner Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Show banner ad (if hidden)")
    public void ShowBanner(String placementId) {
        try {
            BannerView banner = banners.get(placementId);
            if (banner != null) {
                banner.setVisibility(android.view.View.VISIBLE);
            }
        } catch (Exception e) {
            ErrorOccurred("ShowBanner Error: " + e.getMessage());
        }
    }
    
    // ================ VIDEO ADS ================
    
    @SimpleFunction(description = "Load fullscreen video ad")
    public void LoadFullscreenVideo(String placementId) {
        try {
            FullscreenVideo video = new FullscreenVideo(context, placementId);
            video.setListener(new VideoAdListener(placementId, "fullscreen"));
            
            // Configure video
            video.setBackButtonCanClose(true);
            video.setMute(true);
            
            video.loadAd();
            fullscreenVideos.put(placementId, video);
            
        } catch (Exception e) {
            ErrorOccurred("LoadFullscreenVideo Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Show fullscreen video ad")
    public void ShowFullscreenVideo(String placementId) {
        try {
            FullscreenVideo video = fullscreenVideos.get(placementId);
            if (video != null && video.isAdLoaded()) {
                video.showAd();
            } else {
                ErrorOccurred("Fullscreen video not loaded for: " + placementId);
            }
        } catch (Exception e) {
            ErrorOccurred("ShowFullscreenVideo Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Load rewarded video ad")
    public void LoadRewardedVideo(String placementId) {
        try {
            RewardedVideo video = new RewardedVideo(context, placementId);
            video.setListener(new VideoAdListener(placementId, "rewarded"));
            
            // Configure rewarded video
            video.setBackButtonCanClose(false); // Don't allow back button to close
            video.setMute(false); // Keep sound for rewarded
            
            video.loadAd();
            rewardedVideos.put(placementId, video);
            
        } catch (Exception e) {
            ErrorOccurred("LoadRewardedVideo Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Show rewarded video ad")
    public void ShowRewardedVideo(String placementId) {
        try {
            RewardedVideo video = rewardedVideos.get(placementId);
            if (video != null && video.isAdLoaded()) {
                video.showAd();
            } else {
                ErrorOccurred("Rewarded video not loaded for: " + placementId);
            }
        } catch (Exception e) {
            ErrorOccurred("ShowRewardedVideo Error: " + e.getMessage());
        }
    }
    
    @SimpleFunction(description = "Check if video ad is loaded")
    public boolean IsVideoAdLoaded(String placementId, String adType) {
        if ("fullscreen".equalsIgnoreCase(adType)) {
            FullscreenVideo video = fullscreenVideos.get(placementId);
            return video != null && video.isAdLoaded();
        } else if ("rewarded".equalsIgnoreCase(adType)) {
            RewardedVideo video = rewardedVideos.get(placementId);
            return video != null && video.isAdLoaded();
        }
        return false;
    }
    
    // ================ EVENT LISTENERS ================
    
    private class InterstitialListener implements OnAdLoaded, OnAdOpened, OnAdClicked, OnAdClosed, OnAdError {
        private String placementId;
        
        public InterstitialListener(String id) {
            this.placementId = id;
        }
        
        @Override
        public void adLoaded(String s) {
            InterstitialLoaded(placementId);
        }
        
        @Override
        public void adOpened() {
            InterstitialOpened(placementId);
        }
        
        @Override
        public void adClicked() {
            InterstitialClicked(placementId);
        }
        
        @Override
        public void adClosed() {
            InterstitialClosed(placementId);
        }
        
        @Override
        public void adError(String s) {
            InterstitialError(placementId, s);
        }
    }
    
    private class VideoAdListener implements OnAdLoaded, OnAdOpened, OnAdClicked, OnAdClosed, OnAdError, OnVideoEnded {
        private String placementId;
        private String adType;
        
        public VideoAdListener(String id, String type) {
            this.placementId = id;
            this.adType = type;
        }
        
        @Override
        public void adLoaded(String s) {
            VideoAdLoaded(placementId, adType);
        }
        
        @Override
        public void adOpened() {
            VideoAdOpened(placementId, adType);
        }
        
        @Override
        public void adClicked() {
            VideoAdClicked(placementId, adType);
        }
        
        @Override
        public void adClosed() {
            VideoAdClosed(placementId, adType);
        }
        
        @Override
        public void adError(String s) {
            VideoAdError(placementId, adType, s);
        }
        
        @Override
        public void videoEnded() {
            VideoAdCompleted(placementId, adType);
        }
    }
    
    // ================ EVENTS ================
    
    @SimpleEvent(description = "SDK initialized successfully")
    public void AdInitialized() {
        EventDispatcher.dispatchEvent(this, "AdInitialized");
    }
    
    @SimpleEvent(description = "Interstitial ad loaded successfully")
    public void InterstitialLoaded(String placementId) {
        EventDispatcher.dispatchEvent(this, "InterstitialLoaded", placementId);
    }
    
    @SimpleEvent(description = "Interstitial ad opened")
    public void InterstitialOpened(String placementId) {
        EventDispatcher.dispatchEvent(this, "InterstitialOpened", placementId);
    }
    
    @SimpleEvent(description = "Interstitial ad clicked")
    public void InterstitialClicked(String placementId) {
        EventDispatcher.dispatchEvent(this, "InterstitialClicked", placementId);
    }
    
    @SimpleEvent(description = "Interstitial ad closed")
    public void InterstitialClosed(String placementId) {
        EventDispatcher.dispatchEvent(this, "InterstitialClosed", placementId);
    }
    
    @SimpleEvent(description = "Interstitial ad failed to load or show")
    public void InterstitialError(String placementId, String errorMessage) {
        EventDispatcher.dispatchEvent(this, "InterstitialError", placementId, errorMessage);
    }
    
    @SimpleEvent(description = "Banner ad loaded successfully")
    public void BannerLoaded(String placementId) {
        EventDispatcher.dispatchEvent(this, "BannerLoaded", placementId);
    }
    
    @SimpleEvent(description = "Banner ad failed to load")
    public void BannerError(String placementId, String errorMessage) {
        EventDispatcher.dispatchEvent(this, "BannerError", placementId, errorMessage);
    }
    
    @SimpleEvent(description = "Banner ad clicked")
    public void BannerClicked(String placementId) {
        EventDispatcher.dispatchEvent(this, "BannerClicked", placementId);
    }
    
    @SimpleEvent(description = "Banner ad opened fullscreen")
    public void BannerOpened(String placementId) {
        EventDispatcher.dispatchEvent(this, "BannerOpened", placementId);
    }
    
    @SimpleEvent(description = "Banner ad closed")
    public void BannerClosed(String placementId) {
        EventDispatcher.dispatchEvent(this, "BannerClosed", placementId);
    }
    
    @SimpleEvent(description = "Video ad loaded successfully")
    public void VideoAdLoaded(String placementId, String adType) {
        EventDispatcher.dispatchEvent(this, "VideoAdLoaded", placementId, adType);
    }
    
    @SimpleEvent(description = "Video ad opened")
    public void VideoAdOpened(String placementId, String adType) {
        EventDispatcher.dispatchEvent(this, "VideoAdOpened", placementId, adType);
    }
    
    @SimpleEvent(description = "Video ad clicked")
    public void VideoAdClicked(String placementId, String adType) {
        EventDispatcher.dispatchEvent(this, "VideoAdClicked", placementId, adType);
    }
    
    @SimpleEvent(description = "Video ad closed")
    public void VideoAdClosed(String placementId, String adType) {
        EventDispatcher.dispatchEvent(this, "VideoAdClosed", placementId, adType);
    }
    
    @SimpleEvent(description = "Video ad failed to load or show")
    public void VideoAdError(String placementId, String adType, String errorMessage) {
        EventDispatcher.dispatchEvent(this, "VideoAdError", placementId, adType, errorMessage);
    }
    
    @SimpleEvent(description = "Video ad completed (rewarded videos)")
    public void VideoAdCompleted(String placementId, String adType) {
        EventDispatcher.dispatchEvent(this, "VideoAdCompleted", placementId, adType);
    }
    
    @SimpleEvent(description = "General error occurred")
    public void ErrorOccurred(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorMessage);
    }
}