package com.example.sampleblank.model;

public class Store {
    public long storeId;
    public String storeImage;
    public String storeName;
    public String storeOfferDescription;
    public Double storeLatitude;
    public Double storeLongitude;
    public String storeOffer;
    public String storeOfferExpiration;

    public Store(Long storeId, String storeName, String storeOffer,
                 Double storeLatitude, Double storeLongitude,
                 String storeOfferDescription, String storeOfferExpiration,
                 String storeImage) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeOffer = storeOffer;
        this.storeLatitude = storeLatitude;
        this.storeLongitude = storeLongitude;
        this.storeOfferDescription = storeOfferDescription;
        this.storeOfferExpiration = storeOfferExpiration;
        this.storeImage = storeImage;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public String getStoreImage() {
        return storeImage;
    }

    public void setStoreImage(String storeImage) {
        this.storeImage = storeImage;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreOfferDescription() {
        return storeOfferDescription;
    }

    public void setStoreOfferDescription(String storeOfferDescription) {
        this.storeOfferDescription = storeOfferDescription;
    }

    public Double getStoreLatitude() {
        return storeLatitude;
    }

    public void setStoreLatitude(Double storeLatitude) {
        this.storeLatitude = storeLatitude;
    }

    public Double getStoreLongitude() {
        return storeLongitude;
    }

    public void setStoreLongitude(Double storeLongitude) {
        this.storeLongitude = storeLongitude;
    }

    public String getStoreOffer() {
        return storeOffer;
    }

    public void setStoreOffer(String storeOffer) {
        this.storeOffer = storeOffer;
    }

    public String getStoreOfferExpiration() {
        return storeOfferExpiration;
    }

    public void setStoreOfferExpiration(String storeOfferExpiration) {
        this.storeOfferExpiration = storeOfferExpiration;
    }
}
