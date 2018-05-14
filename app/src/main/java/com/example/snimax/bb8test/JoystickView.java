package com.example.snimax.bb8test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by snimax on 27-Feb-17.
 */

public class JoystickView extends SurfaceView implements  SurfaceHolder.Callback, View.OnTouchListener {

    private float centerX; // Center of screen
    private float centerY;
    private float centerXR, centerYR; // RIGHT controller
    private float centerXL, centerYL; // LEFT controller
    private float baseRadius;
    private float hatRadius;
    private float oldXR = centerXR;
    private float oldYR = centerYR;
    private float oldXL = centerXL;
    private float oldYL = centerYL;
    private float buttonRadius;
    private float LButtonY;
    private float LButtonX;
    private float RButtonY;
    private float RButtonX;
    private int mActivePointerId;
    public JoystickListener joystickCallback;

    private void setupDimensions() {
        centerX = getWidth() / 2;
        centerY = getHeight()/2;
        centerXR = (float) (getWidth()*0.80);
        centerYR = getHeight() / 2;
        centerXL = (float) (getWidth() * 0.20);
        centerYL = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) / 5;
        hatRadius = Math.min(getWidth(), getHeight()) / 9;
        LButtonY = (float) (getHeight()/2 * 0.7);
        LButtonX = (float) getWidth()/2;
        RButtonY = (float) (getHeight()/2  * 1.3);
        RButtonX = (float) getWidth()/2;
        buttonRadius = Math.min(getWidth(), getHeight()) / 12;
    }

    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener){
            joystickCallback = (JoystickListener) context;
        }
    }

    public JoystickView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener){
            joystickCallback = (JoystickListener) context;
        }
    }

    public JoystickView (Context context, AttributeSet attributes) {
        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener){
            joystickCallback = (JoystickListener) context;
        }
    }
    private void drawButtons(Canvas canvas, Paint pColor){
        pColor.setARGB(255,230,230,230);
        canvas.drawCircle(LButtonX, LButtonY, buttonRadius, pColor);
        pColor.setARGB(255,200,200,200);
        canvas.drawCircle(RButtonX, RButtonY, buttonRadius, pColor);
    }
    private void drawJoystickR(Canvas canvas, Paint pColor, float newX, float newY) {
        pColor.setARGB(255, 80, 80, 80); //base color: dark grey
        canvas.drawCircle(centerXR, centerYR, baseRadius, pColor);
        pColor.setARGB(255, 255, 140, 0); // inner color: dark orange
        canvas.drawCircle(newX, newY, hatRadius, pColor);
    }

    private void drawJoystickL(Canvas canvas, Paint pColor, float newX, float newY) {
        pColor.setARGB(255, 80, 80, 80); //base color: dark grey
        canvas.drawCircle(centerXL, centerYL, baseRadius, pColor);
        pColor.setARGB(255, 255, 140, 0); // inner color: dark orange
        canvas.drawCircle(newX, newY, hatRadius, pColor); // TODO : might need to fix this line? / 3 Didnt catch that...

    }

    //NEW FUNCTION
    private void drawJoysticks(int stick, float newX, float newY) {
        if(!getHolder().getSurface().isValid()) return;
        Canvas myCanvas = this.getHolder().lockCanvas();
        Paint pColor = new Paint();
        myCanvas.drawColor(Color.GRAY, PorterDuff.Mode.SRC);
        if (stick == 1) { // Update left stick position
            drawJoystickR(myCanvas, pColor, oldXR, oldYR);
            drawJoystickL(myCanvas, pColor, newX, newY);
            drawButtons(myCanvas, pColor);
            oldXL = newX;
            oldYL = newY;
        } else if (stick == 2) { // Update right stick position
            drawJoystickL(myCanvas, pColor, oldXL, oldYL);
            drawJoystickR(myCanvas, pColor, newX, newY);
            drawButtons(myCanvas, pColor);
            oldXR = newX;
            oldYR = newY;
        } else { // creation of draw ; Could be 0 or w/e
            drawJoystickR(myCanvas, pColor, centerXR, centerYR);
            drawJoystickL(myCanvas, pColor, centerXL, centerYL);
            drawButtons(myCanvas, pColor);
            oldXR = centerXR;
            oldYR = centerYR;
            oldXL = centerXL;
            oldYL = centerYL;
        }
        getHolder().unlockCanvasAndPost(myCanvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawJoysticks(0, 0, 0);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public boolean onTouch(View v, MotionEvent e){
        float displacement;
        float xToSend = 0;
        float yToSend = 0;
        float buttonInp = 0;
        final int pointerCount = e.getPointerCount();
        for (int p = 0; p < pointerCount; p++) {
            mActivePointerId = e.getPointerId(p);

            int pointerIndex = e.findPointerIndex(mActivePointerId);

            if (!v.equals(this)) return false;
            if (e.getAction() == e.ACTION_UP) {
                drawJoysticks(0, 0, 0);
                joystickCallback.onJoystickMoved(0, 0, 0, getId()); // getID() ? How shall this represent which joystick being moved? (Maybe need to rethink?)
                return true;
            }

            if (e.getX(pointerIndex) > centerX*1.2) {
                displacement = (float) Math.sqrt((Math.pow(e.getX(pointerIndex) - centerXR, 2)) + Math.pow(e.getY(pointerIndex) - centerYR, 2));
                double angle = Math.PI / 4;

                if (displacement < baseRadius) {
                    drawJoysticks(2, e.getX(pointerIndex), centerYR);
                    xToSend = (e.getX(pointerIndex) - centerXR) / baseRadius;
                } else {
                    float ratio = baseRadius / displacement;
                    float constrainedX = centerXR + (e.getX(pointerIndex) - centerXR) * ratio;
                    float constrainedY = centerYR + (e.getY(pointerIndex) - centerYR) * ratio;
                    drawJoysticks(2, constrainedX, centerYR);
                    xToSend = (constrainedX - centerXR) / baseRadius;
                }

            } else if (e.getX(pointerIndex) < centerX*0.8) {
                displacement = (float) Math.sqrt((Math.pow(e.getX(pointerIndex) - centerXL, 2)) + Math.pow(e.getY(pointerIndex) - centerYL, 2));
                double angle = Math.PI / 4;

                if (displacement < baseRadius) {
                    drawJoysticks(1, centerXL, e.getY(pointerIndex));
                    yToSend = -(e.getY(pointerIndex) - centerYL) / baseRadius;

                } else {
                    float ratio = baseRadius / displacement;
                    float constrainedX = centerXL + (e.getX(pointerIndex) - centerXL) * ratio;
                    float constrainedY = centerYL + (e.getY(pointerIndex) - centerYL) * ratio;
                    drawJoysticks(1, centerXL, constrainedY);
                    yToSend = -(constrainedY - centerYL) / baseRadius;
                }

            } else if(e.getX(pointerIndex) > centerX*0.8 && e.getX(pointerIndex) < centerX*1.2 && e.getY(pointerIndex) > centerY){
                buttonInp = -1;

            } else if(e.getX(pointerIndex) > centerX*0.8 && e.getX(pointerIndex) < centerX*1.2 && e.getY(pointerIndex) < centerY){
                buttonInp = 1;

            }
        }
        joystickCallback.onJoystickMoved(xToSend, yToSend, buttonInp, getId());
        return true;
    }

    public interface JoystickListener{
        void onJoystickMoved(float xPercent, float yPercent, float buttonInp, int id);
    }

}
