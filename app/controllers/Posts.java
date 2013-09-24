package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Context;
import play.data.*;
import static play.data.Form.*;

import java.util.*;

import models.*;
import views.html.*;

/**
 * Manage posts related operations.
 */
@Security.Authenticated(Secured.class)
public class Posts extends Controller {

    /**
     * Display the posts panel for this project.
     */
    public static Result index(Long source, Long page) {
    	if(Secured.isLogin()) {
    		return ok(
                    posts.render(
                        SourceModel.find.byId(source),
                        PostModel.findBySource(Context.current().request().username(), source, page.intValue()),
                        page
                    )
                );
        } else {
            return forbidden();
        }
            
    }
    
    public static Result myfavorite(Long page) {
    	if(Secured.isLogin()) {
    		return ok(
                    posts.render(
                        null,
                        PostModel.findMyFavorite(Context.current().request().username(), page.intValue()),
                        page
                    )
                );
        } else {
            return forbidden();
        }
            
    }
    
    public static Result images(Long post) {
    	if(Secured.isLogin()) {
    	PostModel pm = PostModel.find.byId(post);
    	
    	if(pm == null){
    		return redirect(routes.Sources.sources());
    	}
    	
            return ok(
            		images.render(
            				ImageModel.findByPost(post),
            				pm.name,
            				null
            				)
            );
    	} else {
            return forbidden();
        }
    }
  
    // -- PostModels
  
    /**
     * Update a post
     */  
    public static Result update(Long post) {
       
            PostModel.markAsFavorite(
            		Context.current().request().username(),	
                post
            );
            return ok();
        
    }
    
    public static Result favorite(Long post) {
        
        PostModel.markAsFavorite(
        		Context.current().request().username(),
            post
        );
        return ok();
    
}
  
    /**
     * Delete a post
     */
    public static Result delete(Long post) {
       
            PostModel.find.ref(post).delete();
            return ok();
    
    }
  
   

}

