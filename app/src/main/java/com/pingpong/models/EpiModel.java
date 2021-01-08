package com.pingpong.models;

import java.util.List;

public class EpiModel {
    String seson,epi,streamURL,serverType, imageUrl;
    private String skipIntro="";
    List<SubtitleModel> subtitleList;
    private String id="";


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getStreamURL() {
        return streamURL;
    }

    public void setStreamURL(String streamURL) {
        this.streamURL = streamURL;
    }

    public String getSeson() {
        return seson;
    }

    public void setSeson(String seson) {
        this.seson = seson;
    }

    public String getEpi() {
        return epi;
    }

    public void setEpi(String epi) {
        this.epi = epi;
    }

    public List<SubtitleModel> getSubtitleList() {
        return subtitleList;
    }

    public void setSubtitleList(List<SubtitleModel> subtitleList) {
        this.subtitleList = subtitleList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSkipIntro() {
        return skipIntro;
    }

    public void setSkipIntro(String skipIntro) {
        this.skipIntro = skipIntro;
    }
}
