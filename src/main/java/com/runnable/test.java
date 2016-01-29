package com.runnable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class test {

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    MyThread1 myThread1 = new MyThread1("Cain");
    thread1 thread1 = new thread1();
    thread1 thread11 = new thread1();
    thread2 thread2 = new thread2();
    myThread1.start();
    thread11.start();
    thread1.start();
    thread2.start();

    // 创建一个线程池
    ExecutorService pool = Executors.newFixedThreadPool(1);
    // 创建两个有返回值的任务
    Callable c1 = new MyCallable("A");
    Callable c2 = new MyCallable("B");
    // 执行任务并获取Future对象
    Future f1 = pool.submit(c1);
    Future f2 = pool.submit(c2);
    // 从Future对象上获取任务的返回值，并输出到控制台
    System.out.println(">>>" + f1.get().toString());
    System.out.println(">>>" + f2.get().toString());
    // 关闭线程池
    pool.shutdown();
  }
}

class MyCallable implements Callable {
  private String oid;

  MyCallable(String oid) {
    this.oid = oid;
  }

  public Object call() throws Exception {
    return this.oid + "任务返回的内容";
  }
}

class MyThread1 extends Thread {
  private String name;

  public MyThread1(String name) {
    this.name = name;
  }

  @Override
  public void run() {
    System.out.println("hello " + this.name);
  }
}

class thread1 extends Thread {
  @Override
  public void run() {
    System.out.println("aaa");
  }
}

class thread2 extends Thread {
  @Override
  public void run() {
    System.out.println("bbb");
  }
}
