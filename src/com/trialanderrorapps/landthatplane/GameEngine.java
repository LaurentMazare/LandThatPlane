package com.trialanderrorapps.landthatplane;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.GestureDetector.*;

enum Mode {PAUSED, PLAYING, GAMEOVER, DEMO}

class PlaneRenderer{
  private static Paint trajectoryPaint = new Paint();
  private static Paint strokePaint = new Paint();
  private static Paint fillPaint = new Paint();

  public static void init() {
    trajectoryPaint.setAntiAlias(true);
    trajectoryPaint.setColor(Color.LTGRAY);
    trajectoryPaint.setStrokeWidth(0);
    trajectoryPaint.setAlpha(200);
    strokePaint.setAntiAlias(true);
    strokePaint.setColor(android.graphics.Color.BLACK);
    strokePaint.setStyle(Paint.Style.STROKE);
    strokePaint.setStrokeWidth(2);
    fillPaint.setAntiAlias(true);
    fillPaint.setAlpha(255);
  }

  public static void drawTrajectory(Plane plane, Canvas canvas) {
    float lastX = plane.x;
    float lastY = plane.y;
    int i = 0;
    synchronized(plane) {
      for (Point p: plane.trajectory) {
        float x = p.x;
        float y = p.y;
        if (x > GamePanel.width || x < 0 || y > GamePanel.height || y < 0) break;
        canvas.drawLine(lastX, lastY, x, y, trajectoryPaint);
        lastX = x;
        lastY = y;
      }
    }
  }
  public static void draw(Plane plane, Canvas canvas, boolean selected) {
    if (selected)
      fillPaint.setColor(Color.RED);
    else if (0 < plane.startedLanding)
      fillPaint.setColor(Color.GREEN);
    else
      fillPaint.setColor(Color.LTGRAY);
    float x = plane.x, y = plane.y, dx = plane.dx, dy = plane.dy;
    float dN = FloatMath.sqrt(dx*dx+dy*dy);
    float cosT = dx / dN, sinT = dy / dN;
    Path path = new Path();
    path.setFillType(Path.FillType.EVEN_ODD);
    path.moveTo(x+cosT*10, y+sinT*10);
    path.lineTo(x+cosT*(-10)-sinT*(-10), y+sinT*(-10)+cosT*(-10));
    path.lineTo(x+cosT*(-5), y+sinT*(-5));
    path.lineTo(x+cosT*(-10)-sinT*(10), y+sinT*(-10)+cosT*(10));
    path.close();
    canvas.drawPath(path, fillPaint);
    canvas.drawPath(path, strokePaint);
  }
}

class GroundRenderer{
  private static Paint paint = new Paint();
  public static void draw(Ground g, Canvas canvas) {
    paint.setColor(Color.GREEN);
    paint.setAntiAlias(true);
    paint.setStrokeWidth(2);
    float dx1 = (g.yLeft - g.yRight) / g.length * g.width / 2;
    float dy1 = (g.xRight - g.xLeft) / g.length * g.width / 2;
    float x1 = g.xLeft + dx1;
    float y1 = g.yLeft + dy1;
    float x2 = g.xLeft - dx1;
    float y2 = g.yLeft - dy1;
    float x3 = g.xRight - dx1;
    float y3 = g.yRight - dy1;
    float x4 = g.xRight + dx1;
    float y4 = g.yRight + dy1;
    canvas.drawLine(x1, y1, x2, y2, paint);
    canvas.drawLine(x2, y2, x3, y3, paint);
    canvas.drawLine(x3, y3, x4, y4, paint);
    canvas.drawLine(x4, y4, x1, y1, paint);
  }
}

class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
  static float width;
  static float height;
  Paint textPaint = new Paint();
  GameThread gameThread;
  GameEngine gameEngine;
  GameSession gameSession;

  public GamePanel(GameEngine g, Context context) {
    super(context);
    gameEngine = g;
    gameSession = g.gameSession;
    getHolder().addCallback(this);
    textPaint.setTextSize(20);
    textPaint.setAntiAlias(true);
    textPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));
    textPaint.setAlpha(200);
    textPaint.setColor(Color.DKGRAY);
    PlaneRenderer.init();
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
    // Render background in white
    canvas.drawColor(Color.WHITE);
    // Render the ground
    GroundRenderer.draw(gameSession.ground, canvas);
    // Render the planes
    for (Plane p: gameSession.planes)
      PlaneRenderer.drawTrajectory(p, canvas);
    for (Plane p: gameSession.planes)
      PlaneRenderer.draw(p, canvas, p.id == gameEngine.selectedPlaneId);
    // Render the score / nb of planes left
    String statusStr;
    if (gameEngine.mode == Mode.GAMEOVER)
      statusStr = String.format("!!!GAME OVER!!! %d PLANES SAVED.", gameSession.landedPlanes);
    else
      statusStr = String.format("LANDED %2d   CRASHED %d", gameSession.landedPlanes, gameSession.lostPlanes);
    canvas.drawText(statusStr, 25, 25, textPaint);
  }
}

public class GameEngine {
  GameSession gameSession = new GameSession();
  GamePanel gamePanel;
  Mode mode = Mode.PLAYING;
  long selectedPlaneId = -1;
  long lastEventTime = 0, lastDownTime = 0;

  public GameEngine(Context context) {
    gamePanel = new GamePanel(this, context);
  }
  
  public void refresh(Canvas canvas) {
    if (3 < gameSession.lostPlanes) mode = Mode.GAMEOVER;
    if (mode == Mode.PLAYING) {
      long currentTime = SystemClock.uptimeMillis();
      if (currentTime > lastEventTime + 1000) selectedPlaneId = -1;
      gameSession.refresh(currentTime);
    }
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
    if (mode != Mode.PLAYING) return;
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
    if (mode != Mode.PLAYING) return;
    if (downTime != lastDownTime || selectedPlaneId == -1) return;
    Plane p = gameSession.getPlaneById(selectedPlaneId);
    if (p == null) return;
    lastEventTime = time;
    p.addPointToTrajectory(x, y);
  }
}


