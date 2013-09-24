package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Context;
import play.api.templates.Html;
import play.data.*;
import static play.data.Form.*;

import java.util.*;

import models.*;

import util.Constants;
import views.html.*;
import views.html.helper.form;

/**
 * Manage projects related operations.
 */
@Security.Authenticated(Secured.class)
public class Categories extends Controller {

	static Form<CategoryModel> categoryForm = form(CategoryModel.class);

	public static Result saveCategory() {
		Form<CategoryModel> filledForm = categoryForm.bindFromRequest();
		CategoryModel sm = filledForm.get();
		sm.createdAt = new Date();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.form.category.render(CategoryModel.all(), SourceModel.all(), filledForm));
		} else {
			
				CategoryModel.create(sm);
				return redirect(routes.Sources.sources());
		}
	}

	public static Result editCategory(Long source) {
		
		if (source > 0) {
			CategoryModel sm = CategoryModel.find.byId(source);
			if (sm != null) {
				return ok(views.html.form.category.render(CategoryModel.all(), SourceModel.all(),
						categoryForm.fill(sm)));
			}
		}
		return redirect(routes.Sources.sources());
		
	}

	public static Result createCategory() {
		return ok(views.html.form.category.render(CategoryModel.all(), SourceModel.all(),
				categoryForm.fill(new CategoryModel())));
	}

	public static Result deleteCategory(Long id) {
		CategoryModel.delete(id);
		return ok(Constants.RETURN_SUCCESS);
	}

}
