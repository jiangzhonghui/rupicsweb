package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Context;
import play.api.templates.Html;
import play.data.*;
import static play.data.Form.*;

import java.util.*;

import org.apache.http.cookie.SM;

import fetch.FetchWorker;

import models.*;

import views.html.*;

/**
 * Manage projects related operations.
 */
@Security.Authenticated(Secured.class)
public class Sources extends Controller {

	static Form<SourceModel> sourceForm = form(SourceModel.class);

	public static Result sources() {
		String name = Context.current().request().username();
		User user = User.findByEmail(name);
		return ok(views.html.main.render(CategoryModel.all(),
				SourceModel.all(), views.html.welcome.render(user)));
	}

	public static Result saveSource() {
		Form<SourceModel> filledForm = sourceForm.bindFromRequest();
		SourceModel sm = filledForm.get();
		String id = filledForm.data().get("id");
		if (id != null && Long.valueOf(id) > 0) {
			// filledForm.data().put("createdBy",
			// SourceModel.find.byId(id).createdBy.email);
			sm.createdBy = SourceModel.find.byId(Long.valueOf(id)).createdBy;
		} else {
			String name = Context.current().request().username();
			// filledForm.data().put("createdBy", name);
			sm.createdBy = User.findByEmail(name);
		}
		if (filledForm.hasErrors()) {
			return badRequest(views.html.form.source.render(
					CategoryModel.all(), SourceModel.all(), filledForm));
		} else {

			if (Secured.isOwnerOf(sm)) {
				SourceModel.create(sm);
				return redirect(routes.Sources.sources());
			} else {
				return forbidden();
			}
		}
	}

	public static Result editSource(Long source) {

		if (source > 0) {
			SourceModel sm = SourceModel.find.byId(source);
			if (sm != null) {
				return ok(views.html.form.source.render(CategoryModel.all(),
						SourceModel.all(), sourceForm.fill(sm)));
			}
		}
		return redirect(routes.Sources.sources());

	}

	public static Result createSource() {
		return ok(views.html.form.source.render(CategoryModel.all(),
				SourceModel.all(), sourceForm.fill(new SourceModel())));
	}

	public static Result deleteSource(Long id) {
		sourceForm.fill(new SourceModel());
		SourceModel.delete(id);
		return redirect(routes.Sources.sources());
	}

	public static Result download(Long id) {
		SourceModel sm = SourceModel.find.byId(id);
		FetchWorker.getInstance().fetchSource(sm);
		return ok("Successfully download all images from " + sm.name);
	}

	public static Result images(Long source, Long page) {
		if (Secured.isLogin()) {
			SourceModel sm = SourceModel.find.byId(source);

			if (sm == null) {
				return redirect(routes.Sources.sources());
			}
			if (page == 1) {
				return ok(views.html.source.images.render(
						ImageModel.findBySource(source, page), sm.name, sm.id));
			} else {
				return ok(views.html.source.imageli.render(ImageModel.findBySource(source, page)));
			}
		} else {
			return forbidden();
		}
	}

}
