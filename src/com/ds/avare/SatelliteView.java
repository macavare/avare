/*
Copyright (c) 2012, Zubair Khan (governer@gmail.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

/**
 * 
 * @author zkhan
 *
 * Draws all infromation recieved from satellites and GPS 
 */
public class SatelliteView extends View {

    /*
     * Satellite view
     */
    private GpsStatus       mGpsStatus;
    private double         mLatitude;
    private double         mLongitude;
    private Paint           mPaint;
    private float          min;
    private Context         mContext;
    private float          mFontHeight;
    private int            mAccuracy;
    String 				     mLastTime;

    /**
     * 
     */
    private void setup(Context context) {
        mContext = context;        
        mLatitude = 0;
        mLongitude = 0;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "LiberationMono-Bold.ttf"));
        mPaint.setStrokeWidth(4);
        mPaint.setShadowLayer(0, 0, 0, Color.BLACK);
        mLastTime = "";
    }
    
    /**
     * 
     * @param context
     */
    public SatelliteView(Context context) {
        super(context);
        setup(context);
    }

    /**
     * 
     * @param context
     * @param aset
     */
    public SatelliteView(Context context, AttributeSet aset) {
        super(context, aset);
        setup(context);
    }

    /**
     * @param context
     * Default for tools, do not call
     */
    public SatelliteView(Context context, AttributeSet aset, int arg) {
        super(context, aset, arg);
        setup(context);
    }

    /**
     * 
     * @param status
     */
    public void updateGpsStatus(GpsStatus status) {
        mGpsStatus = status;
        postInvalidate();
    }

    /**
     * 
     * @param value
     * @param places
     * @return
     */
    private double round(double value, int places) {
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    
    /**
     * 
     * @param status
     */
    public void updateLocation(Location location) {
        if(null == location) {
            mGpsStatus = null;
            return;
        }
        mLatitude = round(location.getLatitude(), 8);
        mLongitude = round(location.getLongitude(), 8);
        mAccuracy = Math.round(location.getAccuracy() * 3.28084f);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        mLastTime = sdf.format(new Date(System.currentTimeMillis()));
    }
    
    /**
     * 
     * @param num
     * @param ttf
     * @param lon
     * @param lat
     * @param accuracy
     */
    private void drawParamsText(Canvas canvas, String num, String lon, String lat, String accuracy) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);

        /*
         * Now draw stats text
         */
        canvas.drawText(mContext.getString(R.string.gps) + "(@" + mLastTime + ")" + ":" + num, 4, mFontHeight, mPaint);
        canvas.drawText(mContext.getString(R.string.Lon) + ":" + lon, 4, mFontHeight * 2, mPaint);
        canvas.drawText(mContext.getString(R.string.Lat) + ":" + lat, 4, mFontHeight * 3, mPaint);
        canvas.drawText(mContext.getString(R.string.accuracy) + ":" + accuracy, 4, mFontHeight * 4, mPaint);    	
    }

    /* (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    public void onDraw(Canvas canvas) {

        /*
         * Move the GPS circle pic on corner so we have space for text
         */
        canvas.save();
        min = Math.min(getWidth(), getHeight()) - 8;
        if(min == (getHeight() - 8)) {
            canvas.translate(getWidth() / 2 - min / 2 - 8, 0);
        }
        else {
            canvas.translate(0, getHeight() / 2 - min / 2 - 8);            
        }
        
        /*
         * Now draw the target cross hair
         */
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(getWidth() / 2 - min / 2, getHeight() / 2, getWidth() / 2 + min / 2, getHeight() / 2, mPaint);
        canvas.drawLine(getWidth() / 2, getHeight() / 2 - min / 2, getWidth() / 2, getHeight() / 2 + min / 2, mPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, min / 2, mPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, min / 4, mPaint);
        mPaint.setTextSize(min / 20);
        FontMetrics fm = mPaint.getFontMetrics();
        mFontHeight =  fm.bottom - fm.top;

        if(mGpsStatus != null) {
	        Iterable<GpsSatellite>satellites = mGpsStatus.getSatellites();
	        Iterator<GpsSatellite>sat = satellites.iterator();
	        mPaint.setColor(Color.WHITE);
	        mPaint.setStyle(Paint.Style.STROKE);
	
	        /*
	         * Now draw a circle for each satellite, use simple projections of x = sin(theta), y = cos(theta)
	         * Arm for each projection is sin(elevation). Theta = azimuth.
	         */
            int i = 0;
	        while (sat.hasNext()) {
	            i++;
	            GpsSatellite satellite = sat.next();
	            if(satellite.usedInFix()) {
	                mPaint.setColor(Color.GREEN);
	            }
	            else {
	                mPaint.setColor(Color.RED);
	            }
	            
	            double angle = Math.toRadians(satellite.getAzimuth());
	            double e = Math.cos(Math.toRadians(satellite.getElevation())) * min / 2;
	            canvas.drawCircle(
	                    (float)(getWidth() / 2 + e * Math.sin(angle)), 
	                    (float)(getHeight() / 2 - e * Math.cos(angle)),
	                    (satellite.getSnr() / 100) * min / 16,
	                    mPaint);
	        }
	        canvas.restore();

	        /*
	         * Now draw stats text
	         */
	        drawParamsText(canvas, Integer.toString(i),
	        		Double.toString(mLongitude), Double.toString(mLatitude),
	        		Integer.toString(mAccuracy));
        }
        
        else {
        	
            canvas.restore();
	        /*
	         * Now draw stats text
	         */
	        drawParamsText(canvas, "?", "?", "?", "?");
        }
    }
}