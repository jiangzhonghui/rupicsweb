package fetch.plugin;

import org.apache.http.client.HttpClient;

public class ImagetwistPlugin  extends ParserPlugin {
	
	@Override
	public String execute(HttpClient httpClient, String content,
			String url) {
		if(content != null){
			content = content.replace("imagetwist.com/th/", "imagetwist.com/i/");
			content = content.replace("myhotimage.com/upload/small/", "myhotimage.com/upload/big/");
			content = content.replace("imagelore.org/thumbs/", "imagelore.org/images/");
			content = content.replace("t1.imgchili.com/", "i2.imgchili.com/");
			content = content.replace("t2.imgchili.com/", "i2.imgchili.com/");
			content = content.replace("t3.imgchili.com/", "i2.imgchili.com/");
			content = content.replace("t4.imgchili.com/", "i2.imgchili.com/");
			content = content.replace("t5.imgchili.com/", "i2.imgchili.com/");
			content = content.replace("www.picshareunit.com/upload/thumbs/", "www.picshareunit.com/upload/files/");
		}
		return content;
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
