package models;

import java.io.Serializable;
import java.util.*;

import play.db.ebean.*;
import play.data.format.Formats;
import play.data.validation.Constraints.*;

import javax.persistence.*;

import fetch.plugin.ParserPlugin;

@Entity
@Table(name="category")
public class CategoryModel extends Model implements Serializable {

	public final static String LOGTAG = "CategoryModel";

	@Id
	public Long id;

	@Required
	public String name;

	public String cover = ""; //image path

	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdAt;

	public static Finder<Long, CategoryModel> find = new Finder(Long.class,
			CategoryModel.class);

	public static List<CategoryModel> all() {
		return find.all();
	}
	
	public static void create(CategoryModel category) {
		if(category.id != null && category.id > 0){
			category.update();
		}else{
			category.id = null;
		category.save();
		}
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public CategoryModel() {

	}

}
