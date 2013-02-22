package com.trialanderrorapps.landthatplane;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.util.*;
import android.view.*;
import android.view.GestureDetector.*;

import java.io.Serializable;
import java.util.*;

class Plane implements Serializable {
  float x, y, dx, dy;
  Plane(float x_, float y_, float dx_, float dy_) {
    x = x_;
    y = y_;
    dx = dx_;
    dy = dy_;
  }

  void refresh() {
    x += dx;
    y += dy;
  }
}

public class GameSession implements Serializable {
  LinkedList<Plane> planes = new LinkedList();
  float width, height;
  long nextPlaneTime;
  Random rng = new Random();

  GameSession() {
  }

  public void start(float w, float h) {
    width = w;
    height = h;
    nextPlaneTime = System.currentTimeMillis() + 5000;
  }

  public void refresh() {
    long currentTime = System.currentTimeMillis();
    if (nextPlaneTime < currentTime) {
      planes.add(randomPlane());
      nextPlaneTime = currentTime + 5000;
    }
    LinkedList<Plane> toRemove = new LinkedList();
    for (Plane p: planes) {
      p.refresh();
      if (p.x < 0 || p.y < 0 || p.x > width || p.y > height) toRemove.add(p);
    }
    for (Plane p: toRemove) planes.remove(p);
  }

  private Plane randomPlane() {
    float pos = rng.nextFloat() * (2 * width + 2 * height);
    float x, y;
    if (pos > 2*width + height) {
      x = 0;
      y = pos - 2*width + height;
    }
    else if (pos > 2*width) {
      x = width;
      y = pos - 2*width + height;
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
}
