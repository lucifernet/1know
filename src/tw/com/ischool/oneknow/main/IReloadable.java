package tw.com.ischool.oneknow.main;

public interface IReloadable {
	void reload();

	void setOnReloadCompletedListener(OnReloadCompletedListener listener);

	interface OnReloadCompletedListener {
		void onCompleted();
	}
}
