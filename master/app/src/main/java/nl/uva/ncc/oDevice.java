package nl.uva.ncc;

/**
 * Created by koen on 24-6-14.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.widget.ImageView;

import se.bitcraze.crazyfliecontrol.R;

/**
 * Created by terry on 20-6-14.
 */
public class oDevice
{
    private double mX;
    private double mY;

    Bitmap bmpDevice;

    public oDevice(double lon, double lat, Bitmap bmpDevice, int width, int height)
    {
        setX(width, lon);
        setY(height, lat);
        this.bmpDevice = bmpDevice;
    }

    public Bitmap getBmpDevice() {
        return bmpDevice;
    }

    public void setBmpDevice(Bitmap bmpDevice) {
        this.bmpDevice = bmpDevice;
    }

    public double getWidth() {
        return this.bmpDevice.getWidth();
    }

    public double getHeight() {
        return this.bmpDevice.getHeight();
    }

    public double getX() {
        return mX;
    }

    public void setX(int width, double lon) {
        //this.mX = (int) ((width/360.0) * (180 + lon));
        this.mX = lon;
    }

    public double getY() {
        return mY;
    }

    public void setY(int height, double lat) {
        //this.mY = (int) ((height/180.0) * (90 - lat));
        this.mY = lat;
    }
}
