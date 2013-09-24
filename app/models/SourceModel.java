package models;

import java.io.Serializable;
import java.util.*;

import play.db.ebean.*;
import play.data.format.Formats;
import play.data.validation.Constraints.*;

import javax.persistence.*;

import fetch.plugin.ParserPlugin;

@Entity
@Table(name="source")
public class SourceModel extends Model implements Serializable {

	public final static String LOGTAG = "SourceModel";

	public static final String RATE = "RATE";

	@Id
	public Long id;

	@Required
	public String name;

	public String language;

	@Required
	public String url = ""; // $page$ means page number

	public String postUrlPattern; // post url pattern that means every post link
									// should contain this string

	public int deepLevel=1; // 0 - single page ; 1 - forum pluginList

	public String tag; // hot beauty, art, wallpaper, download task

	public String beginWord; // the keyword for identify where the body begins

	public String endWord; // the keyword for identify where the body ends

	public String postBeginWord; // the keyword for identify where the post
									// body begins

	public String postEndWord; // the keyword for identify where the post body
								// ends

	public boolean isShare; // 0 - false ; 1 - true

	public boolean isIncludeNextPage; // 0 - false ; 1 - true

	public String nextPageKeyword; // if isIncludeNextPage == 1, nextPageKeyword
									// used to find the link of next page

	public String imageType = "all"; // default - all, jpeg, jpg, gif, png

	public boolean isIncludeImageInSite; // 0 - only images outside of site ; 1
											// - any image including in-site

	public int minWidth = 0; // 0 - not limited, if succeed the size, just
								// remove the image

	public int minHeight = 0; // 0 - not limited

	public boolean isReadOnly; // 0 - editable ; 1 - readonly

	public String plugin; // an array of class names for invoke before get post
							// content

	public String encode; // encode of the website

	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdAt;

	@ManyToOne
	public User createdBy;

	public int subscribeAmount = 0;

	public int rate = 0; // available value is 1 - 10, 0 means no rate

	@Transient
	private ArrayList<ParserPlugin> pluginList;

	public static Finder<Long, SourceModel> find = new Finder(Long.class,
			SourceModel.class);

	public static List<SourceModel> all() {
		return find.all();
	}
	
	public static List<SourceModel> getSourceByUser(String user) {
		//get user's own sources
		List<SourceModel> list = SourceModel.find.where()
		          .eq("createdBy.email", user).findList();
		//get shared sources
		if(list != null){
			List<SourceModel> list2 = SourceModel.find.where()
			          .eq("isShare", true).orderBy("subscribeAmount desc")
			          .findPagingList(20).getPage(0).getList();
			          for(SourceModel sm : list2){
			        	 if(!list.contains(sm)){
			        		 list.add(sm);
			        	 }
			          }
		}
		return list;
	}

	public static void create(SourceModel source) {
		if(source.id != null && source.id > 0){
			source.update();
		}else{
			source.id = (long) 0;
		source.save();
		}
	}

	public static void delete(Long id) {
		PostModel.deleteBySource(id);
		find.ref(id).delete();
	}

	public SourceModel() {

	}


	public String getUrlByPage(int page) {
		if (this.url.contains("$page$")) {
			return this.url.replace("$page$", String.valueOf(page));
		} else if (this.url.contains("$page*")) {
			int start = this.url.indexOf("$page*");
			int end = this.url.lastIndexOf("$");
			String org = this.url.substring(start, end + 1);
			int number = Integer.valueOf(this.url.substring(start + 6, end));
			return this.url.replace(org, String.valueOf((page - 1) * number));
		} else {
			return this.url;
		}
	}
	
	public List<ParserPlugin> getPluginInstance() {
		if (pluginList == null) {
			pluginList = new ArrayList<ParserPlugin>();
			if (this.plugin != null) {
				String[] classNames = this.plugin.split(",");
				for(String name : classNames){
					try {
						Class<ParserPlugin> pluginClass = (Class<ParserPlugin>) Class.forName(name);
						ParserPlugin pluginInstance = pluginClass.newInstance();
						pluginInstance.setSm(this);
						pluginList.add(pluginInstance);
					} catch (IllegalAccessException e) {
					} catch (InstantiationException e) {
					} catch (ClassNotFoundException e) {
					}
				}
			}
//			pluginList.add(new CheckAdPlugin());
		}
		
		return pluginList;
	}

	public static boolean isOwner(SourceModel source, String user) {
        return source.createdBy.email.equals(user);
    }

}
