package models;

import java.io.Serializable;
import java.util.*;

import play.db.ebean.*;
import play.data.format.Formats;
import play.data.validation.Constraints.*;
import util.Constants;

import javax.persistence.*;
import javax.validation.Constraint;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagingList;

@Entity
@Table(name="post",
uniqueConstraints=
@UniqueConstraint(columnNames={"id", "url"}))
public class PostModel extends Model implements Serializable{

	@Id
	public Long id;

	@Required
	public String name;

	@Column(unique=true, nullable=false) 
	public String url;

	public String imgUrl; // facto, it stores an array of urls for all of images in the post

	@ManyToOne(cascade = CascadeType.REMOVE)
	public SourceModel source; // 

	public int hits = 0; // view amount of this post

	public int rate = 0; // default: 0 ; 1 - 5 for rating this post

	public boolean deleteFlag; // 0 - exists images ; 1 - no images

	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdAt; 

	public int imageAmount = 0;

	public int localImageAmount = 0;
	
	@ManyToOne
	public IssueModel issue;
	
	@Transient
	public boolean isFavorite;
	
	public static Finder<Long,PostModel> find = new Finder(
    Long.class, PostModel.class
  );
  
	public static List<PostModel> all() {
    return find.all();
  }
  
  public static void create(PostModel post) {
  	post.save();
  }
  
  public static void delete(Long id) {
  	find.ref(id).delete();
  }
  
  public static void deleteBySource(Long id) {
	  List<PostModel> list = PostModel.find.where()
	          .eq("source.id", id).findList();
	  for(PostModel pm : list){
		  ImageModel.deleteByPost(pm.id);
	  	delete(pm.id);
	  }
	  }
  
  public static List<PostModel> findBySource(Long source) {
      return PostModel.find.where()
          .eq("source.id", source)
          .findList();
  }
  
  public static List<PostModel> findBySource(String userEmail, Long source, int page) {
	  User user = User.findByEmail(userEmail);
      List<PostModel> list = PostModel.find.where()
          .eq("source.id", source).eq("deleteFlag", false).orderBy("createdAt desc")
          .findPagingList(Constants.AMOUNT_PER_PAGE).getPage(page-1).getList();
      for(PostModel pm : list){
//    	  delete(pm.id);
    	  if(user != null && user.favoritePost.contains(pm)){
    		  pm.isFavorite = true;
    	  }
      }
      
      return list;
  }
  
  public static List<PostModel> findMyFavorite(String userEmail, int page) {
	  int start = (page-1)*10;
	  int end = start + 10;
	  User user = User.findByEmail(userEmail);
      List<PostModel> list = (start >= 0 && end < user.favoritePost.size())?user.favoritePost.subList(start, start+10):user.favoritePost;
      for(PostModel pm : list){
    		  pm.isFavorite = true;
      }
      
      return list;
  }
  
  public static void markAsFavorite(String userEmail, Long postId) {
	  User user = User.findByEmail(userEmail);
        PostModel post = PostModel.find.ref(postId);
        if(user.favoritePost.contains(post)){
        user.favoritePost.remove(post);	
        }else{
        user.favoritePost.add(post);
        }
        user.update();
    }
	
	public PostModel(){
		
	}
	
}
