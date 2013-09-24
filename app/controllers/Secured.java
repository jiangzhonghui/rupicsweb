package controllers;

import java.util.Date;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import models.*;

public class Secured extends Security.Authenticator {
    
    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
    }
    
    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }
    
    // Access rights
    
    public static boolean isOwnerOf(SourceModel source) {
        return SourceModel.isOwner(
            source,
            Context.current().request().username()
        );
    }
    
    public static boolean isLogin() {
    	String name = Context.current().request().username();
        if( name != null && name.length() > 0){
        	User user = User.findByEmail(name);
        	if(user.validEndDate.after(new Date())){
        		return true;
        	}
        }
        return false;
    }
    
}