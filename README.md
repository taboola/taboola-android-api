# Taboola Native Android SDK (TaboolaApi)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
[ ![Download](https://api.bintray.com/packages/taboola-com/taboola-android-sdk/android-sdk/images/download.svg?version=1.3.6) ](https://bintray.com/taboola-com/taboola-android-sdk/android-sdk/1.3.6/link)
[![License](https://img.shields.io/badge/License%20-Taboola%20SDK%20License-blue.svg)](https://github.com/taboola/taboola-android/blob/master/LICENSE)

## Table Of Contents
1. [Getting Started](#1-getting-started)
2. [Example Apps](#2-example-apps)
3. [SDK Reference](#3-sdk-reference)
4. [Proguard](#4-proguard)
5. [License](#5-license)

## Basic concepts
The TaboolaApi allows you to get Taboola recommendations to display in your app. 
For each recommendation item TaboolaApi will provide pre-populated views, which you can style to match your app look and feel and place where needed within your app.
The views will automatically handle everything else: click handling, reporting visibility back to Taboola's server and more.

Browse through the sample apps in this repository to see how the TaboolaApi can be implemented in different types of apps.
## 1. Getting Started


>The TaboolaApi for Android is based on Taboola's REST API v1.2. 
Please refer to the the Taboola REST API documenation for more details.



### 1.1. Minimum requirements

* Android version 2.1  (```android:minSdkVersion="9"```)

### 1.2. Incorporating the SDK

1. Add the library dependency to your project
  
  ```groovy
   compile 'com.taboola:android-sdk:1.3.+@aar'

   // include to have clicks opened in chrome tabs rather than in a default browser (mandatory)
   compile 'com.android.support:customtabs:26.+'

   // SDK dependencies
   compile 'com.squareup.retrofit2:retrofit:2.3.0'
   compile 'com.squareup.retrofit2:converter-gson:2.3.0'
   compile 'com.squareup.picasso:picasso:2.5.2'
 ```
> ## Notice
> We encourage developers to use the latest SDK version. In order to stay up-to-date we suggest subscribing to get github notifications whenever there is a new release. For more information check: https://help.github.com/articles/managing-notifications-for-pushes-to-a-repository/


2. Include this line in your app’s AndroidManifest.xml to allow Internet access
 ```
   <uses-permission android:name="android.permission.INTERNET" />
 ```

### 1.3. Init TaboolaApi

In your `Application` class

```java
   public class MyApplication extends Application {
       @Override
       public void onCreate() {
           super.onCreate();
           TaboolaApi.getInstance().init(getApplicationContext(),
                   "<publisher-as-supplied-by-taboola>",
                   "<api-key-as-supplied-by-taboola>");
       }
   }
```
### 1.4. Construct your request for recommendations

Create a `TBPlacementRequest` for each placement (You can do this in your `Activity` or `Fragment` code)

```java
   String placementName = "article";
   int recCount = 5; //  how many recommendations should be returned
   
   TBPlacementRequest placementRequest = new TBPlacementRequest(placementName, recCount)
           .setThumbnailSize(400, 300) // ThumbnailSize is optional
           .setTargetType("mix"); // TargetType is optional
```
Create a `TBRecommendationsRequest` and add all of the previously created `TBPlacementRequest` objects to it

```java
   String pageUrl = "http://example.com";
   String sourceType = "text";
           
   TBRecommendationsRequest recommendationsRequest =
           new TBRecommendationsRequest(pageUrl, sourceType)
                   .setUserReferrer("<UserReferrer>") // optional
                   .setUserUnifiedId("<UnifiedId>") // optional
                   .setSourceId("<SourceId>") // optional
                   .addPlacementRequest(placementRequest)
                   .addPlacementRequest(placementRequest2)
                   .addPlacementRequest(placementRequest3);
```

(Maximum 12 `TBPlacementRequest` per one `TBRecommendationsRequest`) 

### 1.5. Fetch Taboola recommendations
```java
   TaboolaApi.getInstance().fetchRecommendations(recommendationsRequest, new TBRecommendationRequestCallback() {
       @Override
       public void onRecommendationsFetched(TBRecommendationsResponse response) {
           // map where a Key is the Placements name (you can store it as a member variable for convenience)
           Map<String, TBPlacement> placementsMap = response.getPlacementsMap();
       }
                           
       @Override
       public void onRecommendationsFailed(Throwable throwable) {
           // todo handle error
           Toast.makeText(MainActivity.this, "Failed: " + throwable.getMessage(),
                   Toast.LENGTH_LONG).show();
       }
   });
```

### 1.6. Displaying Taboola recommendations
```java
   TBPlacement placement = placementsMap.get(placementName);
   TBRecommendationItem item = placement.getItems().get(0);
                
   mAdContainer.addView(item.getThumbnailView(MainActivity.this));
   mAdContainer.addView(item.getTitleView(MainActivity.this));
   TBTextView brandingView = item.getBrandingView(this);
   if (brandingView != null) { // If branding text is not available null is returned
       mAdContainer.addView(brandingView);
   }
```

### 1.7. Supply your own implementation of the attribution view
Attribution view is a view with localized "By Taboola" text and icon.
Call `handleAttributionClick()` every time this view is clicked 
```java
   public void onAttributionClick() {
       TaboolaApi.getInstance().handleAttributionClick(MainActivity.this);
   }
```

### 1.8 Request next batch of the recommendations for placement
Used for implementing pagination or infinite scroll (load more items as the user scrolls down). The method gets the next batch of recommendation items for a specified placement. The name of the returned Placement will have a "counter" added as a suffix. For example, if the original placement name was "article" the new name will be "article 1", next one "article 2", and so on. The counter is incremented on each successful fetch.


```Java
   TaboolaApi.getInstance().getNextBatchForPlacement(mPlacement, optionalCount, new TBRecommendationRequestCallback() {
           @Override
           public void onRecommendationsFetched(TBRecommendationsResponse response) {
               TBPlacement placement = response.getPlacementsMap().values().iterator().next(); // there will be only one placement
               // todo do smth with new the Items
           }

           @Override
           public void onRecommendationsFailed(Throwable throwable) {
               Toast.makeText(MainActivity.this, "Fetch failed: " + throwable.getMessage(),
                       Toast.LENGTH_LONG).show();
           }
 });
```


### 1.9. Intercepting recommendation clicks

The default click behavior of TaboolaWidget is as follows:

* On devices where Chrome custom tab is supported - open the recommendation in a Chrome custom tab (in-app)
* Otherwise - open the recommendation in the system default web browser (outside of the app) 

TaboolaApi allows app developers to intercept recommendation clicks in order to create a click-through or to override the default way of opening the recommended article. 

In order to intercept clicks, you should implement the interface `com.taboola.android.api.TaboolaOnClickListener` and set it in the sdk.

```java
   TaboolaApi.getInstance().setOnClickListener(new TaboolaOnClickListener() {
       @Override
       public boolean onItemClick(String placementName, String itemId, String clickUrl, boolean isOrganic) {
           return false;
       }
   });

```

This method will be called every time a user clicks a recommendation, right before triggering the default behavior. You can block default click handling for organic items by returning `false` in `onItemClick()` method.

* Return **`false`** - abort the default behavior, the app should display the recommendation content on its own (for example, using an in-app browser). (Aborts only for organic items!)
* Return **`true`** - this will allow the app to implement a click-through and continue to the default behaviour.

`isOrganic` indicates whether the item clicked was an organic content recommendation or not.
**Best practice would be to suppress the default behavior for organic items, and instead open the relevant screen in your app which shows that piece of content.**

## 2. Example App
This repository includes an example Android app which uses the `TaboolaApi`.

## 3. SDK Reference
[TaboolaApi Reference](doc/TaboolaApi_reference.md)

## 4. ProGuard
You can find proguard rules for Taboola Widget in [proguard-taboola-api.pro](/Examples/Article-Page-4-Items-Bottom/app/proguard-taboola-api.pro) file.
The file contains instructions on which rules to comment/uncomment depending on which parts of the SDK you are using.

## 5. License
This program is licensed under the Taboola, Inc. SDK License Agreement (the “License Agreement”).  By copying, using or redistributing this program, you agree to the terms of the License Agreement.  The full text of the license agreement can be found at [https://github.com/taboola/taboola-android/blob/master/LICENSE](https://github.com/taboola/taboola-android/blob/master/LICENSE).
Copyright 2017 Taboola, Inc.  All rights reserved.
