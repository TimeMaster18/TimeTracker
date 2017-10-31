/**
 * 
 */
package timeTracker;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Observable;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author     marcm
 * @uml.dependency   supplier="java.util.Observer"
 * @uml.dependency   supplier="timeTracker.Interval"
 */
public class SimpleTask extends Task {
	
	static Logger logger = LoggerFactory.getLogger(SimpleTask.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	
	/**
	 * @uml.property   name="children"
	 * @uml.associationEnd   multiplicity="(0 -1)" aggregation="shared" inverse="task:timeTracker.Interval"
	 */
	private Collection<Interval> children = new ArrayList<Interval>();

	/**
	 * @uml.property   name="minIntervalTime"
	 */
	public static long minIntervalTime;

	/**
	 * Constructor of the class.
	 */
	public SimpleTask(String name, String description, Project father) {
		super(description, name, father);
		
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		Date currentD = Clock.getInstance().getCurrentDate();		
		
		if (!this.children.isEmpty()) {
			if (this.getLastInterval().isRunning()) {
				this.getLastInterval().setEndDate(currentD);
				this.getLastInterval().calculateTime();
				this.setEndDate(currentD);
			}
		}		
		this.calculateTotalTime();		
	}

	@Override
	public void acceptVisitor(Visitor visitor, int level) {		
		visitor.visitTask(this, level);
		for (Interval child : children) {
			child.acceptVisitor(visitor, level+1);
		}	
	}

	/**
	 * Getter of the property <tt>children</tt>
	 * @return  Returns the intervals.
	 * @uml.property  name="children"
	 */
	@SuppressWarnings("unchecked")
	public Collection<Interval> getChildren() {
		if (children.isEmpty()) {
			return null;
		} else {
			return children;
		}
	}

	/**
	 * Adds a new interval to the intervals list.
	 */
	public void start() {	
		if (!this.children.isEmpty() && this.getLastInterval().isRunning() == true){
			logger.warn("Can't start the task, it is already running");
			System.out.println("Can't start the task, it is already running");
		}else{
			Interval interval = new Interval(children.size() + 1, this);
			children.add(interval);		
			logger.info("interval for task "+this.getName()+" started");
		}
		
	}

	/**
	 * Calculates the total time of all the children of the current SimpleTask.
	 */
	public void calculateTotalTime() {
		long sum = 0;
		
		for (Interval child : children) {
			sum += child.getTotalTime();
		}
		
		this.totalTime = sum;
		
		if(this.father != null){
			father.calculateTotalTime();
		}
	}

	/** 
	 * Searches for the interval with the id given and returns it if it has been found.
	 * @param id: id of the interval to be found.
	 * @return Return the interval with the same id, or null if it has not been found.
	 */
	public Interval getIntervalById(int id) {
		for (Interval interval : children) {
			if (interval.getId() == id) {
				return interval;
			}			
		}
		return null;
	}
	

	/**
	 */
	public Interval getLastInterval(){
		return getIntervalById(children.size());			
	}

	/**
	 * Removes the specified interval if it is in the intervals list.
	 * @param id: id of the interval to be removed.
	 * @return True if the interval could be removed and false if it couldn't.
	 */
	public boolean removeInterval(int id) {
		Interval interval = getIntervalById(id);
		if (interval != null) {
			logger.debug("interval exists");
			children.remove(interval);
			return true;
		} else {
			logger.debug("interval doesnt exist");
			return false;
		}
	}

	/**
	 */
	public void stop() {
		if (!this.children.isEmpty()) {
			if(this.getLastInterval().isRunning()) {
				
				this.getLastInterval().stop();
				
				if (this.getLastInterval().getTotalTime() < minIntervalTime) {
					logger.debug("interval too short");
					this.removeInterval(this.getLastInterval().getId());					
				}
			} else {
				System.out.println("No se puede parar un intervalo que ya se ha parado.");
			}
		}
	}


	/**
	 */
	public static void setMinIntervalTime(long intervalTime){
		minIntervalTime = intervalTime;
	}
	
	public boolean childrenIsEmpty() {
		return this.childrenIsEmpty();
	}
}


