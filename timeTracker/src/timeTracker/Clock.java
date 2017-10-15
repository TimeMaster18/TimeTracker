/**
 * 
 */
package timeTracker;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

/** 
 * @author marcm
 */
public class Clock extends Observable {

	Timer timer;
	int seconds;
	
	// Notify task to notify observers
    class NotifyTask extends TimerTask{

        @Override
        public void run() {
        setChanged();
        notifyObservers();
        }
    }
    
    public Clock(){
    	timer = new Timer();
    }
	
    public void schedule(long seconds){
        timer.scheduleAtFixedRate(new NotifyTask(), 0, seconds*1000); //delay in milliseconds
    }
    
    
    
}