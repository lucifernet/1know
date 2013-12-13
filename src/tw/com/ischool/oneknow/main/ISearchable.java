package tw.com.ischool.oneknow.main;

public interface ISearchable {
	void setOnSearchListener(OnSearchListener listener);

	void search(String keyword);

	void cancelSearch();
	
	boolean readyForSearch();
	
	public interface OnSearchListener {
		void onSearchReady();

		void onSearchCompleted(int count);
	}
}
