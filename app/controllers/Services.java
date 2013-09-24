package controllers;

import play.*;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.Context;
import play.api.templates.Html;
import play.data.*;
import static play.data.Form.*;

import java.util.*;

import models.*;
import models.dto.MessageModel;
import models.dto.SimpleImage;
import models.dto.SimpleIssue;

import views.html.*;

/**
 * Mobile services
 */
public class Services extends Controller {

	public static Result getCategories() {
		MessageModel<List<CategoryModel>> mm = new MessageModel<List<CategoryModel>>();
		mm.setFlag(true);
		mm.setData(CategoryModel.all());
		return ok(Json.toJson(mm));
	}

	public static Result getIssuesByCategory(Long category, Long page) {
		MessageModel<List<SimpleIssue>> mm = new MessageModel<List<SimpleIssue>>();
		mm.setFlag(true);
		mm.setData(IssueModel.findOnlySelfIssueByCategory(category, page.intValue()));
		return ok(Json.toJson(mm));
	}
	
	public static Result getImagesByIssue(Long issue) {
		MessageModel<List<SimpleImage>> mm = new MessageModel<List<SimpleImage>>();
		mm.setFlag(true);
		mm.setData(ImageModel.findOnlySelfByIssue(issue));
		return ok(Json.toJson(mm));
	}
	
	public static Result logindirct(String deviceId, String desc) {
		User user = User.loginDirect(deviceId, desc);
		MessageModel<User> mm = new MessageModel<User>();
		mm.setFlag(true);
		mm.setData(user);
		return ok(Json.toJson(mm));
	}
	
	public static Result login(String email, String password, String deviceId, String desc) {
		User user = User.login(email, password, deviceId, desc);
		MessageModel<User> mm = new MessageModel<User>();
		mm.setFlag(true);
		mm.setData(user);
		return ok(Json.toJson(mm));
	}
	
	public static Result addUserConsumedKbytes(String deviceId, Long size) {
		User user = User.find.where().eq("deviceId", deviceId).findUnique();
		if(user != null){
			user.limitKBytes -= size;
			user.update();	
		}
		MessageModel<User> mm = new MessageModel<User>();
		mm.setFlag(true);
		mm.setData(user);
		return ok(Json.toJson(mm));
	}
}
