package models;

import java.io.Serializable;
import java.sql.Blob;
import java.util.*;

import play.db.ebean.*;
import play.data.format.Formats;
import util.Constants;

import javax.persistence.*;

import models.dto.SimpleImage;

@Entity
@Table(name="image",
uniqueConstraints=
@UniqueConstraint(columnNames={"id", "url"}))
public class ImageModel extends Model implements Serializable{

	@Id
	public Long id;

	@Column(unique=true, nullable=false) 
	public String url;
	
	public String fileType;
	
	public String href; //if possible, store big pics url

	@ManyToOne(cascade = CascadeType.REMOVE)
	public PostModel post; // 
	
	@ManyToOne(cascade = CascadeType.DETACH)
	public IssueModel issue;
	
	public int issueOrder;

	public boolean deleteFlag; //
	
	public int ratedAmount;
	
	public int viewedAmount;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdAt;
	
	public String tag;
	
	public static Finder<Long,ImageModel> find = new Finder(
    Long.class, ImageModel.class
  );
  
	public static List<ImageModel> all() {
    return find.all();
  }
  
  public static void create(ImageModel post) {
  	post.save();
  	
  	if(post.issue != null){
  		post.issue.cover = post.url;
  		post.issue.imageAmount = getCountByIssue(post.issue.id); 
  		post.issue.update();
  		post.issue.category.cover = post.url;
  		post.issue.category.update();
  	}
  }
  
  public static void delete(Long id) {
  	find.ref(id).delete();
  }
  
  public static void deleteByPost(Long id) {
	  List<ImageModel> list = ImageModel.find.where()
	          .eq("post.id", id)
	          .findList();
	      for(ImageModel im: list){
	    	  delete(im.id);
	      }
	  }
  
  public static List<ImageModel> findByPost(Long post) {
		  return ImageModel.find.where()
		          .eq("post.id", post).eq("deleteFlag", false)
		          .order("createdAt desc")
		          .findList();
  }
  
  public static List<ImageModel> findByIssue(Long issue) {
      return ImageModel.find.where()
          .eq("issue.id", issue).eq("deleteFlag", false)
          .order("issueOrder")
          .findList();
  }
  
  public static List<ImageModel> findBySource(Long source, Long page) {
      return ImageModel.find.where()
          .eq("post.source.id", source).eq("deleteFlag", false)
          .order("issueOrder")
          .findPagingList(Constants.IMAGE_AMOUNT_PER_PAGE).getPage(page.intValue()).getList();
  }
  
  public static void deleteIssueRef(Long issue) {
	  List<ImageModel> list = ImageModel.find.where()
	          .eq("issue.id", issue)
	          .findList();
	      for(ImageModel im: list){
	    	  im.issue = null;
	    	  im.update();
	      }
  }
  
  public static List<SimpleImage> findOnlySelfByIssue(Long issue) {
      List<ImageModel> list = ImageModel.find
    		  .select("id,url")
    		  .where()
          .eq("issue.id", issue).eq("deleteFlag", false)
          .order("issueOrder")
          .findList();
      
      List<SimpleImage> list2 = new ArrayList<SimpleImage>();
      for(ImageModel im : list){
    	  SimpleImage si = new SimpleImage();
    	  si.id = im.id;
    	  si.url = im.url;
    	  si.issueOrder = im.issueOrder;
    	  list2.add(si);
      }
      return list2;
  }
  
  public static int getCountByIssue(Long issue) {
      return ImageModel.find.where()
          .eq("issue.id", issue).eq("deleteFlag", false)
          .findRowCount();
  }
  
  public static void delete(List<Long> images) {
      List<ImageModel> list = ImageModel.find.where()
          .in("id", images)
          .findList();
      for(ImageModel im: list){
    	  im.deleteFlag = true;
    	  im.update();
      }
  }
  
  public static void sortImages(List<Long> images, Long issue) {
	  IssueModel issueModel = IssueModel.find.ref(issue);
      int i = 1;
      ImageModel imCover = null;
      for(Long imId: images){
    	  ImageModel im = find.ref(imId);
    	  if(im.issue == null || im.issue.id != issue){
    		  im.issue = issueModel;
    	  }
    	  im.issueOrder = i++;
    	  im.update();
    	  
    	  imCover = im;
      }
      //update cover image url
      if(imCover != null){
      issueModel.cover = imCover.url;
      issueModel.imageAmount = getCountByIssue(issue); 
      issueModel.update();
      issueModel.category.cover = issueModel.cover;
      issueModel.category.update();
      }
  }
  
  public static void importIssue(List<Long> images, Long issue) {
	  IssueModel issueModel = IssueModel.find.ref(issue);
      List<ImageModel> list = ImageModel.find.where()
          .in("id", images)
          .findList();
      
      //count total amount of the issue
      int total = ImageModel.find.where()
      .eq("issue.id", issue).findRowCount();
      int i = 1;
      
      for(ImageModel im: list){
    	  if(im.issue == null || im.issue.id != issue){
    	  im.issue = issueModel;
    	  im.issueOrder = total + i++;
    	  }
    	  im.update();
      }
      //update cover image url
      if(list.size() > 0){
      issueModel.cover = list.get(list.size() - 1).url;
      issueModel.imageAmount = getCountByIssue(issue); 
      issueModel.update();
      issueModel.category.cover = issueModel.cover;
      issueModel.category.update();
      }
  }
	
	public ImageModel(){
		
	}

}
