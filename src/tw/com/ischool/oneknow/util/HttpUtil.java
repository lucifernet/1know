package tw.com.ischool.oneknow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import tw.com.ischool.oneknow.OneKnowException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class HttpUtil {
	public static final String TAG_HTTP_UTIL = "HttpUtil";

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private static DefaultHttpClient newHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	private DefaultHttpClient _httpClient;
	private static CookieStore globalCookies;

	public HttpUtil(DefaultHttpClient httpClient) {
		_httpClient = httpClient;
	}

	public InputStream getStream(String url) throws ClientProtocolException,
			IOException {

		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("Accept", "application/json, text/plain, */*");
		httpget.setHeader("Connection", "keep-alive");
		// Execute the request
		HttpResponse response;
		try {
			response = _httpClient.execute(httpget);
		} catch (ClientProtocolException ex) {
			Log.e(TAG_HTTP_UTIL, ex.getMessage());
			throw ex;
		}
		// Get hold of the response entity
		HttpEntity entity = response.getEntity();
		// If the response does not enclose an entity, there is no need
		// to worry about connection release

		if (entity != null) {

			// A Simple JSON Response Read
			InputStream instream = entity.getContent();
			return instream;
		}
		return null;
	}

	public String postForString(String url, String content)
			throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(url);
		StringEntity params = new StringEntity(content, "UTF-8");
		post.setEntity(params);
		post.setHeader("Accept", "application/json, text/plain, */*");
		// post.setHeader("Accept-Encoding", " gzip,deflate,sdch");
		post.setHeader("Content-type", "application/json;charset=UTF-8");
		// post.setHeader("Accept-Language",
		// "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");

		HttpResponse response = _httpClient.execute(post);
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity, StringUtil.EMPTY);

		return result;
	}

	public String putForString(String url, String content)
			throws ClientProtocolException, IOException, OneKnowException {

		// --This code works for updating a record from the feed--
		HttpPut httpPut = new HttpPut(url.toString());
		httpPut.addHeader("Accept", "application/json");
		httpPut.addHeader("Content-type", "application/json;charset=UTF-8");

		StringEntity entity = new StringEntity(content,"UTF-8");
		entity.setContentType("application/json;charset=UTF-8");// text/plain;charset=UTF-8
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json;charset=UTF-8"));
		httpPut.setEntity(entity);

		// Send request to WCF service
		HttpResponse response = _httpClient.execute(httpPut);
		HttpEntity entity1 = response.getEntity();

		if (entity1 != null
				&& (response.getStatusLine().getStatusCode() >= HttpStatus.SC_OK &&
					    response.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES)) {			
			return EntityUtils.toString(entity, StringUtil.EMPTY);
		} else {
			//int sc = response.getStatusLine().getStatusCode();
			//return response.getStatusLine().getReasonPhrase();
			throw new OneKnowException(response.getStatusLine().getReasonPhrase());
		}

		// /////////////////////
//		HttpPut put = new HttpPut(url);
//		StringEntity params = new StringEntity(content, "UTF-8");
//		put.setEntity(params);
//		put.setHeader("Accept", "application/json");
//		put.setHeader("Content-type", "application/json;charset=UTF-8");
//
//		HttpResponse response = _httpClient.execute(put);
//		HttpEntity entity = response.getEntity();
//		String result = EntityUtils.toString(entity, StringUtil.EMPTY);
//
//		return result;
	}

	public void delete(String url) throws ClientProtocolException, IOException {
		HttpDelete post = new HttpDelete(url);
		post.setHeader("Accept", "application/json");

		_httpClient.execute(post);
	}

	public String getString(String url) throws ClientProtocolException,
			IOException {
		String result = StringUtil.EMPTY;

		InputStream instream = getStream(url);

		if (instream != null) {
			result = convertStreamToString(instream);
			instream.close();
		}

		return result;
	}

	public Bitmap getImage(String url) {

		Bitmap mIcon11 = null;

		InputStream in;
		try {
			in = getStream(url);
			mIcon11 = BitmapFactory.decodeStream(in);
			in.close();
		} catch (ClientProtocolException e) {
			Log.e(TAG_HTTP_UTIL, e.getClass() + ":" + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG_HTTP_UTIL, e.getClass() + ":" + e.getMessage());
		}

		return mIcon11;
	}

	public DefaultHttpClient getHttpClient() {
		return _httpClient;
	}

	public static void setGlobalCookies(CookieStore cookies) {
		globalCookies = cookies;
	}

	public void syncCookie() {
		this._httpClient.setCookieStore(globalCookies);
	}

	public static synchronized HttpUtil createInstance() {
		DefaultHttpClient httpClient = HttpUtil.newHttpClient();
		HttpUtil http = new HttpUtil(httpClient);
		return http;
	}

	public static synchronized HttpUtil createInstanceWithCookie() {
		DefaultHttpClient httpClient = HttpUtil.newHttpClient();
		HttpUtil http = new HttpUtil(httpClient);
		http.syncCookie();
		return http;
	}

}
