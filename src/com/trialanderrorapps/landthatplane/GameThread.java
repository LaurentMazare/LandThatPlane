package com.trialanderrorapps.landthatplane;
import android.view.*;
import android.graphics.Canvas;
import android.content.Context;
import android.util.*;

public class GameThread extends Thread {
  boolean isRunning; 
  GameEngine gameEngine;

  public GameThread(GameEngine g) {
    gameEngine = g;
    isRunning = true;
  }

  public void quit() {
    isRunning = false;
  }

  @Override
  public void run() {
    super.run();
    SurfaceHolder holder = gameEngine.getHolder();
    while (isRunning) {
      Canvas canvas = holder.lockCanvas();
      if (null != canvas) {
        gameEngine.refresh(canvas);
        holder.unlockCanvasAndPost(canvas);
      }
      try {
        sleep(40);
      }
      catch (InterruptedException e) {
      }
    }
  }
}
