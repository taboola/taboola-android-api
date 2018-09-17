# Taboola Native Android SDK (TaboolaApi)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
[ ![Download](https://api.bintray.com/packages/taboola-com/taboola-android-sdk/android-sdk/images/download.svg) ](https://bintray.com/taboola-com/taboola-android-sdk/android-sdk/_latestVersion)[![License](https://img.shields.io/badge/License%20-Taboola%20SDK%20License-blue.svg)](https://github.com/taboola/taboola-android/blob/master/LICENSE)

## Table Of Contents
1. [Getting Started](#1-getting-started)
2. [Example Apps](#2-example-apps)
3. [SDK Reference](#3-sdk-reference)
4. [Proguard](#4-proguard)
5. [GDPR](#5-gdpr)
6. [License](#6-license)

## Basic concepts
The TaboolaApi allows you to get Taboola recommendations to display in your app.
For each recommendation item TaboolaApi will provide pre-populated views, which you can style to match your app look and feel and place where needed within your app.
The views will automatically handle everything else: click handling, reporting visibility back to Taboola's server and more.

Browse through the sample apps in this repository to see how the TaboolaApi can be implemented in different types of apps.
## 1. Getting Started


>The TaboolaApi for Android is based on Taboola's REST API v1.2.
Please refer to the the Taboola REST API documenation for more details.



### 1.1. Minimum requirements

* Android version 4.0  (```android:minSdkVersion="14"```)

### 1.2. Incorporating the SDK

1. Add the library dependency to your project

  ```groovy
   implementation 'com.taboola:android-sdk:2.0.27@aar'

   // include to have clicks opened in chrome tabs rather than in a default browser (mandatory)
   implementation 'com.android.support:customtabs:27.+'

   // SDK dependencies
   implementation 'com.squareup.retrofit2:retrofit:2.3.0'
   implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
   implementation 'com.squareup.picasso:picasso:2.5.2'
 ```
> ## Notice
> We encourage developers to use the latest SDK version. In order to stay up-to-date we suggest to subscribe in order to get github notifications whenever there is a new release. For more information check: https://help.github.com/articles/managing-notifications-for-pushes-to-a-repository/


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
           ...
           TaboolaApi.getInstance().init(getApplicationContext(),
                   "<publisher-as-supplied-by-taboola>",
                   "<api-key-as-supplied-by-taboola>");
           ...
       }
   }
```
### 1.4. Construct your request for recommendations

Create a `TBPlacementRequest` for each placement (You can do this in your `Activity` or `Fragment` code)

```java
   String placementName = "article";
   int recCount = 5; //  How many recommendations should be returned

   TBPlacementRequest placementRequest = new TBPlacementRequest(placementName, recCount)
           .setThumbnailSize(400, 300) // ThumbnailSize is optional, sizes are in pixels
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
The following code requests data from Taboola servers and stores it locally:
```java
   TaboolaApi.getInstance().fetchRecommendations(recommendationsRequest, new TBRecommendationRequestCallback() {
       @Override
       public void onRecommendationsFetched(TBRecommendationsResponse response) {
           // map where a Key is the Placements name (you can store it as a member variable for convenience)
           Map<String, TBPlacement> placementsMap = response.getPlacementsMap();
       }

       @Override
       public void onRecommendationsFailed(Throwable throwable) {
           //TODO: handle error
           Log.d(TAG, "Failed: " + throwable.getMessage());
       }
   });
```

### 1.6. Displaying Taboola recommendations
To display Taboola native views, follow these steps:

#### 1.6.1. About <AdContainer>:
In the upcoming code sample, <AdContainer> is the View in which you wish to contain Taboola's child Views. This example shows how to add the views of one Taboola Item to a parent View in your app.

#### 1.6.2. Extract Taboola Views from Server data
Inside the "onRecommendationsFetched" callback, after creating the placementsMap instance, extract a Taboola placement object and from it, a Taboola item. You can display Taboola content using native objects using the following code:
```java
   TaboolaApi.getInstance().fetchRecommendations(recommendationsRequest, new TBRecommendationRequestCallback() {
       @Override
       public void onRecommendationsFetched(TBRecommendationsResponse response) {
           // map where a Key is the Placements name (you can store it as a member variable for convenience)
           Map<String, TBPlacement> placementsMap = response.getPlacementsMap();
           
           extractViewsFromOneItem();

   }
   
   /**
    * This demo only shows handling one item, you might want to display additional items.
    * (For example, directly inject the Item List to a RecyclerView adapter).
    */
   private void extractViewsFromOneItem() {
    TBPlacement placement = placementsMap.get(placementName);
    if (placement != null){
      TBRecommendationItem item = placement.getItems().get(0);
      <AdContainer>.addView(item.getThumbnailView(<Actvitiy>));
      <AdContainer>.addView(item.getTitleView(<Actvitiy>));
      TBTextView brandingView = item.getBrandingView(<Actvitiy>);
      if (brandingView != null) { // If branding text is not available null is returned
        <AdContainer>.addView(brandingView);
      }
    }
   }
```
Note: A `brandingView` is meant to display the advertising brand information.

### 1.7.Taboola Attribution View
Taboola Attribution View is a view with a "By Taboola" text and icon.

#### 1.7.1. Add Taboola Attribution View to your layout
The following is a sample, feel free to implement yourself:
```xml
    <LinearLayout
        android:id="@+id/attribution_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="right"
        android:weightSum="4"
        android:orientation="horizontal">

        <ImageView
            android:layout_width="0dp"
            android:layout_weight="1"
            android:layout_height="wrap_content"
            android:scaleType="fitCenter"
            app:srcCompat="@drawable/icon_attribution"/>
        
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginRight="4dp"
            android:layout_weight="3"
            android:gravity="right"
            android:text="@string/attribution_view_text"/>
    </LinearLayout>
```

#### 1.7.2. Find Taboola Attribution View
```java
  View attributionView = <Activity>.findViewById(R.id.attribution_view);
```

#### 1.7.3. Set a click listener on Taboola Attribution View:
```java
   attributionView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onAttributionClick();
      }
   });
        
   public void onAttributionClick() {
       TaboolaApi.getInstance().handleAttributionClick(<Activity>);
   }
```

### 1.8 Request next batch of the recommendations for placement
Used for implementing pagination or infinite scroll (load more items when the user scrolls down). The method gets the next batch of recommendation items for a specified placement. The name of the returned Placement will have a "counter" added as a suffix. For example, if the original placement name was "article" the new name will be "article 1", next one "article 2", and so on. The counter is incremented on each successful fetch.


```Java
TaboolaApi.getInstance().getNextBatchForPlacement(mPlacement, optionalCount, new TBRecommendationRequestCallback() {
  @Override
  public void onRecommendationsFetched(TBRecommendationsResponse response) {
    TBPlacement placement = response.getPlacementsMap().values().iterator().next(); // There will be only one placement.
    //TODO: Do something with new the Items
  } 

  @Override
  public void onRecommendationsFailed(Throwable throwable) {
    Log.d(TAG, "Fetch failed:" + throwable.getMessage());
  } 
});
```

### 1.9. Intercepting recommendation clicks

##### 1.9.1. The default click behaviour of TaboolaWidget is as follows:
* On devices where `Chrome Custom Tabs` are supported - Taboola will open the recommendation in a Chrome Custom Tab (in-app)
* Otherwise - Taboola will open the recommendation in the default system web browser (outside of the app)

##### 1.9.2. Overriding default behaviour:
TaboolaApi allows app developers to intercept recommendation clicks in order to create a click-through or to override the default way of opening the recommended article.

In order to intercept clicks, you should implement the interface `com.taboola.android.api.TaboolaOnClickListener` and set it in the sdk.

1. Implement the interface `com.taboola.android.api.TaboolaOnClickListener` 
    1.1 `TaboolaOnClickListener` include the methods:
     ```java
    public boolean onItemClick(String placementName, String itemId, String clickUrl, boolean isOrganic);
     ```
    1.2 Example implementation:
    In the same Activity/Fragment as `TaboolaWidget` instance:
     ```java
    TaboolaOnClickListener taboolaOnClickListener = new TaboolaOnClickListener() {
      @Override
      public boolean onItemClick(String placementName, String itemId, String clickUrl, boolean isOrganic) {          
          //Code...
          return false;
      }};
     ```    
2. Connect the event listener to your `TaboolaWidget` instance. 
    ```java
    TaboolaApi.getInstance().setOnClickListener(taboolaOnClickListener);
    ```    
    
##### 1.9.3. Event: onItemClick
`boolean onItemClick(String placementName, String itemId, String clickUrl, boolean isOrganic)`
This method will be called every time a user clicks on a Taboola Recommendation, right before it is sent to Android OS for relevant action resolve. The return value of this method allows you to control further system behaviour (after your own code executes).

###### 1.9.3.1 `placementName:`
The name of the placement, in which an Item was clicked.

###### 1.9.3.2 `itemtId:`
The id of the Item clicked.

###### 1.9.3.3 `clickUrl:`
Original click url.

###### 1.9.3.4 `isOrganic:` 
Indicates whether the item clicked was an organic content Taboola Recommendation or not.
(The **best practice** would be to suppress the default behavior for organic items, and instead open the relevant screen in your app which will show that piece of content).

###### 1.9.3.5 `Return value:`
* Returning **`false`** - Aborts the click's default behavior. The app should display the Taboola Recommendation content on its own (for example, using an in-app browser).
* Returning **`true`** - The click will be a standard one and will be sent to the Android OS for default behaviour.
**Note:** Sponsored item clicks (non-organic) are not overridable!    
    
    
## 2. Example App
This repository includes an example Android app which uses the `TaboolaApi`.

To use it:
##### 2.1 Clone this repository
1. Look for the "Clone or Download" button on this page top.
2. Copy the url from the drop box.
3. Clone to your local machine using your favourite Git client.

##### 2.2 Open the project wih your IDE.
1. Open the project as you would any other Android project.
2. Taboola is optimized to working with Android Studio but other IDEs should work as well.

##### 2.3 Example App As Troubleshooting Helper:
In case you encounter some issues while integrating the SDK into your app, try to recreate the scenario within the example app. This might help to isolate the problems. For more help, you would be able to send the example app with your recreated issue to Taboola's support.

## 3. SDK Reference
[TaboolaApi Reference](doc/TaboolaApi_reference.md)

## 4. ProGuard
You can find proguard rules for Taboola Widget in [proguard-taboola-api.pro](/Examples/Article-Page-4-Items-Bottom/app/proguard-taboola-api.pro) file.
The file contains instructions to the rules which you should use depending on which parts of the SDK you are using (you should comment/uncomment which you need).

## 5. GDPR
In order to support the The EU General Data Protection Regulation (GDPR - https://www.eugdpr.org/) in Taboola Mobile SDK, application developer should show a pop up asking the user’s permission for storing their personal data in the App. In order to control the user’s personal data (to store in the App or not) there exists a flag `User_opt_out`. It’s mandatory to set this flag when using the Taboola SDK. The way to set this flag depends on the type of SDK you are using. By default we assume no permission from the user on a pop up, so the personal data will not be saved.

### 5.1. How to set the flag in the SDK integration
Below you can find the way how to set the flag on API Android SDK we support. It’s recommended to put these lines alongside the other settings, such as publisher name, etc.

```java
    ...
    HashMap<String, String> optionalPageCommands = new HashMap<>();
    optionalPageCommands.put("apiParams", "user.opt_out=true");
    TaboolaApi.getInstance().setExtraProperties(optionalPageCommands);
```

## 6. License
This program is licensed under the Taboola, Inc. SDK License Agreement (the “License Agreement”).  By copying, using or redistributing this program, you agree with the terms of the License Agreement.  The full text of the license agreement can be found at [https://github.com/taboola/taboola-android/blob/master/LICENSE](https://github.com/taboola/taboola-android/blob/master/LICENSE).
Copyright 2017 Taboola, Inc.  All rights reserved.
