package models;

import java.io.Serializable;
import java.util.*;

import play.db.ebean.*;
import play.data.format.Formats;
import play.data.validation.Constraints.*;
import play.mvc.Result;
import util.Constants;

import javax.persistence.*;

import models.dto.SimpleIssue;

@Entity
@Table(name="issue")
public class IssueModel extends Model implements Serializable{

	@Id
	public Long id;

	@Required
	public String name;

	@ManyToOne(cascade = CascadeType.REMOVE)
	public CategoryModel category; 
	
	public int imageAmount = 0;
	
	public String cover;//image path
	
	public boolean deleteFlag;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdAt;
	
	public static Finder<Long,IssueModel> find = new Finder(
    Long.class, IssueModel.class
  );
  
	public static List<IssueModel> all() {
    return find.all();
  }
	public static List<IssueModel> findIssueByCategory(Long category, int page) {
	    return find.where()
		          .eq("category.id", category).eq("deleteFlag", false).orderBy("createdAt desc")
		          .findPagingList(Constants.AMOUNT_PER_PAGE).getPage(page-1).getList();
	  }
	
	public static List<SimpleIssue> findOnlySelfIssueByCategory(Long category, int page) {
	    List<IssueModel> list = find
	    		.select("id, name, imageAmount, cover")
	    		.where()
		          .eq("category.id", category).eq("deleteFlag", false).orderBy("createdAt desc")
		          .findPagingList(Constants.AMOUNT_PER_PAGE).getPage(page-1).getList();
	    List<SimpleIssue> list2 = new ArrayList<SimpleIssue>();
	    for(IssueModel im : list){
	    	SimpleIssue si = new SimpleIssue();
	    	si.id = im.id;
	    	si.cover = im.cover;
	    	si.createdAt = im.createdAt;
	    	si.imageAmount = im.imageAmount;
	    	si.name = im.name;
	    	list2.add(si);
	    }
	    return list2;
	  }
  
  public static IssueModel create(IssueModel post, Long category) {
	  post.category = CategoryModel.find.ref(category);
  	post.save();
  	return post;
  }
  
  public static void delete(Long id) {
	  ImageModel.deleteIssueRef(id);
  	find.ref(id).delete();
  }
  
  public static String renameIssue(Long issueId, String newName) {
		IssueModel issue = find.ref(issueId);
	    issue.name = newName;
	    issue.update();
	    issue.createdAt = new Date();
	    return newName;
	}
	
	public IssueModel(){
		
	}
	
}
