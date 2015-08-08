package com.stanfy.enroscar.goro;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.concurrent.Callable;

/** Message data. */
public class MessageData implements Parcelable {
  /** Queue name. */
  final String queue;
  /** Task instance. */
  final Callable<?> task;
  /** Error instance. */
  final Object resultOrError;

  public MessageData(final Callable<?> task, final Object resultOrError, final String queue) {
    this.task = task;
    this.resultOrError = resultOrError;
    this.queue = queue;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable((Parcelable) task, 0);
    dest.writeValue(resultOrError);
    dest.writeString(queue);
  }

  public static final Creator<MessageData> CREATOR = new Creator<MessageData>() {
    @Override
    public MessageData createFromParcel(Parcel source) {
      final ClassLoader loader = MessageData.class.getClassLoader();

      return new MessageData(
              (Callable<?>) source.readParcelable(loader),
              source.readValue(loader),
              source.readString()
      );
    }

    @Override
    public MessageData[] newArray(int size) {
      return new MessageData[size];
    }
  };
}
