package fetch.plugin;

import java.io.Serializable;

import org.apache.http.client.HttpClient;

import models.SourceModel;

public abstract class ParserPlugin implements Serializable {
	protected SourceModel sm;
	
	public SourceModel getSm() {
		return sm;
	}


	public void setSm(SourceModel sm) {
		this.sm = sm;
	}


	public abstract boolean isBeforeParsePostList();

	public abstract boolean isBeforeParseImage();

	public abstract String execute(HttpClient httpClient, String content, String url);
}
