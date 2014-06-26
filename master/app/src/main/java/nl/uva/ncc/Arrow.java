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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import se.bitcraze.crazyfliecontrol.R;

/**
 * Created by terry on 20-6-14.
 */
public class Arrow extends View
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
    int xTrans = 10;
    int yTrans = 10;
    int targetX = 0;
    int targetY = 0;
    int stepSizeX = 0;
    int stepSizeY = 0;

    List<oDevice> oDevices = new ArrayList<oDevice>();
    private int height = 0;
    private int width = 0;

    ArrayList<String> mNames;
    ArrayList<Double> mSlavesLat;
    ArrayList<Double> mSlavesLon;

    public Arrow(Context context, ArrayList<String> slaveNames, ArrayList<Double> slavesLat,
                 ArrayList<Double> slavesLon)
    {
        super(context);

        mNames = slaveNames;
        mSlavesLat = slavesLat;
        mSlavesLon = slavesLon;

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setStyle(Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[] { 10, 20 }, 0));

        bmpArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.abc_ab_bottom_solid_dark_holo);
        bmpDevice = BitmapFactory.decodeResource(context.getResources(), R.drawable.abc_ic_clear_disabled);
        bmpDeviceResized = Bitmap.createScaledBitmap(bmpDevice, 50, 50, false);

//        Button btn = new Button(this);
//        btn.setText("press this button");
//        btn.setId(1);


        //this.setImageResource(R.drawable.arrow2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        height = this.getHeight();
        width = this.getWidth();

        if(oDevices.isEmpty())
        {
            Random r = new Random();
            int numberOfDevices = r.nextInt(3) + 3;

            bmpArrowResized = Bitmap.createScaledBitmap(bmpArrow, this.thrust, this.thrust, false);

            targetX = (int)(r.nextDouble()*width);
            targetY = (int)(r.nextDouble()*height);
            oDevices.add(new oDevice(targetX, targetY,
                    bmpArrowResized, width, height));
            for(int i = 0; i < numberOfDevices; i++) {
//                oDevices.add(new oDevice(mSlavesLon.get(i), mSlavesLat.get(i), bmpDeviceResized,
//                        canvas.getWidth(), canvas.getHeight()));
                  oDevices.add(new oDevice(r.nextDouble()*width, r.nextDouble()*height,
                          bmpDeviceResized, width, height));
            }

            this.targetX = 400;
            this.targetY = 600;
        }

        try {
            oDevices.set(0, changeDirection(oDevices.get(0), targetX, targetY));
            drawDevicesOnCanvas(oDevices, canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
        invalidate();
//        bmpArrowResized = oDevices.get(0).getBmpDevice();
//
//        Matrix rotator = new Matrix();
//        rotator.reset();
//        rotator.postTranslate((float)oDevices.get(0).getX(), (float)oDevices.get(0).getY());
//        rotator.postRotate(direction, bmpArrowResized.getWidth() / 2, bmpArrowResized.getHeight() / 2);
//        //rotator.postTranslate(cX, cY);
//        //canvas.drawBitmap(bmpArrowResized, rotator, paint);

    }


    private oDevice changeDirection(oDevice drone, int tarX, int tarY) {
//        int diffX = tarX - (int)drone.getX();
//        int diffY = tarY - (int)drone.getY();
//        int stepSizeX = 1, stepSizeY = 1;
//
//        if(diffX > diffY) {
//            stepSizeY = (int)(diffY / diffX);
//        }
//        else
//        {
//            stepSizeX = (int)(diffX / diffY);
//        }
//
//        int stepSize = 5;
        drone.setX(0, stepSizeX + drone.getX());
        drone.setY(0, stepSizeY + drone.getY());

        return drone;
    }

    private void setTarget(int tX, int tY)
    {
        this.targetX = tX;
        this.targetY = tY;
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

                if (i.getBmpDevice() == bmpArrowResized

                    && i.getX() < targetX + i.getWidth()
                    && i.getX() > targetX - i.getWidth()

                    && i.getY() < targetY + j.getHeight()
                    && i.getY() > targetY - j.getHeight())
                {
                    stepSizeX = 0;
                    stepSizeY = 0;

                    continue;
                }

                if (j.getBmpDevice() == bmpArrowResized

                    && j.getX() < targetX + j.getWidth()
                    && j.getX() > targetX - j.getWidth()

                    && j.getY() < targetY + i.getHeight()
                    && j.getY() > targetY - i.getHeight())
                {
                    stepSizeX = 0;
                    stepSizeY = 0;

                    continue;
                }

                if (j.getBmpDevice() == bmpArrowResized
                    || i.getBmpDevice() == bmpArrowResized)
                {
                    continue;
                }


                mPath.moveTo((int) i.getX(), (int) i.getY());
                mPath.lineTo((int) j.getX(), (int) j.getY());
                canvas.drawPath(mPath, paint);
            }
        }

        devicesList.add(devicesList.get(0));
        for (oDevice i : devicesList.subList(1, devicesList.size() - 1)) {
            canvas.drawCircle((int) i.getX(), (int) i.getY(), (int) i.getWidth(), fillPaint);
            Log.d("test", i.getX() + " " + i.getY());
            canvas.drawBitmap(i.getBmpDevice(), (int) i.getX() - (int) i.getWidth() / 2, (int) i.getY() - (int) i.getHeight() / 2, null);
            //String posText = i.getX() + ", " + i.getY();
            //canvas.drawText(posText, 0, posText.length() - 1, (float)i.getX(), (float)(i.getY() + i.getHeight()), null);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("touch", "touch");
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                targetX = (int)event.getX();
                targetY = (int)event.getY();
                int currentX = (int)oDevices.get(0).getX();
                int currentY = (int)oDevices.get(0).getY();

                int diffX = targetX - currentX;
                int diffY = targetY - currentY;

                if(Math.abs(targetX - currentX) > Math.abs(targetY - currentY)) {
                    if(diffX > 0) {
                        stepSizeX = 5;
                    } else {
                        stepSizeX = -5;
                    }

                    stepSizeY = stepSizeX * (targetY - currentY)/(targetX - currentX);

                } else {
                    if(diffY > 0) {
                        stepSizeY = 5;
                    } else {
                        stepSizeY = -5;
                    }
                    stepSizeX = stepSizeY * (targetX - currentX)/(targetY - currentY);
                }
//                stepSizeX = (targetX - currentX)/100;
//                stepSizeY = (targetY - currentY)/100;
                Log.d("stepsizes", stepSizeX + " " + stepSizeY);
                invalidate();
                break;
        }
        return true;
    }
}
