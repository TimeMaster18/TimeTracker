package timetracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Brief extends Report {

	/**
	 * @uml.property  name="logger"
	 */
	private static Logger logger = LoggerFactory.getLogger(Brief.class);
	
	private Table rootProjectsTable;
	
	/**
	 * Constructor of the class;
	 */
	public Brief(final Project projectSet, final Format formatSet,
			final Date startDateSet, final Date endDateSet) {
		super(projectSet, formatSet, startDateSet, endDateSet);
	}
	
	public final void generateReport() {
		logger.debug("generating report of root projects");
		getElements().add(new Line());
		getElements().add(new TextElement("Brief report"));
		getElements().add(new Line());
		
		getElements().add(new TextElement("Period"));
			// Period Table
		logger.debug("creating period table");
		ArrayList<String> periodFields = new ArrayList<String>();
		periodFields.add("");
		periodFields.add("Date");
		
		Table periodTable = new Table(periodFields);
		
		ArrayList<String> row = new ArrayList<String>();
		row.add("From");
		row.add(getDateFormat().format(getStartDate()));
		periodTable.addRow(row);
		
		row = new ArrayList<String>();
		row.add("Until");
		row.add(getDateFormat().format(getEndDate()));
		periodTable.addRow(row);
		
		row = new ArrayList<String>();
		row.add("Report generation date");
		Date currentDate = new Date();
		row.add(getDateFormat().format(currentDate));
		periodTable.addRow(row);
		
		getElements().add(periodTable);
			//
		
		getDateFormat().format(getStartDate());
	
		getElements().add(new Line());
		
		getElements().add(new TextElement("Projectes arrel"));
		
			// Creating table to save rootProjects' data
		logger.debug("creating root projects table");
		ArrayList<String> rootProjectsFields = new ArrayList<String>();
		rootProjectsFields.add("Project");
		rootProjectsFields.add("Start date");
		rootProjectsFields.add("End date");
		rootProjectsFields.add("Total time");
		this.rootProjectsTable = new Table(rootProjectsFields);	
		
		for (Activity rootProject : getProject().getChildren()) {
			logger.debug("visiting a new root project");
			rootProject.acceptVisitor(this, 0);
		}
		
		getElements().add(this.rootProjectsTable);
			//
		
		getElements().add(new Line());
		getElements().add(new TextElement("Time Tracker v1.0"));	
		
		// Aply format
		getFormat().setReport(this);
		getFormat().applyFormat();
	}

	@Override
	public final void visitInterval(final Interval interval, final int level) {
		long time;
		if (interval.getStartDate().before(getEndDate())
				&& interval.getEndDate().after(getStartDate())) {
			logger.debug("visiting a valid interval");
			// It's a valid interval

			if (interval.getStartDate().before(getStartDate())
					&& interval.getEndDate().after(getEndDate())) {
				// I4
				time = interval.getEndDate().getTime()
						- interval.getStartDate().getTime();
			} else if (interval.getStartDate().before(getStartDate())) {
				// I1
				time = interval.getEndDate().getTime()
						- getStartDate().getTime();
			} else if (interval.getEndDate().after(getEndDate())) {
				// I3
				time = getEndDate().getTime()
						- interval.getStartDate().getTime();
			} else {
				// I2
				time = interval.getEndDate().getTime()
						- interval.getStartDate().getTime();
			}
			logger.debug("getting intersection time of the interval");
			setIntersectionTime(getIntersectionTime() 
					+ TimeUnit.MILLISECONDS.toSeconds(time));
		}
	}

	@Override
	public final void visitTask(final Task task, final int level) {
		if (task.getStartDate().before(getEndDate())
				&& task.getEndDate().after(getStartDate())) {
			// It's a valid task, we can visit its intervals
			logger.debug("visiting a valid task");
			for (Interval child : task.getChildren()) {
				child.acceptVisitor(this, level + 1);
			}
		}
	}

	@Override
	public final void visitProject(final Project project, final int level) {
		// Is this correct?
		if (level == 0) {
			logger.debug("visiting a root project, " 
					+ "restarting the intersection time");
			setIntersectionTime(0);
		}		

		if (project.getStartDate().before(getEndDate())
				&& project.getEndDate().after(getStartDate())) {
			// It's a valid project, we can visit its activities
			logger.debug("visiting a valid project");
			for (Activity child : project.getChildren()) {
				child.acceptVisitor(this, level + 1);
			}			
			
			if (level  == 0) {
					// Adding project data
				logger.debug("generating data table " 
						+ "for the current root project");
				ArrayList<String> row = new ArrayList<String>();
				row.add(project.getName());
				row.add(getDateFormat().format(project.getStartDate()));
				row.add(getDateFormat().format(project.getEndDate()));
				row.add(getDurationFormat().format(getIntersectionTime()));
	
				this.rootProjectsTable.addRow(row);
			}			
		}

	}

	

}