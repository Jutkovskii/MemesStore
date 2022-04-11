package com.log28.memesstore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.SystemClock;
import android.view.View;

import java.io.InputStream;

public class GifView extends View {
    Movie gifMovie;
    InputStream stream;
    long movieStart;
    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, InputStream stream) {
        super(context);
        this.stream=stream;
        gifMovie= Movie.decodeStream(stream);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        final long now= SystemClock.uptimeMillis();
        if(movieStart==0) movieStart=now;
        final int relTime = (int) ((now-movieStart)%gifMovie.duration());
        gifMovie.setTime(relTime);
        gifMovie.draw(canvas,0,0);
        this.invalidate();
    }
}
