package com.stanfy.enroscar.goro;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Handler for calling listener methods. Works in the main thread.
 */
class ListenersHandler extends BaseListenersHandler {
  private static final String MESSAGE_KEY = "com.stanfy.enroscar.goro.MessageData";

  /** Message code. */
  private static final int MSG_START = 1, MSG_FINISH = 2, MSG_ERROR = 3, MSG_CANCEL = 4,
                           MSG_SCHEDULE = 5;

  /** Initial capacity. */
  private static final int INIT_CAPACITY = 5;

  private final Messenger messenger;

  public ListenersHandler(Messenger messenger) {
    super(INIT_CAPACITY);

    this.messenger = messenger;
  }

  public ListenersHandler() {
    super(INIT_CAPACITY);

    messenger = new Messenger(new H(this));
  }

  public Messenger getMessenger() {
    return messenger;
  }

  public void postSchedule(final Callable<?> task, final String queue) {
    try {
      Message msg = Message.obtain(null, MSG_SCHEDULE);
      Bundle data = msg.getData();
      data.putParcelable(MESSAGE_KEY, new MessageData(task, null, queue));
      messenger.send(msg);
    } catch (RemoteException e) {
      // this object should be deactivated soon
    }
  }

  public void postStart(final Callable<?> task) {
    try {
      Message msg = Message.obtain(null, MSG_START);
      Bundle data = msg.getData();
      data.putParcelable(MESSAGE_KEY, new MessageData(task, null, null));
      messenger.send(msg);
    } catch (RemoteException ignore) {
      // this object should be deactivated soon
    }
  }

  public void postFinish(final Callable<?> task, Object result) {
    try {
      Message msg = Message.obtain(null, MSG_FINISH);
      Bundle data = msg.getData();
      data.putParcelable(MESSAGE_KEY, new MessageData(task, result, null));
      messenger.send(msg);
    } catch (RemoteException ignore) {
      // this object should be deactivated soon
    }
  }

  public void postError(final Callable<?> task, Throwable error) {
    try {
      Message msg = Message.obtain(null, MSG_ERROR);
      Bundle data = msg.getData();
      data.putParcelable(MESSAGE_KEY, new MessageData(task, error, null));
      messenger.send(msg);
    } catch (RemoteException ignore) {
      // this object should be deactivated soon
    }
  }

  public void postCancel(final Callable<?> task) {
    try {
      Message msg = Message.obtain(null, MSG_CANCEL);
      Bundle data = msg.getData();
      data.putParcelable(MESSAGE_KEY, new MessageData(task, null, null));
      messenger.send(msg);
    } catch (RemoteException ignore) {
      // this object should be deactivated soon
    }
  }

  /** Handler implementation. */
  private static class H extends Handler {

    /** Outer class instance handler. */
    private final WeakReference<ListenersHandler> listenersHandlerRef;

    public H(ListenersHandler listenersHandler) {
      super(Looper.getMainLooper());
      this.listenersHandlerRef = new WeakReference<>(listenersHandler);
    }

    @Override
    public void handleMessage(@SuppressWarnings("NullableProblems") final Message msg) {
      ListenersHandler lh = listenersHandlerRef.get();
      if (lh == null) {
        return;
      }
      ArrayList<GoroListener> taskListeners = lh.taskListeners;
      if (taskListeners.isEmpty()) {
        return;
      }
      // make a copy of listeners to allow them to modify listeners collection
      taskListeners = new ArrayList<>(taskListeners);

      Bundle resultBundle = msg.getData();
      resultBundle.setClassLoader(ListenersHandler.class.getClassLoader());
      MessageData data = resultBundle.getParcelable(MESSAGE_KEY);
      if (data == null) {
        throw new IllegalArgumentException("Data cannot be null");
      }

      switch (msg.what) {
        case MSG_SCHEDULE:
          for (GoroListener listener : taskListeners) {
            listener.onTaskSchedule(data.task, data.queue);
          }
          break;

        case MSG_START:
          for (GoroListener listener : taskListeners) {
            listener.onTaskStart(data.task);
          }
          break;

        case MSG_FINISH:
          for (GoroListener listener : taskListeners) {
            listener.onTaskFinish(data.task, data.resultOrError);
          }
          break;

        case MSG_ERROR:
          for (GoroListener listener : taskListeners) {
            listener.onTaskError(data.task, (Throwable) data.resultOrError);
          }
          break;

        case MSG_CANCEL:
          for (GoroListener listener : taskListeners) {
            listener.onTaskCancel(data.task);
          }
          break;

        default:
          throw new IllegalArgumentException("Unexpected message " + msg);
      }

    }
  }

}
