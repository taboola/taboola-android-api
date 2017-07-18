package com.taboola.samples.article;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.taboola.android.sdk.TBPlacementRequest;
import com.taboola.android.sdk.TBRecommendationItem;
import com.taboola.android.sdk.TBRecommendationRequestCallback;
import com.taboola.android.sdk.TBRecommendationsRequest;
import com.taboola.android.sdk.TBRecommendationsResponse;
import com.taboola.android.sdk.TaboolaSdk;
import com.taboola.samples.R;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mAdContainer;
    private LinearLayout mAttributionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdContainer = (LinearLayout) findViewById(R.id.ad_container);
        mAttributionView = (LinearLayout) findViewById(R.id.attribution_view);

        final String placementName = "article";
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        TBPlacementRequest placementRequest = new TBPlacementRequest(placementName, 4)
                .setThumbnailSize(screenSize.x, (screenSize.y / 3)); // ThumbnailSize is optional

        TBRecommendationsRequest request = new TBRecommendationsRequest("http://example.com", "text");
        request.addPlacementRequest(placementRequest);

        TaboolaSdk.getInstance().fetchRecommendations(request, new TBRecommendationRequestCallback() {
            @Override
            public void onRecommendationsFetched(TBRecommendationsResponse response) {
                List<TBRecommendationItem> items = response.getPlacementsMap().get(placementName).getItems();
                showRecommendations(items);
            }

            @Override
            public void onRecommendationsFailed(Throwable throwable) {
                Toast.makeText(MainActivity.this, "Failed: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onAttributionClick(View view) {
        TaboolaSdk.getInstance().handleAttributionClick(this);
    }

    private void showRecommendations(List<TBRecommendationItem> items) {
        mAttributionView.setVisibility(View.VISIBLE);

        for (TBRecommendationItem item : items) {
            mAdContainer.addView(item.getThumbnailView(MainActivity.this));
            mAdContainer.addView(item.getTitleView(MainActivity.this));
            mAdContainer.addView(item.getBrandingView(this));

            View separator = new View(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, pxFromDp(this, 20));
            separator.setLayoutParams(layoutParams);

            mAdContainer.addView(separator);
        }
    }

    public static int pxFromDp(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
