package tw.com.ischool.oneknow;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.ischool.oneknow.util.HttpUtil;
import tw.com.ischool.oneknow.util.StringUtil;
import android.os.AsyncTask;
import android.util.Log;

public class OneKnow {
	public static final String TAG_ONE_KNOW = "OneKnow";

	private static final String ERROR_TITLE = "<title>We're sorry, but something went wrong (500)</title>";

	public static final String DOMAIN = "1know.net";
	public static final String URL = "http://1know.net/";
	public static final String URL_IMAGE = "assets/catch_images/";
	public static final String SERVICE_ACCOUNT_USER = "account/user";
	public static final String SERVICE_ACCOUNT_SWITCH = "account/switch";
	public static final String SERVICE_LOGIN = "account/login";
	public static final String SERVICE_LEARNING_KNOW = "v2/learning";
	public static final String SERVICE_KNOW_INFO = "v2/learning/%s";
	public static final String SERVICE_LEARNING_UNIT = "v2/learning/%s/units";
	public static final String SERVICE_STUDY_HISTORY = "v2/learning/units/%s/studyHistory";
	public static final String SERVICE_UNIT_STATUS = "v2/learning/units/%s/status";
	public static final String MOBILE_VIDEO = "mobile/video";
	public static final String SERVICE_LOGOUT = "account/logout";
	public static final String SERVICE_DISCOVERY_KNOWLEDGES = "v2/discovery/knowledges";
	public static final String SERVICE_UNIT_NOTES = "v2/learning/units/%s/notes";
	public static final String SERVICE_NOTE_UPDATE = "v2/learning/notes/%s";
	public static final String SERVICE_STUDY_ACTIVITY = "v2/learning/%s/studyActivity?days=%s&timezone=%s";
	public static final String SERVICE_KNOW_UNSUBSCRIBE = "v2/learning/%s/unsubscribe";
	public static final String SERVICE_YOUR_NOTES = "v2/learning/notes";
	public static final String SERVICE_STUDY_QUIZ = "v2/learning/units/%s/quizzes";
	public static final String SERVICE_STUDY_RESULT = "v2/learning/units/%s/studyResult";
	public static final String SERVICE_GET_UNIT = "v2/learning/units/%s";
	public static final String SERVICE_SUBSCRIBE = "v2/learning/%s/subscribe";

	public static <T> T getFrom(String serviceName,
			HashMap<String, String> params, Class<T> typeClass)
			throws ClientProtocolException, IOException, JSONException,
			OneKnowException {
		StringBuilder sb = new StringBuilder(URL).append(serviceName);

		if (params != null) {
			sb.append("?");
			int index = 0;
			for (String key : params.keySet()) {
				sb.append(key).append("=").append(params.get(key));
				index++;

				if (index < params.size())
					sb.append("&");
			}
		}

		HttpUtil http = HttpUtil.createInstanceWithCookie();
		String result = http.getString(sb.toString());

		Constructor<T> c;
		try {
			c = typeClass.getConstructor(String.class);
			return c.newInstance(result);
		} catch (Exception e) {
			if (result.contains(ERROR_TITLE))
				throw new OneKnowException("something went wrong");
			else {
				Log.e(TAG_ONE_KNOW,
						"JSON parsing occured error because string is : "
								+ result);
				throw new JSONException("Unsupport type:" + typeClass.getName());
			}
		}
	}

	public static <T> T postTo(String serviceName, JSONObject req,
			Class<T> typeClass) throws ClientProtocolException, IOException,
			JSONException, OneKnowException {
		HttpUtil http = HttpUtil.createInstanceWithCookie();
		StringBuilder sb = new StringBuilder(URL).append(serviceName);

		String content = StringUtil.EMPTY;
		if (req != null)
			content = req.toString();

		String result = http.postForString(sb.toString(), content);

		if (typeClass.getName().equals(Void.class.getName())) {
			return null;
		}

		Constructor<T> c;
		try {
			c = typeClass.getConstructor(String.class);
			return c.newInstance(result);
		} catch (Exception e) {
			if (result.contains(ERROR_TITLE))
				throw new OneKnowException("something went wrong");
			else {
				Log.e(TAG_ONE_KNOW,
						"JSON parsing occured error because string is : "
								+ result);
				throw new JSONException("Unsupport type:" + typeClass.getName());
			}
		}
	}

	public static <T> T putTo(String serviceName, JSONObject req,
			Class<T> typeClass) throws ClientProtocolException, IOException,
			JSONException, OneKnowException {
		HttpUtil http = HttpUtil.createInstanceWithCookie();
		StringBuilder sb = new StringBuilder(URL).append(serviceName);

		String result = http.putForString(sb.toString(), req.toString(2));

		if (typeClass.getName().equals(Void.class.getName())) {
			return null;
		}

		Constructor<T> c;
		try {
			c = typeClass.getConstructor(String.class);
			return c.newInstance(result);
		} catch (Exception e) {
			if (result.contains(ERROR_TITLE))
				throw new OneKnowException("something went wrong");
			else {
				Log.e(TAG_ONE_KNOW,
						"JSON parsing occured error because string is : "
								+ result);
				throw new JSONException("Unsupport type:" + typeClass.getName());
			}
		}
	}

	public static void delete(String serviceURL)
			throws ClientProtocolException, IOException {
		HttpUtil http = HttpUtil.createInstanceWithCookie();
		StringBuilder sb = new StringBuilder(URL).append(serviceURL);

		http.delete(sb.toString());
	}

	public static <T> T getAndSyncCookie(String serviceName,
			HashMap<String, String> params, Class<T> typeClass)
			throws ClientProtocolException, IOException, JSONException,
			OneKnowException {
		StringBuilder sb = new StringBuilder(URL).append(serviceName).append(
				"?");

		if (params != null) {
			int index = 0;
			for (String key : params.keySet()) {
				sb.append(key).append("=").append(params.get(key));
				index++;

				if (index < params.size())
					sb.append("&");
			}
		}
		
		HttpUtil http = HttpUtil.createInstance();
		String result = http.getString(sb.toString());
		HttpUtil.setGlobalCookies(http.getHttpClient().getCookieStore());

		Constructor<T> c;
		try {
			c = typeClass.getConstructor(String.class);
			return c.newInstance(result);
		} catch (Exception e) {
			if (result.contains(ERROR_TITLE))
				throw new OneKnowException("something went wrong");
			else {
				Log.e(TAG_ONE_KNOW,
						"JSON parsing occured error because string is : "
								+ result);
				throw new JSONException("Unsupport type:" + typeClass.getName());
			}
		}
	}

	public static <T> T postAndSyncCookie(String serviceName,
			JSONObject request, Class<T> typeClass)
			throws ClientProtocolException, IOException, JSONException,
			OneKnowException {
		StringBuilder sb = new StringBuilder(URL).append(serviceName);

		HttpUtil http = HttpUtil.createInstance();
		String result = http.postForString(sb.toString(), request.toString());
		HttpUtil.setGlobalCookies(http.getHttpClient().getCookieStore());

		if (typeClass.getName().equals(Void.class.getName())) {
			return null;
		}
		
		Constructor<T> c;
		try {
			c = typeClass.getConstructor(String.class);
			return c.newInstance(result);
		} catch (Exception e) {
			if (result.contains(ERROR_TITLE))
				throw new OneKnowException("something went wrong");
			else {
				Log.e(TAG_ONE_KNOW,
						"JSON parsing occured error because string is : "
								+ result);
				throw new JSONException("Unsupport type:" + typeClass.getName());
			}
		}
	}

	public static void logout() {
		OneKnow oneknow = new OneKnow();
		LogoutTask task = oneknow.new LogoutTask();
		task.execute();
	}

	private class LogoutTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			HttpUtil http = HttpUtil.createInstanceWithCookie();
			try {
				http.getString(URL + SERVICE_LOGOUT);
			} catch (ClientProtocolException e) {
				return false;
			} catch (IOException e) {
				return false;
			}

			HttpUtil.setGlobalCookies(http.getHttpClient().getCookieStore());
			return true;
		}
	}

}
