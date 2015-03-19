package com.testwork.ImageSearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ImageSearchActivity extends Activity implements OnClickListener, AdapterView.OnItemClickListener {
    private Button btnSearch;
    private EditText textEditSearch;
    private ListView listViewImages;
    private String textSearch;
    private List<ImageData> imageDataList;
    private AdapterImageData adapter;
    private ImageDB imagesDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnSearch = (Button)findViewById(R.id.btnSearchId);
        textEditSearch = (EditText)findViewById(R.id.textSearchId);
        listViewImages = (ListView) findViewById(R.id.listViewId);
        imagesDB = new ImageDB(this);

        btnSearch.setOnClickListener(this);
        listViewImages.setOnItemClickListener(this);

        if ( !isOnline() ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_dialog)
                    .setMessage(R.string.offline)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawableResource(R.drawable.dialogdb);
            alert.show();
            btnSearch.setEnabled(false);

            imageDataList = imagesDB.getAllImageData();

            SetListViewAdapter(imageDataList);
        }


    }

    @Override
    public void onClick(View view) {
        textSearch = textEditSearch.getText().toString();
        textSearch = Uri.encode(textSearch);

        new SearchImagesTask().execute();
    }

    public List<ImageData> getImageDataList (JSONArray resultArray) {
        List<ImageData> imageDataList = new ArrayList<ImageData>();

        try {
            for ( int i = 0; i < resultArray.length(); i++ ) {
                JSONObject obj = resultArray.getJSONObject(i);
                ImageData imageData = new ImageData();

                imageData.setTitle(obj.getString("title"));
                imageData.setThumbUrl(obj.getString("tbUrl"));
                imageData.setUrl(obj.getString("url"));

                imageDataList.add(imageData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageDataList;
    }

    public void SetListViewAdapter(List<ImageData> images)
    {
        adapter = new AdapterImageData(this, images);
        listViewImages.setAdapter(adapter);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
                            int position, long id) {
        ImageData itemAtPosition = (ImageData)adapterView.getItemAtPosition(position);

        Intent intent = new Intent(this.getApplicationContext(), DetailImage.class);
        ArrayList<String> sendData = new ArrayList<String>();

        sendData.add(itemAtPosition.getTitle());
        sendData.add(itemAtPosition.getUrl());

        intent.putStringArrayListExtra("titleUrl", sendData);

        startActivity(intent);
    }

    public class SearchImagesTask extends AsyncTask<Void, Void, Void> {
        private final int MAX_NUMBER_IMAGE_RESPONSE = 64;
        private final int NUMBER_IMAGE_RESPONSE = 8;
        private ProgressDialog dialogWait;
        private JSONArray jsonArrayDataImage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            jsonArrayDataImage = new JSONArray();
            dialogWait = ProgressDialog.show(ImageSearchActivity.this, "", "Wait...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL url;

            try
            {
                for ( int i = 0; i < MAX_NUMBER_IMAGE_RESPONSE; i += NUMBER_IMAGE_RESPONSE ) {
                    url = new URL("https://ajax.googleapis.com/ajax/services/search/images?" +
                            "v=1.0&q=" + textSearch + "&start=" + i + "&rsz=" + NUMBER_IMAGE_RESPONSE);

                    URLConnection connection = url.openConnection();

                    String line;
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    //System.out.println(builder.toString());
                    JSONObject jsonDataImage = new JSONObject(builder.toString());
                    JSONObject responseData = jsonDataImage.getJSONObject("responseData");
                    JSONArray resultArray = responseData.getJSONArray("results");

                    for ( int j = 0; j < resultArray.length(); j++ ) {
                        JSONObject obj = resultArray.getJSONObject(j);
                        jsonArrayDataImage.put(obj);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(dialogWait.isShowing())
            {
                dialogWait.dismiss();
            }

            //JSONObject responseData = jsonDataImage.getJSONObject("responseData");
            // JSONArray resultArray = responseData.getJSONArray("results");

            imageDataList = getImageDataList(jsonArrayDataImage);

            imagesDB.addListImageData(imageDataList);

            SetListViewAdapter(imageDataList);
        }
    }
}
