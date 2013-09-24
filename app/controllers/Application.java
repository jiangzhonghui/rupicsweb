package controllers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.codehaus.jackson.JsonNode;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import play.*;
import play.libs.Akka;
import play.libs.Comet;
import play.libs.F.Callback0;
import play.mvc.*;
import play.mvc.Http.Context;
import play.data.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static play.data.Form.*;

import models.*;
import scala.concurrent.duration.Duration;
import util.DateUtils;
import views.html.*;

public class Application extends Controller {

	final static ActorRef clock = Clock.instance;
	
	// -- Authentication

	private static HashMap<String, User> hashLogin = new HashMap<String, User>();

	public static class Login {

		public String email;
		public String password;

		public String validate() {
			if (User.authenticate(email, password) == null) {
				return "Invalid user or password";
			}
			return null;
		}

	}

	/**
	 * Login page.
	 */
	public static Result login() {
		String hash = generateHash();

		// save hash into map
		hashLogin.put(hash, null);

		return ok(login.render(hash, form(Login.class)));
	}
	
	private static String generateHash(){
		String hash = String.valueOf(System.currentTimeMillis() + Math.random()
				* (new Random().nextInt(10) * 10));
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(hash.getBytes());
			BigInteger bigInt = new BigInteger(1, thedigest);
			hash = bigInt.toString(16);
			while (hash.length() < 32) {
				hash = "0" + hash;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash;
	}

	/**
	 * Handle the chat websocket.
	 */
	public static WebSocket<JsonNode> hash(final String hash) {
		return new WebSocket<JsonNode>() {

			// Called when the Websocket Handshake is done.
			public void onReady(WebSocket.In<JsonNode> in,
					WebSocket.Out<JsonNode> out) {

				// Join the chat room.
				try {
					QRLoginQueue.join(hash, in, out);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	public static Result qrcode(String hash) {
		String url = "http://" + Context.current().request().host()
				+ Context.current().request().path();
		url = url.replace("/qrcode", "/login");
		url += "?hash=" + hash;
		int size = 256;
		String fileType = "png";
		response().setContentType("image/png");

		return ok(createQRImage(url, size, fileType));
	}

	private static ByteArrayInputStream createQRImage(String qrCodeText,
			int size, String fileType) {
		try {
			// Create the ByteMatrix for the QR-Code that encodes the given
			// String
			Hashtable hintMap = new Hashtable();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText,
					BarcodeFormat.QR_CODE, size, size, hintMap);
			// Make the BufferedImage that are to hold the QRCode
			int matrixWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,
					BufferedImage.TYPE_INT_RGB);
			image.createGraphics();

			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
			// Paint and save the image using the ByteMatrix
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < matrixWidth; i++) {
				for (int j = 0; j < matrixWidth; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			ImageIO.write(image, fileType, os);

			return new ByteArrayInputStream(os.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Handle login form submission.
	 */
	public static Result authenticate(String hash) {
		if (hash == null) {
			Form<Login> loginForm = form(Login.class).bindFromRequest();
			if (loginForm.hasErrors()) {
				return badRequest(login.render(generateHash(), loginForm));
			} else {
				session("email", loginForm.get().email);
				return redirect(routes.Sources.sources());
			}
		} else {
			if (hashLogin.containsKey(hash)) {
				// login if client has a real user info
				if (hashLogin.get(hash) != null) {
//					Date validDate = hashLogin.get(hash).validEnd;
//					if (validDate.after(new Date())) {
						session("email", hashLogin.get(hash).email);
						// remove hash from map
						hashLogin.remove(hash);

						return redirect(routes.Sources.sources());
//					}else{
						//TODO:return expiration error message
						//send message
//						Akka.system().scheduler().scheduleOnce(
//				                Duration.create(0, SECONDS),
//				                QRLoginQueue.defaultRoom,
//				                new Talk(hash, "Account Expiration: Your trail time is expired, do you want to be a paid member?"),
//				                Akka.system().dispatcher()
//				            );
//					}
				} else {
					// save it into map if the client has a proper user info
					String deviceId = form().bindFromRequest().get("did");
					if (deviceId != null && deviceId.startsWith("db")) {
						User user = User.find.where().eq("deviceId", deviceId)
								.findUnique();
						if (user == null) {
							user = new User();
							user.email = deviceId;
							user.deviceId = deviceId;
							user.validEndDate = DateUtils.addDateDays(new Date(),
									14);
							user.save();
						}
						hashLogin.put(hash, user);
						
						//send message
//						Akka.system().scheduler().scheduleOnce(
//				                Duration.create(0, SECONDS),
//				                QRLoginQueue.defaultRoom,
//				                new Talk(hash, "login"),
//				                Akka.system().dispatcher()
//				            );
					} else {
						hashLogin.remove(hash);
					}
				}
			}
			return login();
		}
	}
	
	public static Result comet(String hash) {
		MyComet comet = new Application.MyComet("parent.logined") ;
		comet.hash = hash;
        return ok(comet);
    }
	
	static class MyComet extends Comet{
		public MyComet(String name) {
			super(name);
		}

		String hash;
    	
        public void onConnected() {
           clock.tell(this, clock); 
        } 
	}
    
    public static class Clock extends UntypedActor {
        
        static ActorRef instance = Akka.system().actorOf(new Props(Clock.class));
        
        // Send a TICK message every 100 millis
        static {
            Akka.system().scheduler().schedule(
                Duration.Zero(),
                Duration.create(1, SECONDS),
                instance, "TICK",  Akka.system().dispatcher()
            );
        }
        
        //
        
        Map<String,MyComet> sockets = new HashMap<String,MyComet>();
        
        public void onReceive(Object message) {

            // Handle connections
            if(message instanceof MyComet) {
                final MyComet cometSocket = (MyComet)message;
                
                if(sockets.containsKey(cometSocket.hash)) {
                    
                    // Brower is disconnected
                    sockets.remove(cometSocket);
                    hashLogin.remove(cometSocket.hash);
                    Logger.info("Browser disconnected (" + sockets.size() + " browsers currently connected)");
                    
                } else {
                    
                    // Register disconnected callback 
                    cometSocket.onDisconnected(new Callback0() {
                        public void invoke() {
                            getContext().self().tell(cometSocket, clock);
                        }
                    });
                    
                    // New browser connected
                    sockets.put(cometSocket.hash, cometSocket);
                    Logger.info("New browser connected (" + sockets.size() + " browsers currently connected)");
                    
                }
                
            } 
            
            // Tick, send time to all connected browsers
            if("TICK".equals(message)) {
                
                // Send the current time to all comet sockets
                for(String hash: hashLogin.keySet()) {
                	User user = hashLogin.get(hash);
                	if(user != null){
                		
                	Comet cometSocket = sockets.get(hash);
                    cometSocket.sendMessage("login");
                	}
                }
                
            }

        }
        
    }


	/**
	 * Logout and clean the session.
	 */
	public static Result logout() {
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.login());
	}

	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter("jsRoutes",
				controllers.routes.javascript.Sources.saveSource(),
				controllers.routes.javascript.Sources.deleteSource(),
				controllers.routes.javascript.Sources.download(),
				controllers.routes.javascript.Sources.images(),
				controllers.routes.javascript.Posts.index(),
				controllers.routes.javascript.Posts.favorite(),
				controllers.routes.javascript.Posts.images(),
				controllers.routes.javascript.Issues.addIssue(),
				controllers.routes.javascript.Issues.renameIssue(),
				controllers.routes.javascript.Issues.deleteIssue(),
				controllers.routes.javascript.Issues.showIssueSelector(),
				controllers.routes.javascript.Images.deleteImages(),
				controllers.routes.javascript.Images.importImages(),
				controllers.routes.javascript.Images.sortImages()
				));
	}

}
