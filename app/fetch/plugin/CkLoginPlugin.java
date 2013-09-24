package fetch.plugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CkLoginPlugin extends ParserPlugin {
	
	private static final String LOGTAG = "CkLoginPlugin";
	
	@Override
	public String execute(HttpClient httpClient, String content,
			String url) {
		if (content.contains("<div class=\"warning_button\">")) {
			content = ck_warning(httpClient, content, url);
		}
		
		String res = content;
		
		try {

			if (content.contains("home.php?mod=space&amp;do=pm")) {
			} else {
				String login_url = "http://ck101.com/logging.php?action=login";
				
				String account = getAccount();
				if (account == null || account.equals("")) {
					return content;
				}

				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("username", getAccount()));
				nvps.add(new BasicNameValuePair("password", getPassword()));
				
				Document source = Jsoup.parse(content);
				Element formEle = source.select("#lsform").first();
				Elements eles = formEle.select("input");
				Element ele = eles.get(2);
				nvps.add(new BasicNameValuePair(ele.attr("name"),
						ele.attr("value")));
				ele = eles.get(3);
				nvps.add(new BasicNameValuePair(ele.attr("name"),
						ele.attr("value")));
				ele = eles.get(4);
				nvps.add(new BasicNameValuePair(ele.attr("name"),
						ele.attr("value")));
				
				// submit lgoin form
				login_url = "http://ck101.com/" + formEle.attr("action");
				HttpPost httpost = new HttpPost(login_url);

				httpost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));

				HttpResponse response = httpClient.execute(httpost);
				HttpEntity entity = response.getEntity();
//				res = EntityUtils.toString(entity, "utf-8");
				
				EntityUtils.consume(entity);

				// go into current page
				 HttpGet httpget = new HttpGet(url);
				 response = httpClient.execute(httpget);
				 entity = response.getEntity();
				 res = EntityUtils.toString(entity, "utf-8");
				 
				 EntityUtils.consume(entity);
			}

		} catch (NullPointerException ne) {
			ne.printStackTrace();
		} catch (OutOfMemoryError ooe) {
			ooe.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	private String ck_warning(HttpClient httpclient, String result,
			final String path) {
		String login_url = "http://ck101.com/forumdisplay.php?fid=176&confirm18x=1";
		String res = result;
		try {
			// 1.go into confirm page
			HttpGet httpget = new HttpGet(login_url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			 EntityUtils.consume(entity);

			// go to the current page
			httpget = new HttpGet(path);
			response = httpclient.execute(httpget);
			entity = response.getEntity();
			res = EntityUtils.toString(entity, "utf-8");
			 EntityUtils.consume(entity);

		}catch (NullPointerException ne) {
//			e.printStackTrace();
		} catch (Exception e) {
		}

		return res;

	}
	
	public String getAccount() {
		return "cy2012";

	}

	public String getPassword() {
		return "111111";
	}

	@Override
	public boolean isBeforeParsePostList() {
		return true;
	}

	@Override
	public boolean isBeforeParseImage() {
		return true;
	}

}
