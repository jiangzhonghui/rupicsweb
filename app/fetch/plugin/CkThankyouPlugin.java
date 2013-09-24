package fetch.plugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

public class CkThankyouPlugin extends ParserPlugin {

	private static final String LOGTAG = "CkThankyouPlugin";

	@Override
	public String execute(HttpClient httpClient, String content, String url) {
		if (content.contains("<div class=\"locked\">")) {
			String fid = this.getSm().url.substring(this.getSm().url.indexOf("fid=")+4);
			int end = fid.indexOf("&");
			fid = fid.substring(0, end);
			String login_url = "http://ck101.com/forum.php?mod=post&action=thank&fid="
					+ fid + "&tid=";
			String id = "";
			id = url.substring(url.indexOf("tid=") + 4);
			end = id.indexOf("&");
			id = id.substring(0, (end > 0?end:id.length()));
			String res = content;
			try {
				// 1.go to thank you page
				login_url += id;
				HttpGet httpget = new HttpGet(login_url);
				HttpResponse response = httpClient.execute(httpget);
				HttpEntity entity = response.getEntity();
				res = EntityUtils.toString(entity, "utf-8");
				 EntityUtils.consume(entity);

				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				Document source = Jsoup.parse(content);
				Element formEle = source.select("form").get(1);
				List<Element> fele = formEle.select("input");
				for (Element ele : fele) {
//					if (ele.attr("name") == null) {
//						continue;
//					}
//					if ("subject".equalsIgnoreCase(ele
//							.attr("name"))) {
//						nvps.add(new BasicNameValuePair("subject", source
//								.select("#subjecthide").text()));
//						continue;
//					}
					nvps.add(new BasicNameValuePair(ele
							.attr("name"), ele
							.attr("value")));
				}
				nvps.add(new BasicNameValuePair("message", ""));

				// submit thankyou form
				login_url += "&thankssubmit=yes";
				HttpPost httpost = new HttpPost(login_url);

				httpost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));

				response = httpClient.execute(httpost);
				entity = response.getEntity();
//				res = EntityUtils.toString(entity, "utf-8");
				
				EntityUtils.consume(entity);

				// go into current page
				httpget = new HttpGet(url);
				response = httpClient.execute(httpget);
				entity = response.getEntity();
				res = EntityUtils.toString(entity, "utf-8");
				
				EntityUtils.consume(entity);
			} catch (NullPointerException ne) {
				 ne.printStackTrace();
			} catch (OutOfMemoryError ooe) {
				 ooe.printStackTrace();
			} catch (Exception e) {
			}

			return res;
		} else {
			return content;
		}

	}

	private static String randomReply() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("very nice! thank you ... any more pictures? i guess she spent her life whoring for dangerous men.");
		list.add("pretty cool! so bigggggg tits. go ahead and shake them. i felt the thrill of a sexual attraction.");
		list.add("what a hot chick.       how high you can get sleeping with her? being of one sex or the other.");
		list.add("you are so nice.                  she gives the appearance of being more sexually accessible?");
		list.add("thanks a lot, wonderful images.      i want to view more, pls. is it capable of sexual reproduction.");
		list.add("push to the top.        it looks like  she didn't want to have sex with him and less favourably on the grounds of their sex.");
		list.add("thank you!");
		list.add("thank you! Good pics");
		list.add("all right...");
		list.add("so good!");
		Random random = new Random();
		return list.get(random.nextInt(5));
	}

	@Override
	public boolean isBeforeParsePostList() {
		return false;
	}

	@Override
	public boolean isBeforeParseImage() {
		return true;
	}

}
