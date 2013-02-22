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

class Point implements Serializable {
  float x;
  float y;
  Point(float x_, float y_) {
    x = x_;
    y = y_;
  }
}

class Plane implements Serializable {
  static long globalId = 0;
  static float speed = 1;
  float x, y;
  float dx, dy;
  long id;
  LinkedList<Point> trajectory = new LinkedList();

  Plane(float x_, float y_, float destX, float destY) {
    x = x_;
    y = y_;
    id = globalId++;
    // Add the next trajectory point
    trajectory.add(new Point(destX, destY));
  }

  synchronized void refresh() {
    int toRemove = 0;
    float totalDist;
    float distToGo = speed;
    float dist = 0;
    for (Point p: trajectory) {
      dist = FloatMath.sqrt(sqr(p.x-x) + sqr(p.y-y));
      if (distToGo < dist) break;
      toRemove++;
      distToGo -= dist;
      x = p.x;
      y = p.y;
    }
    for (int i = 0; i < toRemove; i++) trajectory.removeFirst();
    if (dist > 0 && !trajectory.isEmpty()) {
      Point p = trajectory.getFirst();
      dx = (p.x - x) * distToGo / dist;
      dy = (p.y - y) * distToGo / dist;
    }
    x += dx;
    y += dy;
  }

  synchronized void addPointToTrajectory(float x, float y) {
    if (!trajectory.isEmpty()) {
      Point p = trajectory.getLast();
      float d = sqr(x-p.x) + sqr(y-p.y);
      if (500 < d) return;
    }
    trajectory.add(new Point(x, y));
  }
  static float sqr(float x) {return x * x;}
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
    return new Plane(x, y, width/2, height/2);
  }

  public long nearestPlane(float x, float y) {
    float minDist = -1;
    long id = -1;
    for (Plane p: planes) {
      float dx = p.x - x;
      float dy = p.y - y;
      float dist = dx*dx + dy*dy;
      if (minDist < 0 || dist < minDist) {
        minDist = dist;
        id = p.id;
      }
    }
    if (30 * 30 < minDist) id = -1;
    return id;
  }

  public Plane getPlaneById(long id) {
    for (Plane p: planes)
      if (p.id == id) return p;
    return null;
  }
}
