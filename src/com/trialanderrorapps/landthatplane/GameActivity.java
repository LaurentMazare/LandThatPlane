package com.trialanderrorapps.landthatplane;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.GestureDetector.*;

public class GameActivity extends Activity implements OnGestureListener {
  private GestureDetector gestureDetector;
  private GameEngine gameEngine;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    gameEngine = new GameEngine(this);
    setContentView(gameEngine.gamePanel);
    gestureDetector = new GestureDetector(this, this);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return (gestureDetector.onTouchEvent(event));
  }

  @Override
  public boolean onSingleTapUp(MotionEvent ev) {
    return true;
  }
  @Override
  public void onShowPress(MotionEvent ev) {
  }
  @Override
  public void onLongPress(MotionEvent ev) {
  }
  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
    return true;
  }
  @Override
  public boolean onDown(MotionEvent ev) {
    return true;
  }
  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return true;
  }
  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }
  @Override
  public void onPause() {
    super.onPause();
  }
}
