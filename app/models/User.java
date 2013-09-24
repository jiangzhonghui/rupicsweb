package models;

import java.util.*;

import javax.persistence.*;

import models.dto.VipLevel;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import util.Utils;

import com.avaje.ebean.*;

/**
 * User entity managed by Ebean
 */
@Entity 
@Table(name="account")
public class User extends Model {

	@Id
	public Long id;
	
    public String email;
    
    public String name;
    
    public String deviceId;
    
    public String password;
    
    public String tokenid;
    
    public Long tokenExpirationTime;
    
    public String desc;
	
    public int limitKBytes;
	
    public Date validEndDate;
    
    @ManyToMany
    public List<PostModel> favoritePost;
    
    // -- Queries
    
    public static Model.Finder<String,User> find = new Model.Finder(String.class, User.class);
    
    /**
     * Retrieve all users.
     */
    public static List<User> findAll() {
        return find.all();
    }

    /**
     * Retrieve a User from email.
     */
    public static User findByEmail(String email) {
        return find.where().eq("email", email).findUnique();
    }
    
    public static User loginDirect(String deviceId, String desc) {
    	User user = find.where().eq("deviceId", deviceId).findUnique();
    	if(user == null){
    		user = new User();
    		user.deviceId = deviceId;
    		user.desc = desc;
    		user.tokenid = Utils.generateHash();
    		user.tokenExpirationTime = new Date().getTime() + 30*60*1000;
    		user.limitKBytes = 10*1024;//10M
    		user.save();
    	}else{
    		user.tokenid = Utils.generateHash();
    		user.tokenExpirationTime = new Date().getTime() + 30*60*1000;
    		user.update();
    	}
        return user;
    }
    
    public static User login(String email, String password, String deviceId, String desc) {
    	User user = find.where().eq("email", email).eq("password", password).findUnique();
    	if(user == null){
    		return null;
    	}else{
    		user.tokenid = Utils.generateHash();
    		user.tokenExpirationTime = new Date().getTime() + 30*60*1000;
    		user.update();
    	}
        return user;
    }
    
    /**
     * Authenticate a User.
     */
    public static User authenticate(String email, String password) {
        return find.where()
            .eq("email", email)
            .eq("password", password)
            .findUnique();
    }
    
    // --
    
    public String toString() {
        return "User(" + email + ")";
    }

}

