package js.webserver;

import static js.base.Tools.*;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.sun.net.httpserver.HttpHandler;

// To get rid of warnings in Eclipse related to com.sun.net.httpserver references,
// see https://stackoverflow.com/questions/9266632

import com.sun.net.httpserver.HttpServer;

import js.base.BaseObject;
import js.base.StateMachine;
import js.base.DateTimeTools;
import js.webserver.SimpleServer;

public class SimpleServer extends BaseObject {

  // ------------------------------------------------------------------
  // The singleton instance
  // ------------------------------------------------------------------

  public static SimpleServer sharedInstance() {
    return sSharedInstance.get();
  }

  private static Supplier<SimpleServer> sSharedInstance = singleton(() -> new SimpleServer());

  // ------------------------------------------------------------------

  /**
   * Add a context
   */
  public SimpleServer withContext(String pathPrefix, HttpHandler handler) {
    Object previousHandler = mContexts.put(pathPrefix, handler);
    checkState(previousHandler == null, "duplicate path for prefix:", pathPrefix);
    return this;
  }

  private Map<String, HttpHandler> mContexts = hashMap();

  public void start() {
    if (!state().is(STATE_NEW))
      return;

    if (mAutoStop > 0)
      activateAutoStop();

    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
      {
        checkState(!mContexts.isEmpty(), "no contexts defined");
        for (Entry<String, HttpHandler> entry : mContexts.entrySet()) {
          server.createContext(entry.getKey(), entry.getValue());
        }
        mContexts = null;
      }
      mHttpThreadPool = Executors.newFixedThreadPool(3);
      server.setExecutor(mHttpThreadPool);
      server.start();
      mHttpServer = server;
      state().next("start");
    } catch (Throwable t) {
      pr("caught:", t);
      state().jump(STATE_STOPPED, "exception while starting");
      throw asRuntimeException(t);
    }
  }

  public void stop() {
    if (state().is(STATE_STOPPED))
      return;
    state().assertIs(STATE_RUNNING);
    log("stop, shutting down thread pool");

    mHttpServer.stop(1);
    mHttpServer = null;
    mHttpThreadPool.shutdown();
    try {
      mHttpThreadPool.awaitTermination(2, TimeUnit.MINUTES);
    } catch (Exception e) {
      pr("...failed to shut down thread pool", INDENT, e);
    }
    mHttpThreadPool = null;
    state().next("stop");
  }

  public boolean running() {
    return state().is(STATE_RUNNING);
  }

  // ------------------------------------------------------------------
  // Auto stop support -- shuts down server if inactive for a while;
  // actually, exits the Java virtual machine
  // ------------------------------------------------------------------

  public SimpleServer withAutoStop() {
    return withAutoStop(60000);
  }

  public SimpleServer withAutoStop(int maxInactivtyMs) {
    state().assertIs(STATE_NEW);
    mAutoStop = maxInactivtyMs;
    return this;
  }

  public void updateLastRequestTime() {
    mLastRequestTime = System.currentTimeMillis();
  }

  public long lastRequestTime() {
    return mLastRequestTime;
  }

  private void activateAutoStop() {
    Thread t = new Thread(() -> updateAutoStop());
    t.setDaemon(true);
    t.start();
  }

  private void updateAutoStop() {
    while (true) {
      DateTimeTools.sleepForRealMs(2000);
      long time = System.currentTimeMillis();
      if (mLastRequestTime == 0)
        mLastRequestTime = time;
      long elapsed = time - mLastRequestTime;
      if (elapsed > mAutoStop) {
        alert("shutdown timer has expired; exiting Java virtual machine");
        System.exit(0);
        break;
      }
    }
  }

  private int mAutoStop;
  private long mLastRequestTime;

  // ------------------------------------------------------------------
  // State machine
  // ------------------------------------------------------------------

  private StateMachine state() {
    if (mStateMachine == null) {
      mStateMachine = new StateMachine("SimpleServer.State")//
          .add(STATE_NEW)//
          .toAdd(STATE_RUNNING)//
          .toAdd(STATE_STOPPED)//
          .build();
      mStateMachine.setVerbose(verbose());
    }
    return mStateMachine;
  }

  private static final String STATE_NEW = "new";
  private static final String STATE_RUNNING = "running";
  private static final String STATE_STOPPED = "stopped";

  private StateMachine mStateMachine;

  private ExecutorService mHttpThreadPool;
  private HttpServer mHttpServer;

}
