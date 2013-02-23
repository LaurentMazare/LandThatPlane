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

enum Status {FLYING, CRASHED, LANDED}

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
  long startedLanding = 0;

  Plane(float x_, float y_, float destX, float destY) {
    x = x_;
    y = y_;
    id = globalId++;
    // Add the next trajectory point
    trajectory.add(new Point(destX, destY));
  }

  synchronized void move() {
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

  synchronized void addPointToTrajectory(float xT, float yT) {
    float lastdX = dx, lastdY = dy;
    float lastX = x, lastY = y;
    Iterator t = trajectory.descendingIterator();
    if (t.hasNext()) {
      Point p = (Point)t.next();
      lastX = p.x;
      lastY = p.y;
      lastdX = p.x - x;
      lastdY = p.y - y;
      float d = sqr(xT-p.x) + sqr(yT-p.y);
      if (d < 255) return;
      if (t.hasNext()) {
        Point prevP = (Point)t.next();
        lastdX = p.x - prevP.x;
        lastdY = p.y - prevP.y;
      }
    }
    float newdX = xT - lastX, newdY = yT - lastY;
    float newN2 = sqr(newdX) + sqr(newdY);
    float lastN2 = sqr(lastdX) + sqr(lastdY);
    float dot = newdX*lastdX + newdY*lastdY;
    if (dot < 0) return;
    trajectory.add(new Point(xT, yT));
  }
  static float sqr(float x) {return x * x;}
}

class Ground implements Serializable {
  float xLeft, yLeft;
  float xRight, yRight;
  float width, w2, length;

  static float sqr(float x) {return x * x;}
  public void setup(float w, float h) {
    xLeft = w / 3;
    yLeft = h / 2;
    xRight = 2 * w / 3;
    yRight = h / 2;
    length = FloatMath.sqrt(sqr(xLeft-xRight) + sqr(yLeft - yRight));
    width = 20;
    w2 = sqr(width/2-4);
  }

  public boolean isLanding(float x, float y) {
    float lambda = (((x-xLeft)*(xRight-xLeft)) + (y-yLeft)*(yRight-yLeft)) / sqr(length);
    float xP = xLeft + lambda * (xRight - xLeft);
    float yP = yLeft + lambda * (yRight - yLeft);
    float mu2 = sqr(xP-x) + sqr(yP-y);
    if (0 < lambda && lambda < 1 && mu2 < w2) return true;
    return false;
  }
}

public class GameSession implements Serializable {
  private static long timeToLand = 3000;
  private static long timeToNextPlane = 5000;
  private static long maxPlanes = 10;

  LinkedList<Plane> planes = new LinkedList();
  int nbPlanes = 0;
  Ground ground = new Ground();
  int landedPlanes = 0, lostPlanes = 0;
  float width, height;
  long nextPlaneTime;
  Random rng = new Random();

  GameSession() {
  }

  public void start(float w, float h) {
    width = w;
    height = h;
    nextPlaneTime = SystemClock.uptimeMillis() + timeToNextPlane;
    ground.setup(w, h);
  }

  private Status getPlaneStatus(Plane p, long currentTime) {
    // Bounds detection
    if (p.x < -10 || p.y < -10 || p.x > 10+width || p.y > 10+height)
      return Status.CRASHED;
    // Collision detection
    for (Plane p2: planes) {
      if (p2.id == p.id) continue;
      float dx = p2.x - p.x, dy = p2.y - p.y;
      if (dx * dx + dy * dy < 100)
        return Status.CRASHED;
    }
    // Landing detection
    if (ground.isLanding(p.x, p.y)) {
      if (p.startedLanding == 0) p.startedLanding = currentTime;
      else if (timeToLand < currentTime - p.startedLanding)
        return Status.LANDED;
    }
    else
      p.startedLanding = 0;
    // Normal mode
    return Status.FLYING;
  }

  public void refresh(long currentTime) {
    if (nextPlaneTime < currentTime && nbPlanes < maxPlanes) {
      planes.add(randomPlane());
      nbPlanes++;
      nextPlaneTime = currentTime + timeToNextPlane;
    }

    // Move all the planes
    for (Plane p: planes) p.move();

    // Check the updated status
    LinkedList<Plane> toRemove = new LinkedList();
    for (Plane p: planes) {
      Status status = getPlaneStatus(p, currentTime);
      if (status == Status.CRASHED) {
        lostPlanes++;
        toRemove.add(p);
      }
      else if (status == Status.LANDED) {
        landedPlanes++;
        toRemove.add(p);
      }
    }
    for (Plane p: toRemove) {
      planes.remove(p);
      nbPlanes--;
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
