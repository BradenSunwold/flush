package edu.washington.cs.flush;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.RatingBar;

/**
 * Created by zhuyina on 10/16/16.
 */
public class RateActivity extends Activity {

    RatingBar rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_rating);
        rating = (RatingBar) findViewById(R.id.rating);
    }
}
