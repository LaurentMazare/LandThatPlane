package com.trialanderrorapps.landthatplane;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.*;

class PlaneRenderer{
  private static Paint paint = new Paint();
  public static void drawTrajectory(Plane plane, Canvas canvas) {
    paint.setAntiAlias(true);
    float lastX = plane.x;
    float lastY = plane.y;
    paint.setColor(Color.LTGRAY);
    paint.setStrokeWidth(0);
    paint.setAlpha(200);
    int i = 0;
    synchronized(plane) {
      for (Point p: plane.trajectory) {
        float x = p.x;
        float y = p.y;
        if (x > GamePanel.width || x < 0 || y > GamePanel.height || y < 0) break;
        canvas.drawLine(lastX, lastY, x, y, paint);
        lastX = x;
        lastY = y;
      }
    }
  }
  public static void draw(Plane plane, Canvas canvas, boolean selected) {
    paint.setAntiAlias(true);
    paint.setAlpha(255);
    if (selected)
      paint.setColor(Color.RED);
    else
      paint.setColor(Color.BLUE);
    canvas.drawCircle(plane.x, plane.y, 10, paint);
  }
}

class GroundRenderer{
  private static Paint paint = new Paint();
  public static void draw(Ground g, Canvas canvas) {
    paint.setColor(Color.GREEN);
    paint.setAntiAlias(true);
    paint.setStrokeWidth(2);
    float dx1 = (g.runawayYLeft - g.runawayYRight) / g.length * g.width / 2;
    float dy1 = (g.runawayXRight - g.runawayXLeft) / g.length * g.width / 2;
    float x1 = g.runawayXLeft + dx1;
    float y1 = g.runawayYLeft + dy1;
    float x2 = g.runawayXLeft - dx1;
    float y2 = g.runawayYLeft - dy1;
    float x3 = g.runawayXRight - dx1;
    float y3 = g.runawayYRight - dy1;
    float x4 = g.runawayXRight + dx1;
    float y4 = g.runawayYRight + dy1;
    canvas.drawLine(x1, y1, x2, y2, paint);
    canvas.drawLine(x2, y2, x3, y3, paint);
    canvas.drawLine(x3, y3, x4, y4, paint);
    canvas.drawLine(x4, y4, x1, y1, paint);
  }
}

class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
  static float width;
  static float height;
  GameThread gameThread;
  GameEngine gameEngine;
  GameSession gameSession;

  public GamePanel(GameEngine g, Context context) {
    super(context);
    gameEngine = g;
    gameSession = g.gameSession;
    getHolder().addCallback(this);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    width = getWidth();
    height = getHeight();
    gameThread = new GameThread(gameEngine);
    gameThread.start();
    gameEngine.start(width, height);
  }
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  }
  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    gameThread.quit();
    while (true) {
      try {
        gameThread.join();
        break;
      }
      catch(Exception e) {
      }
    }
  }

  public void refresh(Canvas canvas) {
    canvas.drawColor(Color.WHITE);
    GroundRenderer.draw(gameSession.ground, canvas);
    for (Plane p: gameSession.planes)
      PlaneRenderer.drawTrajectory(p, canvas);
    for (Plane p: gameSession.planes)
      PlaneRenderer.draw(p, canvas, p.id == gameEngine.selectedPlaneId);
  }
}

public class GameEngine {
  GameSession gameSession = new GameSession();
  GamePanel gamePanel;
  boolean isPaused = false;
  long selectedPlaneId = -1;
  long lastEventTime = 0, lastDownTime = 0;

  public GameEngine(Context context) {
    gamePanel = new GamePanel(this, context);
  }
  
  public void refresh(Canvas canvas) {
    if (isPaused) return;
    long currentTime = SystemClock.uptimeMillis();
    if (currentTime > lastEventTime + 1000) selectedPlaneId = -1;
    gameSession.refresh(currentTime);
    gamePanel.refresh(canvas);
  }

  public SurfaceHolder getHolder() {
    return gamePanel.getHolder();
  }

  public GamePanel getPanel() {
    return gamePanel;
  }

  void start(float width, float height) {
    gameSession.start(width, height);
  }

  void onDown(float x, float y, long time) {
    lastDownTime = time;
    lastEventTime = time;
    selectedPlaneId = gameSession.nearestPlane(x, y);
    Plane p = gameSession.getPlaneById(selectedPlaneId);
    if (p == null) return;
    synchronized(p) {
      p.trajectory.clear();
    }
  }

  void onScroll(float x, float y, long time, long downTime) {
    if (downTime != lastDownTime || selectedPlaneId == -1) return;
    Plane p = gameSession.getPlaneById(selectedPlaneId);
    if (p == null) return;
    lastEventTime = time;
    p.addPointToTrajectory(x, y);
  }
}


