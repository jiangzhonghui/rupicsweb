package fetch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.PersistenceException;

import models.ImageModel;
import models.PostModel;
import models.SourceModel;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchWorker {
	private static FetchWorker instance;

	private static final int MAX_PAGE = 3;

	private Logger logger = LoggerFactory
			.getLogger("chapters.introduction.HelloWorld1");

	private HttpClient httpClient;

	public static FetchWorker getInstance() {
		if (instance == null) {
			instance = new FetchWorker();
		}
		return instance;
	}

	private FetchWorker() {
		this.httpClient = createHttpClient();
	}

	private HttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		params.setParameter("http.socket.timeout", new Integer(1000));
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpConnectionParams.setConnectionTimeout(params, 1000);
		HttpConnectionParams.setSoTimeout(params, 1000);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(params, true);
		// HttpClientParams.setRedirecting(params, true);

		return new DefaultHttpClient(params);
	}

	public void fetchAllSource() {
		List<SourceModel> smList = SourceModel.all();
		for (SourceModel sm : smList) {
			fetchSource(sm);
		}
	}

	public void fetchSource(SourceModel sm) {
		BaseParser bp = new BaseParser(sm);
//		if (sm.id <= 12)
//			return;
		if(sm.url.contains("$page$")){
		for (int i = 0; i < MAX_PAGE; i++) {
			fetchSourceByPage(sm, bp, i + 1);
		}
		}else{
			fetchSourceByPage(sm, bp, -1);
		}
	}

	private void fetchSourceByPage(SourceModel sm, BaseParser bp, int page) {
		// 1.get all posts
		logger.debug("begining fetch source:" + sm.name + " page:" + page);
		ArrayList<PostModel> list = bp.parseListSource(httpClient, page);
		logger.debug("end fetch source:" + sm.name);

		for (PostModel pm : list) {
			// 2.save post into db
			pm.createdAt = new Date();
			try {
				PostModel.create(pm);
			} catch (PersistenceException pe) {
				continue;
			}
			logger.debug("saved post:" + pm.name);

			// 3.fetch images of posts
			logger.debug("begining fetch post url:" + pm.url);
			List<ImageModel> iList = bp
					.parseImageUrlsSource(httpClient, pm.url);
			logger.debug("end fetch post url:" + pm.url);

			// 4.save images
			int i = 0;
			for (ImageModel im : iList) {
				im.post = pm;
				im.tag = sm.tag;
				try {
					im.save();
					i++;
				} catch (PersistenceException pe) {
					continue;
				}

				logger.debug("save image:" + im.url);
			}
			pm.imageAmount = i;
			if(i == 0){
				pm.deleteFlag = true;
			}
			pm.save();
		}
	}
}
