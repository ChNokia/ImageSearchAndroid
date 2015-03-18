package com.testwork.ImageSearch;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailImage extends Activity {
    private TextView textTitle;
    private ImageView srcImage;
    public ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detail_image);

        textTitle = (TextView) findViewById(R.id.detailTitleImageId);
        srcImage = (ImageView) findViewById(R.id.detailImageId);
        imageLoader = new ImageLoader(this.getApplicationContext());

        ArrayList<String> data = getIntent().getStringArrayListExtra("titleUrl");

        srcImage.setTag(data.get(1));
        imageLoader.DisplayImage(data.get(1), this, srcImage);
        textTitle.setText(Html.fromHtml(data.get(0)));

    }
}
