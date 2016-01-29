package com.runnable;

import java.lang.Thread.State;
import java.util.Observable;

public class DoSomething extends Observable implements Runnable {
  // 此方法一经调用，立马可以通知观察者，在本例中是监听线程
  public int doBusiness(Thread t1) {
    // if (true) {
    // super.setChanged();
    // }
    if (t1.getState() == State.TERMINATED) {
      super.setChanged();
      this.notifyObservers();
      System.out.println("1");
      return 0;
    } else {
      return 1;
    }
  }

  public void run() { // 实现run方法
    for (int i = 0; i < 5; i++) { // 重复5次
      System.out.println("次线程do something");
      try {
        Thread.sleep(1000); // 休眠1秒
      } catch (Exception e) {
      }
    }
  }

  public static void main(String[] args) {
    // DoSomething dothing = new DoSomething();
    // Thread t1 = new Thread(dothing);
    // t1.start(); // 这里就是楼主提的问题，启动线程，执行上面写的run()方法
    // for (int i = 0; i < 5; i++) { // 主线程
    // System.out.println("主线程do something");
    // try {
    // // Thread.sleep(1000); // 休眠1秒
    // } catch (Exception e) {
    // }
    // }
    DoSomething dothing = new DoSomething();
    Listener listen = new Listener();
    dothing.addObserver(listen);

    Thread t1 = new Thread(dothing);
    t1.start();
    int i = 1;
    while (i > 0) {
      i = dothing.doBusiness(t1);
    }

  }

  public void save() {
    DoSomething dothing = new DoSomething();
    Thread t1 = new Thread(dothing);
    t1.start(); // 这里就是楼主提的问题，启动线程，执行上面写的run()方法
    for (int i = 0; i < 5; i++) { // 主线程
      System.out.println("主线程do something");
      try {
        // Thread.sleep(1000); // 休眠1秒
      } catch (Exception e) {
      }
    }
  }
}
