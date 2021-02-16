
package com.pingpong.models.single_details;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Video {

    @SerializedName("video_file_id")
    @Expose
    private String videoFileId;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("stream_key")
    @Expose
    private String streamKey;
    @SerializedName("file_type")
    @Expose
    private String fileType;
    @SerializedName("file_url")
    @Expose
    private String fileUrl;
    @SerializedName("subtitle")
    @Expose
    private List<Subtitle> subtitle = new ArrayList<>();
    @SerializedName("skip_intro")
    @Expose
    private String skipIntro = "";
    @SerializedName("paid")
    @Expose
    private String paid = "0";
    @SerializedName("free_time")
    @Expose
    private String free_time = "0";
    @SerializedName("ads")
    @Expose
    private String ads = "";

    public String getVideoFileId() {
        return videoFileId;
    }

    public void setVideoFileId(String videoFileId) {
        this.videoFileId = videoFileId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public List<Subtitle> getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(List<Subtitle> subtitle) {
        this.subtitle = subtitle;
    }

    public String getSkipIntro() {
        return skipIntro;
    }

    public void setSkipIntro(String skipIntro) {
        this.skipIntro = skipIntro;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public String getFree_time() {
        return free_time;
    }

    public void setFree_time(String free_time) {
        this.free_time = free_time;
    }

    public String getAds() {
        return ads;
    }

    public void setAds(String ads) {
        this.ads = ads;
    }
}
