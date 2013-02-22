package com.trialanderrorapps.landthatplane;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.*;

class PlaneRenderer{
  private static Paint paint = new Paint();
  public static void draw(Plane p, Canvas canvas) {
    paint.setColor(Color.RED);
    paint.setAntiAlias(true);
    canvas.drawCircle(p.x, p.y, 10, paint);
  }
}

class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
  GameThread gameThread;
  GameEngine gameEngine;

  public GamePanel(GameEngine g, Context context) {
    super(context);
    gameEngine = g;
    getHolder().addCallback(this);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    gameThread = new GameThread(gameEngine);
    gameThread.start();
    gameEngine.start(getWidth(), getHeight());
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
    for (Plane p: gameEngine.gameSession.planes)
      PlaneRenderer.draw(p, canvas);
  }
}

public class GameEngine {
  GameSession gameSession = new GameSession();
  GamePanel gamePanel;
  boolean isPaused = false;

  public GameEngine(Context context) {
    gamePanel = new GamePanel(this, context);
  }
  
  public void refresh(Canvas canvas) {
    if (isPaused) return;
    gameSession.refresh();
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
}


