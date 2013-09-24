package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Context;
import play.data.*;
import static play.data.Form.*;

import java.util.*;

import org.jboss.netty.handler.ipfilter.CIDR;

import models.*;
import util.Constants;
import views.html.*;

/**
 * Manage posts related operations.
 */
@Security.Authenticated(Secured.class)
public class Issues extends Controller {

	static Form<IssueModel> issueForm = form(IssueModel.class);

	/**
	 * Display the issues of the category
	 */
	public static Result index(Long category, Long page) {
		if (Secured.isLogin()) {
			return ok(issues.render(CategoryModel.find.byId(category),
					IssueModel.findIssueByCategory(category, page.intValue()),
					page));
		} else {
			return forbidden();
		}

	}

	/**
	 * Display images of the issue<br>
	 * 
	 * @param issue
	 * @return
	 */
	public static Result images(Long issue) {
		if (Secured.isLogin()) {
			IssueModel pm = IssueModel.find.byId(issue);

			if (pm == null) {
				return redirect(routes.Sources.sources());
			}

			return ok(images.render(ImageModel.findByIssue(issue), pm.name, issue));
		} else {
			return forbidden();
		}
	}

	// -- IssueModels

	public static Result addIssue(Long category) {
		IssueModel sm = new IssueModel();
		sm.createdAt = new Date();
		sm.name = "New Issue";

			return ok(views.html.issue.item.render(IssueModel.create(sm, category)));
	}
	
	public static Result showIssueSelector() {
			return ok(views.html.issue.issueselector.render(IssueModel.all()));
	}
	
	public static Result renameIssue(Long issueId, String newName) {
			return ok(IssueModel.renameIssue(issueId, newName));
	}

	public static Result deleteIssue(Long id) {
		IssueModel.delete(id);
		return ok(Constants.RETURN_SUCCESS);
	}

}
