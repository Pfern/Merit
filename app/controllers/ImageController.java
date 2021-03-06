package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import models.Image;
import models.Image.imageType;

import com.google.common.io.Files;

import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.images;

@Security.Authenticated(Secured.class)
public class ImageController extends Controller {

	public static Result images() {

		Form<Image> imagesForm = new Form<Image>(Image.class);

		List<Image> imagesList = Image.find.all();

		return ok(images.render(imagesList, imagesForm));
	}

	public static Result addImage() {

		// TODO check if image exists (HASH comparison)
		// TODO rename files (long id)

		// Move file to new location
		// get web path and save it

		Form<Image> imagesForm = new Form<Image>(Image.class).bindFromRequest();

		if (imagesForm.hasErrors()) {
			List<Image> imagesList = Image.find.all();
			return badRequest(images.render(imagesList, imagesForm));
		}

		MultipartFormData body = request().body().asMultipartFormData();

		if (body == null) {
			flash(Application.GLOBAL_FLASH_ERROR,
					"Body is null, please check and try again.");
			List<Image> imagesList = Image.find.all();
			return badRequest(images.render(imagesList, imagesForm));
		}

		FilePart resourceFile = body.getFile("imageFile");

		if (resourceFile == null) {
			flash(Application.GLOBAL_FLASH_ERROR, "Did you select an image?");
			List<Image> imagesList = Image.find.all();
			return badRequest(images.render(imagesList, imagesForm));
		}

		// PNG CHECK
		String fileName = resourceFile.getFilename();
		String extention = fileName.substring(fileName.lastIndexOf('.') + 1)
				.trim();
		if (!extention.toUpperCase().equals("PNG")) {
			flash(Application.GLOBAL_FLASH_ERROR, "The image must be a PNG!");
			List<Image> imagesList = Image.find.all();
			return badRequest(images.render(imagesList, imagesForm));
		}

		String imagesPath = Image.imagesFolder;
		String selectedFolder = Image.lostandfound;

		if (imagesForm.get().imageType == imageType.badge) {
			selectedFolder = Image.badges;
		} else if (imagesForm.get().imageType == imageType.issuer) {
			selectedFolder = Image.issuers;
		}

		// app path
		File projectRoot = Play.application().path();

		// new path
		File fullPath = new File(projectRoot, imagesPath + selectedFolder);

		// new path + filename
		File newLoc = new File(fullPath, resourceFile.getFilename());

		// attempt move
		try {
			Files.move(resourceFile.getFile(), newLoc);
		} catch (IOException e) {
			e.printStackTrace();
			flash(Application.GLOBAL_FLASH_ERROR,
					"We could not move the file, Sorry.");
			List<Image> imagesList = Image.find.all();
			return badRequest(images.render(imagesList, imagesForm));
		}

		// web path

		File imagesFolder = new File("images");

		File relativeFolder = new File(imagesFolder, selectedFolder);

		File relativePath = new File(relativeFolder, resourceFile.getFilename());

		// get URL of folder
		String absURL = routes.Assets.at(relativePath.getPath()).absoluteURL(
				request());

		// create image model + save it
		new Image(absURL, imagesForm.get().name, imagesForm.get().imageType)
				.save();

		flash(Application.GLOBAL_FLASH_SUCCESS, "Image added");

		return redirect(routes.ImageController.images());
	}

	public static Result delete(Long id) {
		Image.find.byId(id).delete();
		flash(Application.GLOBAL_FLASH_SUCCESS, "Image deleted");
		return redirect(routes.ImageController.images());
	}
}
