package controllers;

import java.util.List;

import models.BadgeAssertion;
import models.Revocation;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.Request;

@Security.Authenticated(Secured.class)
public class RevocationController extends Controller {

	public static Result add(Long assertionUID) {
		String reason = "automatic revocation";
		revocate(assertionUID, reason);
		return redirect(routes.AssertionController.assertions());
	}

	public static Result add(Long assertionUID, String reason) {
		revocate(assertionUID, reason);
		return redirect(routes.AssertionController.assertions());
	}

	public static Result remove(Long assertionUID){
		BadgeAssertion ba = BadgeAssertion.find.byId(assertionUID);
		ba.blocked = false;
		ba.save();
		
		Revocation.find.byId(assertionUID).delete();
		return redirect(routes.AssertionController.assertions());
	}
	
	public static void revocate(Long assertionUID, String reason) {
		BadgeAssertion ba = BadgeAssertion.find.byId(assertionUID);
		ba.blocked = true;
		ba.save();
		Revocation r = new Revocation(assertionUID, reason);
		r.save();
	}

	public static Result getRecocationsAsJson() {
		List<Revocation> list = Revocation.find.all();
		return ok(Json.toJson(list));
	}
	public static String getRecocationsString() {
		List<Revocation> list = Revocation.find.all();
		return Json.toJson(list).toString();
	}

	public static String getUrl(Request request) {
		String url = routes.RevocationController.getRecocationsAsJson()
				.absoluteURL(request);
		return url;
	}

}
