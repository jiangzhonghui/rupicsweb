package fetch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.ImageModel;
import models.PostModel;
import models.SourceModel;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fetch.plugin.ParserPlugin;

public class BaseParser {

	private final static String LOGTAG = "BaseParser";

	private final static int MAX_NEXT_LEVEL = 3;

	private String sourceUrl;
	private String baseUrl;
	private SourceModel sm;

	public BaseParser(SourceModel sm) {
		this.sm = sm;
		this.sourceUrl = new String(sm.url);
		this.baseUrl = sourceUrl.indexOf("/", 8) == -1 ? sourceUrl : sourceUrl
				.substring(0, sourceUrl.indexOf("/", 8));
	}

	public ArrayList<PostModel> parseListSource(HttpClient httpClient, int page) {
		ArrayList<PostModel> list = new ArrayList<PostModel>();
		String url = sm.getUrlByPage(page);

		try {
			// 1.get content by url
			String content = getContentByUrl(httpClient, url);

			// 2.execute plugin method
			if (sm.getPluginInstance().size() > 0) {
				for (ParserPlugin plugin : sm.getPluginInstance()) {
					if (!plugin.isBeforeParsePostList())
						continue;
					content = plugin.execute(httpClient, content, url);
				}
			}

			// 2.substring by begin word and end word
			if (sm.beginWord != null && sm.beginWord.length() > 0 && sm.endWord != null && sm.endWord.length() > 0) {
				int start = content.indexOf(sm.beginWord);
				content = content.substring(start);
				int end = content.indexOf(sm.endWord);
				content = content.substring(0, end);
			}

			// 3.parse link by patterns
			Document source = Jsoup.parse(content);

			String condition = "a";
			if (sm.postUrlPattern == null || sm.postUrlPattern.length() == 0 ) {
				condition += "[href]";
			} else {
				condition += "[href*=" + sm.postUrlPattern + "]";
			}

			Elements alist = source.select(condition);

			for (Element aele : alist) {

				String href = aele.attr("href");
				String hname = aele.text();
				if (hname != null && href != null && (href.length() > 4)) {

					PostModel pm = new PostModel();

					if(hname.length()>0){
						pm.name = hname;
					}else{
						Elements subele = aele.select("img");
						if(subele != null && subele.size() > 0){
							String alt = subele.get(0).attr("alt");
							if(alt != null && alt.length() > 0){
								pm.name = alt;
							}else{
								pm.name = String.valueOf(new Date().getTime());
							}
						}else{
							pm.name = String.valueOf(new Date().getTime());
						}
					}
					
					if (href.startsWith("http")) {
						pm.url = href;
					} else if (href.startsWith("/")) {
						pm.url = baseUrl + href;
					} else if (href.startsWith("./")) {
						pm.url = sourceUrl.substring(0,
								sourceUrl.lastIndexOf("/"))
								+ href.substring(1);
					} else {
						pm.url = sourceUrl.substring(0,
								sourceUrl.lastIndexOf("/"))
								+ "/" + href;
					}

					pm.source = sm;

					// put it into list
					list.add(pm);

				}
			}
		} catch (NullPointerException ne) {
			// e.printStackTrace();
		} catch (Exception e) {
		}
		return list;
	}

	public List<ImageModel> parseImageUrlsSource(HttpClient httpClient, String url) {
		List<ImageModel> res = new ArrayList<ImageModel>();

		String currentUrl = url.substring(0, url.lastIndexOf("/"));
		String postBaseUrl = url.indexOf("/", 8) == -1 ? url : url.substring(0,
				url.indexOf("/", 8));

		try {
			// 1.get content by url
			String content = getContentByUrl(httpClient, url);
			if (content.startsWith("http://"))
				return new ArrayList<ImageModel>();

			// 2.execute plugin method
			if (sm.getPluginInstance().size() > 0) {
				for (ParserPlugin plugin : sm.getPluginInstance()) {
					if (!plugin.isBeforeParseImage())
						continue;
					content = plugin.execute(httpClient, content, url);
				}
			}

			// 2.substring by begin word and end word
			if (sm.beginWord != null && sm.beginWord.length() > 0 && sm.postEndWord != null && sm.postEndWord.length() > 0) {
				int start = content.indexOf(sm.postBeginWord);
				content = content.substring(start);
				int end = content.indexOf(sm.postEndWord);
				content = content.substring(0, end);
			}

			// 3.parse images one by one

			// 3.1 get the next page url
			content = getNextPageContent(httpClient, content, currentUrl,
					postBaseUrl, 0);

			Document source = Jsoup.parse(content);
			Elements iList = source.select("img");
			for (Element imgEle : iList) {

				String tmp = imgEle.attr("src");
				String file = imgEle.attr("file");
				if (file != null && file.length() > 0) {
					tmp = file;
				}
				if (tmp != null && tmp.length() > 0
						&& (tmp.startsWith("http://") || sm.isIncludeImageInSite)
						&& (!tmp.endsWith(".gif")) && (!tmp.endsWith("/"))) {
					if (tmp.startsWith("http")) {

					} else if (tmp.startsWith("/")) {
						tmp = postBaseUrl + tmp;
					} else if (tmp.startsWith("./")) {
						tmp = currentUrl + tmp.substring(1);
					} else {
						tmp = currentUrl + "/" + tmp;
					}
					if (res.indexOf(tmp) > 0) {
						continue;
					}
					
					int size = getImageSize(httpClient, tmp);
					if(size > 100000 || size == -1){
					ImageModel im = new ImageModel();
					im.url = tmp;
					im.createdAt = new Date();
					String pName = imgEle.parent().tagName();
					if(pName.equalsIgnoreCase("a")){
						String href = imgEle.parent().attr("href");
						int start = href.indexOf(".");
						if(start > 0){
						String domain = href.substring(start).substring(href.substring(start).indexOf("."));
						if(tmp.contains(domain)){
							im.href = domain;
						}
						}
					}

					res.add(im);
					}

				}
			}
		} catch (NullPointerException ne) {
			ne.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	private String getNextPageContent(HttpClient httpClient, String content,
			String currentUrl, String postBaseUrl, int level) {
		String res = null;
		if (sm.isIncludeNextPage) {
			Document source = Jsoup.parse(content);
			String href = null;

			for (Element aEle : source.select("a[href]")) {
				if (aEle.text() != null
						&& aEle.text().equals(sm.nextPageKeyword)) {
					href = aEle.attr("href");

					if (href.startsWith("http")) {

					} else if (href.startsWith("/")) {
						href = postBaseUrl + href;
					} else if (href.startsWith("./")) {
						href = currentUrl + href.substring(1);
					} else {
						href = currentUrl + "/" + href;
					}

					break;
				}
			}
			if (href == null) {
				res = content;
			} else {
				res = getContentByUrl(httpClient, href);
				if (res == null)
					return content;
				if (sm.postBeginWord != null && sm.postEndWord != null) {
					int start = res.indexOf(sm.postBeginWord);
					res = res.substring(start);
					int end = res.indexOf(sm.postEndWord);
					res = res.substring(0, end);
				}
				if (level < MAX_NEXT_LEVEL) {
					res = content
							+ getNextPageContent(httpClient, res, currentUrl,
									postBaseUrl, level + 1);
				}
			}

		} else {
			res = content;
		}

		return res;
	}

	public String getContentByUrl(HttpClient httpClient, String url) {
		// get content from the url
		HttpEntity resEntity = null;
		
		HttpResponse response = null;

		try {

			HttpGet httpget = new HttpGet(url);

			response = httpClient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {

				// get reponse content type
				Header header = response.getFirstHeader("Content-Type");
				String contentType = header.getValue();

				if (contentType != null && contentType.startsWith("image/")) {
					// if get an image response, return the url
					return url;
				}

				resEntity = response.getEntity();

//				if(sm.encode != null && sm.encode.equals("")){
//					sm.encode = null;
//				}
//				if (sm.encode == null
//						&& (resEntity.getContentEncoding() != null || resEntity
//								.getContentType() != null)) {
//					if (resEntity.getContentEncoding() != null) {
//						sm.encode = resEntity.getContentEncoding().getValue();
//					} else {
//
//						HeaderElement[] eles = resEntity.getContentType()
//								.getElements();
//						for (HeaderElement ele : eles) {
//							for (NameValuePair para : ele.getParameters()) {
//								if ("charset".equals(para.getName())) {
//									sm.encode = para.getValue();
//									break;
//								}
//							}
//							if (sm.encode != null) {
//								break;
//							}
//						}
//					}
//				}

				if(sm.encode != null && !sm.encode.equals("")){
				return EntityUtils.toString(resEntity, sm.encode);
				}else{
					return EntityUtils.toString(resEntity, "utf-8");
				}
				
			}

		} catch (OutOfMemoryError e) {
		} catch (NullPointerException ne) {
			ne.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if( response.getEntity() != null ) {
			try {
				EntityUtils.consume(response.getEntity());
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
			
		}

		return null;
	}
	
	private int getImageSize(HttpClient httpClient, String url){
		int size = 0;
		// get content from the url
				
				HttpResponse response = null;

				try {

					HttpHead method = new HttpHead(url);

					response = httpClient.execute(method);

					int status = response.getStatusLine().getStatusCode();

					if (status == HttpStatus.SC_OK) {

						// get reponse content type
						Header header = response.getFirstHeader("Content-Length");
						if(header != null){
						size = Integer.valueOf(header.getValue());
						}else{
							size = -1;
						}

					}

				} catch (Exception e) {
//					e.printStackTrace();
				} finally{
					if( response != null && response.getEntity() != null ) {
					try {
						EntityUtils.consume(response.getEntity());
					} catch (IOException e) {
						e.printStackTrace();
					}
					}
					
				}
		return size;
	}

}
