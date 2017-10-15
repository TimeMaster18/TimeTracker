/**
 * 
 */
package timeTracker;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/** 
 * @author marcm
 */
public class Task extends Component {

	/** 
	 * @uml.property name="children"
	 * @uml.associationEnd multiplicity="(0 -1)" aggregation="shared" inverse="task:timeTracker.Interval"
	 */
	private Collection<Interval> children = new ArrayList<Interval>();
	
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
	 * Constructor of the class.
	 */
	public Task(String name, String description) {
		super(description, name);
		
	}

	/**
	 * Adds a new interval to the intervals list.
	 */
	public void addInterval(Clock c) {
		Interval interval = new Interval(children.size() + 1);
		children.add(interval);		
		c.addObserver(interval);
	}

	/**
	 * Calculates the total time of all the children of the current Task.
	 */
	public void calculateTotalTime() {
		long sum = 0;
		for (Interval child : children) {
			sum += child.getTotalTime();
		}
		sum += this.totalTime.getTime();
		this.totalTime = new Date(sum);		
	}

	/** 
	 * Searches for the interval with the id given and returns it if it has been found.
	 * @param id: id of the interval to be found.
	 * @return Return the interval with the same id, or null if it has not been found.
	 */
	public Interval getInterval(int id) {
		for (Interval interval : children) {
			if (interval.getId() == id) {
				return interval;
			}			
		}
		return null;
	}
	
	/** 
	 * Searches for the Interval with the given id and returns it if it has been found.
	 * @param id: id of the Interval to be found.
	 * @return Returns the child Interval with the same id, or null if it has not been found.
	 */
	public timeTracker.Interval getChild(int id){
		for (Interval child : children) {
			if (child.getId() == id) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Removes the specified interval if it is in the intervals list.
	 * @param id: id of the interval to be removed.
	 * @return True if the interval could be removed and false if it couldn't.
	 */
	public boolean removeInterval(int id) {
		Interval interval = getInterval(id);
		if (interval != null) {
			int index = (((AbstractList<Interval>) children).indexOf(interval));
			children.remove(index);
			return true;
		} else {
			return false;
		}
	}
}
