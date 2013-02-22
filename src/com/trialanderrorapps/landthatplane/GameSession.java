package com.trialanderrorapps.landthatplane;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.GestureDetector.*;

import java.io.Serializable;
import java.util.*;

class Plane implements Serializable {
  float x, y, dx, dy;
  static long globalId = 0;
  long id;
  Plane(float x_, float y_, float dx_, float dy_) {
    x = x_;
    y = y_;
    dx = dx_;
    dy = dy_;
    id = globalId++;
  }

  void refresh() {
    x += dx;
    y += dy;
  }
}

class Ground implements Serializable {
  float runawayXLeft, runawayYLeft;
  float runawayXRight, runawayYRight;
  float width, length;


  static float sqr(float x) {return x * x;}
  public void setup(float w, float h) {
    runawayXLeft = w / 4;
    runawayYLeft = h / 2;
    runawayXRight = 3 * w / 4;
    runawayYRight = h / 2;
    width = 30;
    length = FloatMath.sqrt(sqr(runawayXLeft-runawayXRight) + sqr(runawayYLeft - runawayYRight));
  }
}

public class GameSession implements Serializable {
  LinkedList<Plane> planes = new LinkedList();
  Ground ground = new Ground();
  int savedPlanes = 0, lostPlanes = 0;
  float width, height;
  long nextPlaneTime;
  Random rng = new Random();

  GameSession() {
  }

  public void start(float w, float h) {
    width = w;
    height = h;
    nextPlaneTime = SystemClock.uptimeMillis() + 5000;
    ground.setup(w, h);
  }

  public void refresh(long currentTime) {
    if (nextPlaneTime < currentTime) {
      planes.add(randomPlane());
      nextPlaneTime = currentTime + 5000;
    }
    LinkedList<Plane> toRemove = new LinkedList();
    for (Plane p: planes) {
      p.refresh();
      if (p.x < 0 || p.y < 0 || p.x > width || p.y > height) toRemove.add(p);
    }
    for (Plane p: toRemove) {
      lostPlanes++;
      planes.remove(p);
    }
  }

  private Plane randomPlane() {
    float pos = rng.nextFloat() * (2 * width + 2 * height);
    float x, y;
    if (pos > 2*width + height) {
      x = 0;
      y = pos - 2*width - height;
    }
    else if (pos > 2*width) {
      x = width;
      y = pos - 2*width;
    }
    else if (pos > width) {
      x = pos - width;
      y = 0;
    }
    else {
      x = pos;
      y = height;
    }
    float dx = width/2 - x;
    float dy = height/2 - y;
    float dnorm = FloatMath.sqrt(dx*dx + dy*dy);
    return new Plane(x, y, dx/dnorm, dy/dnorm);
  }

  public long nearestPlane(float x, float y) {
    for (Plane p: planes) {
      float dx = p.x - x;
      float dy = p.y - y;
      if (dx*dx + dy*dy < 30 * 30) return p.id;
    }
    return -1;
  }

  public Plane getPlaneById(long id) {
    for (Plane p: planes)
      if (p.id == id) return p;
    return null;
  }
}
