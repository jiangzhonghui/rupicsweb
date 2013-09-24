import play.*;
import play.libs.*;

import java.util.*;
import java.util.concurrent.*;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import scala.concurrent.duration.Duration;


import com.avaje.ebean.*;

import fetch.FetchWorker;

import models.*;

public class Global extends GlobalSettings {
    
	public static final String MSG_REFETCH_POST = "fetch_posts";
	
	ActorRef tickActor;
	
    public void onStart(Application app) {
        InitialData.insert(app);
        
        tickActor = Akka.system().actorOf((new Props().withCreator(new UntypedActorFactory() {
            public UntypedActor create() {
                  return new UntypedActor() {
                    public void onReceive(Object message) {
                      if (message.equals(MSG_REFETCH_POST)) {
//                    	  FetchWorker.getInstance().fetchAllSource();
                      } else {
                        unhandled(message);
                      }
                    }
                  };
                }
              })));
      Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS),
                Duration.create(24, TimeUnit.HOURS),
                tickActor, 
                MSG_REFETCH_POST,
  Akka.system().dispatcher()
              );
    }
    
    static class InitialData {
        
        public static void insert(Application app) {
            if(Ebean.find(User.class).findRowCount() == 0) {
                
                Map<String,List<Object>> all = (Map<String,List<Object>>)Yaml.load("initial-data.yml");

                // Insert users first
                Ebean.save(all.get("users"));

                // Insert projects
                Ebean.save(all.get("sources"));
//                for(Object source: all.get("sources")) {
//                    // Insert the source/user relation
//                    Ebean.saveManyToManyAssociations(source, "createdBy");
//                }

                // Insert tasks
//                Ebean.save(all.get("tasks"));
                
            }
        }
        
    }
    
}