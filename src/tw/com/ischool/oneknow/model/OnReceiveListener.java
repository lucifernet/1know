package tw.com.ischool.oneknow.model;

public interface OnReceiveListener<T> {
	void onReceive(T result);

	void onError(Exception e);
}
