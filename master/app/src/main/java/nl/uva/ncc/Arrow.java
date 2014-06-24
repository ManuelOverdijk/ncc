package nl.uva.ncc;

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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import se.bitcraze.crazyfliecontrol.R;

/**
 * Created by terry on 20-6-14.
 */
public class Arrow extends ImageView implements View.OnTouchListener
{
    Paint paint;
    Paint fillPaint;
    int direction = 0;
    int thrust = 40;
    Bitmap bmpDevice;
    Bitmap bmpDeviceResized;
    Bitmap bmpArrow;
    Bitmap bmpArrowResized;

    int cX = 0;
    int cY = 0;

    List<oDevice> oDevices = new ArrayList<oDevice>();
    private int height = 0;
    private int width = 0;

    public Arrow(Context context)
    {
        super(context);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setStyle(Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[] { 10, 20 }, 0));

        bmpArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow2);
        bmpDevice = BitmapFactory.decodeResource(context.getResources(), R.drawable.android2);
        bmpDeviceResized = Bitmap.createScaledBitmap(bmpDevice, 50, 50, false);


        //this.setImageResource(R.drawable.arrow2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        height = this.getHeight();
        width = this.getWidth();

        if(oDevices.isEmpty())
        {
            //cX = (width - bmpArrowResized.getWidth()) / 2;
            //cY = (height - bmpArrowResized.getHeight()) / 2;

            oDevices.add(new oDevice(50,50, bmpDeviceResized, canvas.getWidth(), canvas.getHeight()));
            oDevices.add(new oDevice(50, height - 50, bmpDeviceResized, canvas.getWidth(), canvas.getHeight()));
            oDevices.add(new oDevice(width - 50, height - 50, bmpDeviceResized, canvas.getWidth(), canvas.getHeight()));
            oDevices.add(new oDevice(width - 50, 50, bmpDeviceResized, canvas.getWidth(), canvas.getHeight()));
        }
        drawDevicesOnCanvas(oDevices, canvas);

        bmpArrowResized = Bitmap.createScaledBitmap(bmpArrow, this.thrust, this.thrust, false);

        Matrix rotator = new Matrix();
        rotator.postRotate(direction, bmpArrowResized.getWidth() / 2, bmpArrowResized.getHeight() / 2);
        rotator.postTranslate(cX, cY);
        canvas.drawBitmap(bmpArrowResized, rotator, paint);


        super.onDraw(canvas);
    }

    public void setSimulation(int direction, int thrust) {
        this.direction = direction;
        this.thrust = thrust < 100 ? thrust : 100;
        //this.cX = (int) ((width/360.0) * (180 + lon));
        //this.cY = (int) ((height/180.0) * (90 - lat));
        this.invalidate();
    }

    public int getDirection() {
        return this.direction;
    }
    public int getThrust() {
        return this.thrust;
    }

    public void drawDevicesOnCanvas(List<oDevice> devicesList, Canvas canvas) {

        Path mPath = new Path();
        fillPaint = new Paint();
        fillPaint.setColor(Color.argb(255, 243,243,243));
        fillPaint.setStyle(Paint.Style.FILL);


        for (oDevice i : devicesList) {
            for (oDevice j : devicesList) {
                if (i == j) {
                    continue;
                }

                mPath.moveTo((int) i.getX(), (int) i.getY());
                mPath.lineTo((int) j.getX(), (int) j.getY());
                canvas.drawPath(mPath, paint);
            }
        }

        for (oDevice i : devicesList) {
            canvas.drawCircle((int) i.getX(), (int) i.getY(), (int) i.getWidth(), fillPaint);
            // Log.d("test", i.getX() + " " + i.getY());
            canvas.drawBitmap(bmpDeviceResized, (int) i.getX() - (int) i.getWidth() / 2, (int) i.getY() - (int) i.getHeight() / 2, null);
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {



        return true;
    }
}
