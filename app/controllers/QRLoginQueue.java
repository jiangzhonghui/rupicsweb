package controllers;

import play.mvc.*;
import play.libs.*;
import play.libs.F.*;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.*;
import akka.dispatch.*;
import static akka.pattern.Patterns.ask;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import java.util.*;

import static java.util.concurrent.TimeUnit.*;

/**
 * A chat room is an Actor.
 */
public class QRLoginQueue extends UntypedActor {
	
	static long lastCleanTime = System.currentTimeMillis();
    
    // Default room.
    static ActorRef defaultRoom = Akka.system().actorOf(new Props(QRLoginQueue.class));
    
    static{
    	Akka.system().scheduler().schedule(
                Duration.create(5, MINUTES),
                Duration.create(5, MINUTES),
                defaultRoom,
                new Talk("Robot", "clean"),
                Akka.system().dispatcher()
            );
    }
    
    /**
     * Join the queue.
     */
    public static void join(final String hash, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception{
        
        // Send the Join message to the room
        String result = (String)Await.result(
        		ask(defaultRoom,
        				new Join(hash, out), 
        				2000), 
        		Duration.create(2, SECONDS));
        
        if("OK".equals(result)) {
            
            // For each event received on the socket,
            in.onMessage(new Callback<JsonNode>() {
               public void invoke(JsonNode event) {
                   
                   // Send a Talk message to the room.
                   defaultRoom.tell(new Talk(hash, event.get("text").asText()), defaultRoom);
                   
               } 
            });
            
            // When the socket is closed.
            in.onClose(new Callback0() {
               public void invoke() {
                   
                   // Send a Quit message to the room.
                   defaultRoom.tell(new Quit(hash), defaultRoom);
                   
               }
            });
            
        } else {
            
            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);
            
            // Send the error to the socket.
            out.write(error);
            
        }
        
    }
    
    public void notifyLogin(String hash, String msg){
    	WebSocket.Out<JsonNode> channel = members.get(hash);
    	if(channel != null){
    		ObjectNode event = Json.newObject();
            event.put("kind", "talk");
            event.put("user", hash);
            if(msg == null){
            event.put("message", "login");
            }else{
            	event.put("error", msg);
            }
            
            channel.write(event);
    	}
    }
    
    // Members of this room.
    Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>();
    Map<String, Long> memberValidTime = new HashMap<String, Long>();
    
    public void onReceive(Object message) throws Exception {
        
        if(message instanceof Join) {
            
            // Received a Join message
            Join join = (Join)message;
            
            // Check if this username is free.
            if(members.containsKey(join.hash)) {
                getSender().tell("This hashcode is already used", defaultRoom);
            } else {
                members.put(join.hash, join.channel);
                memberValidTime.put(join.hash, System.currentTimeMillis()+5*60*1000);
//                notifyAll("join", join.hash, "has entered the room");
                getSender().tell("OK", defaultRoom);
            }
            
        } else if(message instanceof Talk)  {
            
            // Received a Talk message
            Talk talk = (Talk)message;
            
            if(talk.text.startsWith("clean")){
            	Set<String> keys = members.keySet();
            	for(String key : keys){
            		if(memberValidTime.get(key) < System.currentTimeMillis()){
            			members.remove(key);
            			memberValidTime.remove(key);
            		}
            	}
            }else if(talk.text.startsWith("login")){
            	notifyLogin(talk.hash, null);
            }else{
            	notifyLogin(talk.hash, talk.text);
            }
            
        } else if(message instanceof Quit)  {
            
            // Received a Quit message
            Quit quit = (Quit)message;
            
            members.remove(quit.hash);
            
//            notifyAll("quit", quit.hash, "has leaved the room");
        
        } else {
            unhandled(message);
        }
        
    }
    
    // Send a Json event to all members
    public void notifyAll(String kind, String user, String text) {
        for(WebSocket.Out<JsonNode> channel: members.values()) {
            
            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("user", user);
            event.put("message", text);
            
            ArrayNode m = event.putArray("members");
            for(String u: members.keySet()) {
                m.add(u);
            }
            
            channel.write(event);
        }
    }
    
    // -- Messages
    
    public static class Join {
        
        final String hash;
        final WebSocket.Out<JsonNode> channel;
        
        public Join(String username, WebSocket.Out<JsonNode> channel) {
            this.hash = username;
            this.channel = channel;
        }
        
    }
    
    public static class Talk {
        
        final String hash;
        final String text;
        
        public Talk(String username, String text) {
            this.hash = username;
            this.text = text;
        }
        
    }
    
    public static class Quit {
        
        final String hash;
        
        public Quit(String username) {
            this.hash = username;
        }
        
    }
    
}
