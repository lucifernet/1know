package tw.com.ischool.oneknow.study;

public interface IUnitPlayerHandler {
	/**
	 * 指定事件觸發處理器
	 * **/
	void setUnitEventListener(OnUnitEventListener listener);

	/**
	 * Fragment 被銷毀前交代遺言用
	 * **/
	void beforeDestory();

	/**
	 * 展開至全螢幕
	 * **/
	void openFullScreen();

	/**
	 * 當返回鍵按下時處理
	 * 
	 * @return true:中止其它動作, false:繼續返回
	 * **/
	boolean handleBackPressed();

	void pause();

	/**
	 * 將進度移至指定位置
	 * 
	 * @param toSecond
	 *            : 指定秒數, 單位為秒
	 * **/
	void seekTo(double toSecond);

	/**
	 * 取得目前進度秒數
	 * 
	 * @return 目前進度秒數
	 * **/
	double getCurrentTime();
}
