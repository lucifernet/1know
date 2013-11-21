package tw.com.ischool.oneknow.main;

public interface ISearchable {
	void setOnSearchListener(OnSearchListener listener);

	void search(String keyword);

	public interface OnSearchListener {
		void onDataReady();

		void onSearchCompleted(int count);
	}
}
