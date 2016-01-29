package com.runnable;

import java.util.Observable;
import java.util.Observer;

public class Listener implements Observer {

  public void update(Observable o, Object arg) {
    System.out.println("RunThread死机");

    DoSomething run = new DoSomething();
    run.addObserver(this);

    new Thread(run).start();

    System.out.println("RunThread重启");

  }

}
