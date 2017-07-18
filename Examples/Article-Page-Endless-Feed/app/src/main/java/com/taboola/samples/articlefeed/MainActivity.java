package com.taboola.samples.articlefeed;

import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.taboola.android.api.TBPlacement;
import com.taboola.android.api.TBPlacementRequest;
import com.taboola.android.api.TBRecommendationRequestCallback;
import com.taboola.android.api.TBRecommendationsRequest;
import com.taboola.android.api.TBRecommendationsResponse;
import com.taboola.android.api.TaboolaApi;
import com.taboola.samples.endlessfeed.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private TBPlacement mPlacement;
    Snackbar snackbar;

    private List<Object> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        EndlessScrollListener scrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextRecommendationsBatch();
            }
        };
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(scrollListener);

        mData.add(new DemoItem(R.drawable.image_demo, getResources().getString(R.string.lorem_ipsum1)));
        mAdapter = new FeedAdapter(mData);
        mRecyclerView.setAdapter(mAdapter);
        snackbar = Snackbar.make(mRecyclerView, "Waiting for network", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        fetchTaboolaRecommendations();
    }

    public void onAttributionClick(View view) {
        TaboolaApi.getInstance().handleAttributionClick(this);
    }

    private void onRecommendationsFetched(TBPlacement placement) {
        placement.prefetchThumbnails();
        mPlacement = placement;

        int currentSize = mAdapter.getItemCount();
        mData.addAll(placement.getItems());
        mAdapter.notifyItemRangeInserted(currentSize, placement.getItems().size());
    }

    private void fetchTaboolaRecommendations() {
        final String placementName = "list_item";
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        TBPlacementRequest placementRequest = new TBPlacementRequest(placementName, 4)
                .setThumbnailSize(screenSize.x / 2, (screenSize.y / 6)); // ThumbnailSize is optional

        TBRecommendationsRequest request = new TBRecommendationsRequest("http://example.com", "text");
        request.addPlacementRequest(placementRequest);

        TaboolaApi.getInstance().fetchRecommendations(request, new TBRecommendationRequestCallback() {
            @Override
            public void onRecommendationsFetched(TBRecommendationsResponse response) {
                MainActivity.this.onRecommendationsFetched(response.getPlacementsMap().get(placementName));
                snackbar.dismiss();
            }

            @Override
            public void onRecommendationsFailed(Throwable throwable) {
                Toast.makeText(MainActivity.this, "Fetch failed: " + throwable.getMessage(),
                        Toast.LENGTH_LONG).show();
                snackbar.dismiss();
            }
        });
    }

    private void loadNextRecommendationsBatch() {
        if (mPlacement == null) {
            return; // wait for the first request to return
        }

        TaboolaApi.getInstance().getNextBatchForPlacement(mPlacement, 10, new TBRecommendationRequestCallback() {
            @Override
            public void onRecommendationsFetched(TBRecommendationsResponse response) {
                TBPlacement placement = response.getPlacementsMap().values().iterator().next(); // there will be only one placement
                MainActivity.this.onRecommendationsFetched(placement);
            }

            @Override
            public void onRecommendationsFailed(Throwable throwable) {
                Toast.makeText(MainActivity.this, "Fetch failed: " + throwable.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
