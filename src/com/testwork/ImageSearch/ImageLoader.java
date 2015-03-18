package com.testwork.ImageSearch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageLoader {
    final int stub_id = R.drawable.ic_launcher;

    private HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();
    private File cacheDir;
    Context context;
    PhotosQueue photosQueue=new PhotosQueue();
    PhotosLoader photoLoaderThread = new PhotosLoader();

    public ImageLoader(Context context)
    {
        this.context = context;

        photoLoaderThread.setPriority(Thread.NORM_PRIORITY-1);

        if ( android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) ) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), context.getString(R.string.cache_dirname));
        } else {
            cacheDir = context.getCacheDir();
        }

        if( !cacheDir.exists( )) {
            cacheDir.mkdirs();
        }
    }


    public void DisplayImage(String url, Activity activity, ImageView imageView)
    {
        if( cache.containsKey(url) ) {
            imageView.setImageBitmap(cache.get(url));
        }
        else {
            queuePhoto(url, activity, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(String url, Activity activity, ImageView imageView)
    {
        photosQueue.Clean(imageView);

        PhotoToLoad p = new PhotoToLoad(url, imageView);

        synchronized(photosQueue.photosToLoad){
            photosQueue.photosToLoad.push(p);
            photosQueue.photosToLoad.notifyAll();
        }

        if( photoLoaderThread.getState() == Thread.State.NEW ) {
            photoLoaderThread.start();
        }
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size = 1024;

        try
        {
            byte[] bytes = new byte[buffer_size];

            for( ; ; )
            {
                int count = is.read(bytes, 0, buffer_size);

                if( count == -1 )
                    break;

                os.write(bytes, 0, count);
            }
        } catch(Exception ex){}
    }

    private Bitmap getBitmap(String url)
    {
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        Bitmap b = decodeFile(f);

        if( b != null ) {
            return b;
        }

        try {
            Bitmap bitmap = null;
            InputStream is = new URL(url).openStream();
            OutputStream os = new FileOutputStream(f);
            CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch(MalformedURLException e) {
            Bitmap bookDefaultIcon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_launcher);

            return bookDefaultIcon;
        }
        catch (Exception ex){
            ex.printStackTrace();

            return null;
        }
    }

    private Bitmap decodeFile(File f){
        try {
            final int REQUIRED_SIZE = 70;
            int scale = 1;
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            int width_tmp = o.outWidth;
            int height_tmp = o.outHeight;

            for ( ; ; scale++ ) {
                if( width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE )
                    break;

                width_tmp /= 2;
                height_tmp /= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;

            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}

        return null;
    }

    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    public void stopThread()
    {
        photoLoaderThread.interrupt();
    }

    class PhotosQueue
    {
        private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();

        public void Clean(ImageView image)
        {
            for( int j = 0 ; j<photosToLoad.size(); ){
                if( photosToLoad.get(j).imageView == image ) {
                    photosToLoad.remove(j);
                } else {
                    ++j;
                }
            }
        }
    }

    class PhotosLoader extends Thread {
        public void run() {
            try {
                for ( ; ; ) {
                    if ( photosQueue.photosToLoad.size() == 0 )
                        synchronized (photosQueue.photosToLoad) {
                            photosQueue.photosToLoad.wait();
                        }
                    if ( photosQueue.photosToLoad.size() != 0 ) {
                        PhotoToLoad photoToLoad;

                        synchronized (photosQueue.photosToLoad) {
                            photoToLoad = photosQueue.photosToLoad.pop();
                        }

                        Bitmap bmp = getBitmap(photoToLoad.url);

                        cache.put(photoToLoad.url, bmp);

                        if ( ((String) photoToLoad.imageView.getTag()).equals(photoToLoad.url) ) {
                            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad.imageView);
                            Activity a = (Activity) photoToLoad.imageView.getContext();

                            a.runOnUiThread(bd);
                        }
                    }
                    if ( Thread.interrupted() )
                        break;
                }
            } catch (InterruptedException e) {
            }
        }
    }

    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        ImageView imageView;

        public BitmapDisplayer(Bitmap b, ImageView i) {
            bitmap = b;
            imageView = i;
        }

        public void run()
        {
            if( bitmap != null )
                imageView.setImageBitmap(bitmap);
            else
                imageView.setImageResource(stub_id);
        }
    }

    public void clearCache() {
        cache.clear();

        File[] files = cacheDir.listFiles();

        for( File f:files ) {
            f.delete();
        }
    }

}
