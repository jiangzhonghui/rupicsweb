package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Context;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.data.*;
import static play.data.Form.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.jboss.netty.handler.ipfilter.CIDR;

import models.*;
import util.Constants;
import views.html.*;

/**
 * Manage images related operations.
 */
@Security.Authenticated(Secured.class)
public class Images extends Controller {

	/**
	 * Display images of the issue<br>
	 * 
	 * @param issue
	 * @return
	 */
	public static Result deleteImages(List<Long> images) {
		if (Secured.isLogin()) {
			ImageModel.delete(images);

			return ok(Constants.RETURN_SUCCESS);
		} else {
			return forbidden();
		}
	}

	public static Result importImages(List<Long> images, Long issue) {
		if (Secured.isLogin()) {
			ImageModel.importIssue(images, issue);

			return ok(Constants.RETURN_SUCCESS);
		} else {
			return forbidden();
		}
	}
	
	public static Result sortImages(List<Long> images, Long issue) {
		if (Secured.isLogin()) {
			ImageModel.sortImages(images, issue);

			return ok(Constants.RETURN_SUCCESS);
		} else {
			return forbidden();
		}
	}
	
	public static Result showImages(String filename) {
		
		String path = Play.application().path().getPath() + "/upload/" + filename;
		
		try {
			return ok(new FileInputStream(new File(path)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return notFound(filename + " is Not Found!");
	}
	
	public static Result uploadImages(Long issue) {
		MultipartFormData body = request().body().asMultipartFormData();
		  FilePart picture = body.getFile("files[]");
		  if (picture != null) {
			  String path = Play.application().path().getPath() + "/upload/";
		    String fileName = picture.getFilename();
		    String contentType = picture.getContentType(); 
		    
		    if(contentType == null || !contentType.startsWith("image")){
		    	return ok("error:not image");
		    }
		    
		    File file = picture.getFile();
		    try {
				IOUtils.copy(new FileInputStream(file), new FileOutputStream(new File(path + fileName)));
				
				ImageModel im = new ImageModel();
			    im.createdAt = new Date();
			    im.fileType = contentType;
			    im.href = fileName;
			    im.url =  routes.Images.showImages(fileName).absoluteURL(request());
			    im.issue = IssueModel.find.byId(issue);
			    
			    ImageModel.create(im);
			    
			    return ok("File uploaded");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		    
		    
		    return ok("error:Writting file error");
		  } else {
		    flash("error", "Missing file");
		    return ok("error:Missing file");    
		  }
	}

}
