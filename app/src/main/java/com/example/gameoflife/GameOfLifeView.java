package com.example.gameoflife;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;

public class GameOfLifeView extends SurfaceView implements Runnable {
    GestureDetector gestureDetector;
    public static final int DEFAULT_SIZE = 50;
    public static final int DEFAULT_ALIVE_COLOR = Color.WHITE;
    public static final int DEFAULT_DEAD_COLOR = Color.BLACK;
    private Thread thread;
    private boolean isRunning;
    private int columnWidth = 1;
    private int rowHeight = 1;
    private int nbColumns = 1;
    private int nbRows = 1;
    private World world;
    private boolean isPaused = false;
    private final Rect r = new Rect();
    private final Paint p = new Paint();
    public GameOfLifeView(Context context) {
        super(context);
        initWorld();
    }
    public GameOfLifeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureListener());
        initWorld();
    }
    @Override
    public void run() {
        while (isRunning) {
            if (!getHolder().getSurface().isValid()) {
                continue;
            }
            try {
                Thread.sleep(300);
            } catch (Exception ignored) {

            }
            Canvas canvas = getHolder().lockCanvas();
            if (!isPaused)
                world.nextGeneration();
            drawCells(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
    public void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }
    public void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException ignore) {}
    }
    public void initWorld() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        nbColumns = point.x / DEFAULT_SIZE;
        nbRows = point.y / DEFAULT_SIZE;
        columnWidth = point.x / nbColumns;
        rowHeight = point.y / nbRows;
        world = new World(nbColumns, nbRows);
    }
    private void drawCells(Canvas canvas) {
         for (int i = 0; i < nbColumns; i++) {
             for (int j = 0; j < nbRows; j++) {
                 Cell cell = world.get(i, j);
                 r.set((cell.x * columnWidth) - 1, (cell.y * rowHeight) - 1,
                         (cell.x * columnWidth + columnWidth) - 1, (cell.y * rowHeight + rowHeight) - 1);
                 p.setColor(cell.alive ? DEFAULT_ALIVE_COLOR : DEFAULT_DEAD_COLOR);
                 canvas.drawRect(r, p);
             }
         }
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {

            int i = (int) (event.getX() / columnWidth);
            int j = (int) (event.getY() / columnWidth);
            Cell cell = world.get(i, j);
            cell.invert();
            invalidate();

            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isPaused = !isPaused;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            world.init(velocityY > 0);

            return true;
        }
    }
}