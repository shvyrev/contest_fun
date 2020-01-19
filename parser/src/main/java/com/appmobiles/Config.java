package com.appmobiles;

import io.quarkus.arc.config.ConfigProperties;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

@ConfigProperties(prefix = "application")
public class Config {

    private int threads;
    private List<String> proxies = new ArrayList<>();
    private String url;
    private String seedSelector;
    private String contentPageSelector;
    private String contentPageUrlPattern;
    private String imageSelector;
    private String videoSelector;
    private String descriptionSelector;
    private String titleSelector;
    private String upsSelector;
    private List<String> textLanguages = new ArrayList<>();
    private String storageUrl;
    private String storageUser;
    private String storagePassword;
    private String storageLinkPattern;
    private String redisUrl;

    public String getRedisUrl() {
        return redisUrl;
    }

    public void setRedisUrl(String redisUrl) {
        this.redisUrl = redisUrl;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    public String getStorageUser() {
        return storageUser;
    }

    public void setStorageUser(String storageUser) {
        this.storageUser = storageUser;
    }

    public String getStoragePassword() {
        return storagePassword;
    }

    public void setStoragePassword(String storagePassword) {
        this.storagePassword = storagePassword;
    }

    public String getStorageLinkPattern() {
        return storageLinkPattern;
    }

    public void setStorageLinkPattern(String storageLinkPattern) {
        this.storageLinkPattern = storageLinkPattern;
    }

    public String getStorageBucketName() {
        return storageBucketName;
    }

    public void setStorageBucketName(String storageBucketName) {
        this.storageBucketName = storageBucketName;
    }

    private String storageBucketName;

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public List<String> getProxies() {
        return proxies;
    }

    public void setProxies(List<String> proxies) {
        this.proxies = proxies;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSeedSelector() {
        return seedSelector;
    }

    public void setSeedSelector(String seedSelector) {
        this.seedSelector = seedSelector;
    }

    public String getContentPageSelector() {
        return contentPageSelector;
    }

    public void setContentPageSelector(String contentPageSelector) {
        this.contentPageSelector = contentPageSelector;
    }

    public String getContentPageUrlPattern() {
        return contentPageUrlPattern;
    }

    public void setContentPageUrlPattern(String contentPageUrlPattern) {
        this.contentPageUrlPattern = contentPageUrlPattern;
    }

    public String getImageSelector() {
        return imageSelector;
    }

    public void setImageSelector(String imageSelector) {
        this.imageSelector = imageSelector;
    }

    public String getVideoSelector() {
        return videoSelector;
    }

    public void setVideoSelector(String videoSelector) {
        this.videoSelector = videoSelector;
    }

    public String getDescriptionSelector() {
        return descriptionSelector;
    }

    public void setDescriptionSelector(String descriptionSelector) {
        this.descriptionSelector = descriptionSelector;
    }

    public String getTitleSelector() {
        return titleSelector;
    }

    public void setTitleSelector(String titleSelector) {
        this.titleSelector = titleSelector;
    }

    public String getUpsSelector() {
        return upsSelector;
    }

    public void setUpsSelector(String upsSelector) {
        this.upsSelector = upsSelector;
    }

    public List<String> getTextLanguages() {
        return textLanguages;
    }

    public void setTextLanguages(List<String> textLanguages) {
        this.textLanguages = textLanguages;
    }

    public JsonObject toJson(){
        return JsonObject.mapFrom(this);
    }
}
