package com.trialanderrorapps.landthatplane;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.*;

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
    Paint paint = new Paint();
    paint.setColor(Color.RED);
    paint.setAntiAlias(true);
    canvas.drawColor(Color.BLACK);
    canvas.drawCircle(30, 30, 15, paint);
  }
}

public class GameEngine {
  GamePanel gamePanel;

  public GameEngine(Context context) {
    gamePanel = new GamePanel(this, context);
  }
  
  public void refresh(Canvas canvas) {
    gamePanel.refresh(canvas);
  }

  public SurfaceHolder getHolder() {
    return gamePanel.getHolder();
  }

  public GamePanel getPanel() {
    return gamePanel;
  }
}


