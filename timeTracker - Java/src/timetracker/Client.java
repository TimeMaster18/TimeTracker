package timetracker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to run the application and operate with it.
 */
public class Client {

	private static final long MILISECONDS_IN_SECOND = 1000;
	
	private static Logger logger = LoggerFactory.getLogger(Client.class);

	/**
	 * @uml.property name="rootProjects"
	 * @uml.associationEnd multiplicity="(0 -1)" aggregation="shared"
	 *                     inverse="client:time.Tracker.Project"
	 */
	private Project voidProject = new Project("/", " ", null);

	/**
	 * Searches for the rootProject with the name given and returns it if it has
	 * been found.
	 */
	public final Activity getRootProject(final String name) {
		logger.debug("getting root project: " + name);

		for (Activity rootProject : voidProject.getChildren()) {
			if (rootProject.getName().equals(name)) {
				return rootProject;
			}
		}

		return null;
	}

	public Client() {
	}

	@SuppressWarnings("unchecked")
	public static void main(final String[] args) {

		Client c = new Client();
		Thread clockThread = new Thread(Clock.getInstance());
		clockThread.start();
		
		// Deserialization
		try {
			FileInputStream fileIn = new FileInputStream("data.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			c.voidProject.setChildren((Collection<Activity>) in.readObject());
			logger.info("Desarialized data from data.ser");
			System.out.println("Desarialized data from data.ser");
		} catch (IOException e) {
			logger.warn("There is not data.ser file created.");
		} catch (ClassNotFoundException e) {
			logger.warn("Activity class not found.");
			return;
		}

		Impresor.getInstance().setRootProjects(c.voidProject.getChildren());

		c.printMenu();

		logger.debug("Back to main()");

		try {
			logger.debug("trying to stop the clock");
			Clock.getInstance().terminate();
			clockThread.join();
			System.out.println("Exiting clock thread");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			logger.error("Cannot stop clock");
		}
		
		// Serialization
		try {
			FileOutputStream fileOut = new FileOutputStream("data.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(c.voidProject.getChildren());
			out.close();
			fileOut.close();
			logger.info("Serialized data is saved in data.ser");
			System.out.println("Serialized data is saved in data.ser");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.warn("There is not data.ser file created.");
		} catch (IOException e) {
			logger.warn("An error has ocurred when we tried to serialize.");
		}
	}

	/**
	 * Asks the user the properties needed to create a new Project.
	 */
	public final ArrayList<String> askRootProjectProperties() {
		ArrayList<String> properties = new ArrayList<String>();
		Scanner sc = new Scanner(System.in);

		logger.debug("introducing root project name");
		System.out.print("Introduce a name for the RootProject: ");
		properties.add(sc.nextLine());
		logger.debug("name introduced: " + properties.get(0));
		while (getRootProject(properties.get(0)) != null) {
			logger.warn("project name " + properties.get(0) + " already exist");
			properties.remove(0);
			logger.debug("Introducing new name for the project");
			System.out.print("A Root Project with the same name already "
					+ "exists in the system. Introduce a new name: ");
			properties.add(sc.nextLine());
			logger.debug("new name introduced: " + properties.get(0));
		}
		logger.debug("introducing description");
		System.out.print("Introduce a description: ");
		properties.add(sc.nextLine());
		logger.debug("description introduced:" + properties.get(1));
		logger.debug("all properties has been introduced correctly");
		// ArrayList with two strings: the name and the description for the
		// new Activity.
		return properties;
	}

	/**
	 * Searches for a specific Activity in the tree, starting from the
	 * rootProjects, using a BFS based algorithm.
	 * @param name: name of the Activity to be found.
	 */
	public final Activity getActivity(final String name) {
		logger.debug("Getting activity: " + name);
		boolean found = false;
		Queue<Activity> nonVisited = new LinkedList<Activity>();
		nonVisited.addAll(voidProject.getChildren());
		Iterator<Activity> iter = voidProject.getChildren().iterator();
		Activity activity = null;

		while (!found && iter.hasNext()) {
			activity = searchActivity(name, iter.next(), nonVisited);
			if (activity != null) {
				found = true;
			}
		}
		return activity;
	}

	/**
	 * Searches for a specific Activity in the tree.
	 * @param name: name of the Activity to be found.
	 * @param activity: actual Activity that is being checked.
	 */
	private Activity searchActivity(final String name, final Activity activity,
			final Queue<Activity> nonVisited) {
		// There's at least one element to visit (the current one)
		if (!nonVisited.isEmpty()) {
			nonVisited.remove();
			if (name.equals(activity.getName())) {
				return activity;

			} else if (activity instanceof Project) {
				// keep searching
				Collection<Activity> children = activity.getChildren();
				if (!children.isEmpty()) {
					nonVisited.addAll(children);
				}
				return searchActivity(name, nonVisited.peek(), nonVisited);

			} else {
				return searchActivity(name, nonVisited.peek(), nonVisited);
			}

		} else {
			logger.debug("nonVisited queue is empty");
			return null;
		}
	}

	/**
	 * Menu that lets swap between the printing of the Tree of Activities and
	 * the subMenu with the options.
	 */
	public final void printMenu() {

		Scanner scanner = new Scanner(System.in);
		int option = -1;

		while (option != 0) {

			Impresor.getInstance().reanudate();
			Thread impresorThread = new Thread(Impresor.getInstance());
			impresorThread.start();
			
			final int subMenu = 1, testa1 = 2, testa2 = 3, 
					testfita2 = 4, exit = 0;
			
			boolean correctType = false;
			while (!correctType) {
				try {
					option = Integer.parseInt(scanner.nextLine());
					logger.debug("chosen option: " + option);
					correctType = true;
				} catch (Exception e) {
					logger.debug("chosen option: " + option);
					logger.warn("Introduced value is not a valid value. "
							+ "Introducing new value");
					System.out.print("Introduced value is not a number. "
							+ "Please enter a number:");
				}
			}

			switch (option) {
			case subMenu:	// 1
				try {
					Impresor.getInstance().terminate();
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				printSubMenu();
				break;

			case testa1:	// 2
				this.testA1(impresorThread);
				break;

			case testa2:	// 3
				this.testA2(impresorThread);
				break;
				
			case testfita2:	// 4
				this.testFita2(impresorThread);
				break;

			case exit:		// 0
				logger.debug("Exit requested");
				Impresor.getInstance().terminate();
				try {
					impresorThread.join();
					logger.debug("impresorThread join executed");
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.error("error trying to join the impresorThread");
				}
				break;

			default:
				System.out.println("Error. Invalid option");
				break;
			}
		}
		logger.debug("Closing Scanner");
		scanner.close();
	}

	private void testA1(final Thread impresorThread) {
		Clock.getInstance().setRefreshTime(2);
		final int waitTimeA1 = 3000;
		final int waitTimeA2 = 7000;
		final int waitTimeA3 = 10000;
		final int waitTimeA4 = 2000;
		try {
			Impresor.getInstance().terminate();
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Project p1 = new Project("P1", " ", null);
		this.voidProject.addExistingChildProject(p1);

		Project p2 = new Project("P2", " ", p1);
		p1.testAddChild(p2);

		Task t3 = new SimpleTask("T3", " ", p1);
		p1.testAddChild(t3);
		Clock.getInstance().getNotification().addObserver(t3);

		Task t1 = new SimpleTask("T1", " ", p2);
		p2.testAddChild(t1);
		Clock.getInstance().getNotification().addObserver(t1);

		Task t2 = new SimpleTask("T2", " ", p2);
		p2.testAddChild(t2);
		Clock.getInstance().getNotification().addObserver(t2);

		System.out.println("Time will count every 2 seconds.");
		Impresor.getInstance().setReprintTime(2);
		Impresor.getInstance().reanudate();
		Thread impresorThread2 = new Thread(Impresor.getInstance());
		impresorThread2.start();

		t3.start();

		try {
			Thread.sleep(waitTimeA1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t3.stop();

		try {
			Thread.sleep(waitTimeA2);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t2.start();

		try {
			Thread.sleep(waitTimeA3);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t2.stop();

		t3.start();

		try {
			Thread.sleep(waitTimeA4);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t3.stop();

		try {
			Impresor.getInstance().terminate();
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		System.out.println("");
		System.out.println("Test A.1 finished!");
		System.out.println("");

	}

	private void testA2(final Thread impresorThread) {
		Clock.getInstance().setRefreshTime(2);
		final int waitTimeA1 = 4000;
		final int waitTimeA2 = 2000;
		final int waitTimeA3 = 2000;
		final int waitTimeA4 = 4000;
		final int waitTimeA5 = 2000;
		final int waitTimeA6 = 4000;
		final int waitTimeA7 = 2000;

		try {
			Impresor.getInstance().terminate();
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Project p1 = new Project("P1", " ", null);
		this.voidProject.addExistingChildProject(p1);

		Project p2 = new Project("P2", " ", p1);
		p1.testAddChild(p2);

		Task t3 = new SimpleTask("T3", " ", p1);
		p1.testAddChild(t3);
		Clock.getInstance().getNotification().addObserver(t3);

		Task t1 = new SimpleTask("T1", " ", p2);
		p2.testAddChild(t1);
		Clock.getInstance().getNotification().addObserver(t1);

		Task t2 = new SimpleTask("T2", " ", p2);
		p2.testAddChild(t2);
		Clock.getInstance().getNotification().addObserver(t2);

		System.out.println("Time will count every 2 seconds.");

		Impresor.getInstance().setReprintTime(2);
		Impresor.getInstance().reanudate();
		Thread impresorThread2 = new Thread(Impresor.getInstance());
		impresorThread2.start();

		t3.start();

		try {
			Thread.sleep(waitTimeA1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t2.start();

		try {
			Thread.sleep(waitTimeA2);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t3.stop();

		try {
			Thread.sleep(waitTimeA3);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t1.start();

		try {
			Thread.sleep(waitTimeA4);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t1.stop();

		try {
			Thread.sleep(waitTimeA5);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t2.stop();

		try {
			Thread.sleep(waitTimeA6);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t3.start();

		try {
			Thread.sleep(waitTimeA7);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t3.stop();

		try {
			Impresor.getInstance().terminate();
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		System.out.println("");
		System.out.println("Test A.2 finished!");
		System.out.println("");
	}
	
	private void testFita2(final Thread impresorThread) {
		Clock.getInstance().setRefreshTime(1);
		final int waitTime1 = 4000;
		final int waitTime2 = 6000;
		final int waitTime3 = 4000;
		final int waitTime4 = 2000;
		final int waitTime5 = 4000;

		try {
			Impresor.getInstance().terminate();
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Project p1 = new Project("P1", " ", this.voidProject);
		this.voidProject.addExistingChildProject(p1);

		Project p2 = new Project("P2", " ", this.voidProject);
		this.voidProject.addExistingChildProject(p2);
		
		Project p12 = new Project("P1.2", " ", p1);
		p1.addExistingChildProject(p12);
		
		Task t1 = new SimpleTask("T1", " ", p1);
		p1.testAddChild(t1);
		Clock.getInstance().getNotification().addObserver(t1);
		
		Task t2 = new SimpleTask("T2", " ", p1);
		p1.testAddChild(t2);
		Clock.getInstance().getNotification().addObserver(t2);
		
		Task t3 = new SimpleTask("T3", " ", p2);
		p2.testAddChild(t3);
		Clock.getInstance().getNotification().addObserver(t3);
		
		Task t4 = new SimpleTask("T4", " ", p12);
		p12.testAddChild(t4);
		Clock.getInstance().getNotification().addObserver(t4);
		
		Impresor.getInstance().setReprintTime(1);
		Impresor.getInstance().reanudate();
		Thread impresorThread2 = new Thread(Impresor.getInstance());
		impresorThread2.start();

		long millisWithinSecond = System.currentTimeMillis() 
				% MILISECONDS_IN_SECOND;
		try {
			Thread.sleep(MILISECONDS_IN_SECOND - millisWithinSecond);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		t1.start();
		t4.start();

		try {
			Thread.sleep(waitTime1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}
		
		
		t1.stop();
		Date startDate = Clock.getInstance().getCurrentDate();
		t2.start();
		

		try {
			Thread.sleep(waitTime2);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t2.stop();
		t4.stop();
		t3.start();

		try {
			Thread.sleep(waitTime3);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t3.stop();
		t2.start();
		Date endDate = Clock.getInstance().getCurrentDate();

		try {
			Thread.sleep(waitTime4);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t3.start();

		try {
			Thread.sleep(waitTime5);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error while putting to sleep the execution");
		}

		t2.stop();
		t3.stop();
		
		// Generate Brief Html report
		Brief briefHtmlReport = new Brief(this.voidProject, new Html(), 
				startDate, endDate, "briefHtmlReport");
		briefHtmlReport.generateReport();
		
		// Generate Brief Text report
		Brief briefTextReport = new Brief(this.voidProject, new Text(), 
				startDate, endDate, "briefTextReport");
		briefTextReport.generateReport();
		
		// Generate Detailed Html report
		Detailed detailedHtmlReport = new Detailed(this.voidProject, 
				new Html(), startDate, endDate, "detailedHtmlReport");
		detailedHtmlReport.generateReport();
		
		// Generate Detailed Text report
		Detailed detailedTextReport = new Detailed(this.voidProject, 
				new Text(), startDate, endDate, "detailedTextReport");
		detailedTextReport.generateReport();

		System.out.println("");
		System.out.println("		Test Fita 2 finished!");
		System.out.println("");
	}

	/**
	 * Adds a rootProject to the rootProjects list.
	 */
	public final void addRootProject() {
		ArrayList<String> properties = new ArrayList<String>();
		logger.debug("adding root project");
		logger.debug("asking project properties");
		properties = askRootProjectProperties();

		Project p = new Project(properties.get(0), properties.get(1), null);
		this.voidProject.addExistingChildProject(p);
		logger.info("root project " + p.getName() + " added");
		Impresor.getInstance().setRootProjects(this.voidProject.getChildren());
	}

	/**
	 * Adds a child Project to an existing Project.
	 */
	public final void addChildProject() {
		Scanner scanner = new Scanner(System.in);

		Project fatherProject = null;
		String fatherName = "";

		logger.debug("Option add child project selected");
		logger.debug("introducing name of father project");
		System.out.print("Enter the name of the Father Project: ");

		boolean correctType = false;
		while (!correctType) {
			try {
				fatherName = scanner.nextLine();
				logger.debug("searching father project " + fatherName);
				fatherProject = (Project) getActivity(fatherName);
				correctType = true;
			} catch (Exception e) {
				logger.warn("Trying to add project into task.");
				logger.debug("Introducing new name of Father Project.");
				System.out.println("You can't add project into task. "
						+ "Please introduce a project name:");
			}
		}

		if (fatherProject != null) {
			logger.debug("father " + fatherName + " has been found");
			logger.debug("Adding child Project to " + fatherName);
			fatherProject.addChildProject(this);
		} else {
			logger.info("The specified Father Project does not exist.");
			System.out.println("Error. "
					+ "The specified Father Project does not exist.");
		}

	}

	/**
	 * Adds a child Task to an existing Project.
	 */
	public final void addChildTask() {

		Scanner scanner = new Scanner(System.in);

		Project fatherProject = null;
		String fatherName = "";

		logger.debug("adding task to project");
		logger.debug("introducing name of father project");

		boolean correctType = false;
		while (!correctType) {
			try {
				System.out.print("Enter the name of the Father Project: ");
				fatherName = scanner.nextLine();
				logger.debug("searching father project " + fatherName);
				fatherProject = (Project) getActivity(fatherName);
				correctType = true;
			} catch (Exception e) {
				logger.warn("Trying to add project into task.");
				logger.debug("Introducing new name of Father Project.");
				System.out.println("You can't add task into task. "
						+ "Please introduce a project name:");
			}
		}

		if (fatherProject != null) {
			logger.debug("father " + fatherName + " has been found");
			logger.debug("Adding child Task to " + fatherName);
			fatherProject.addChildTask(this);
		} else {
			logger.info("The specified Father Project does not exist.");
			System.out.println("Error. "
					+ "The specified Father Project does not exist.");
		}
	}

	public final void startTask() {
		Scanner scanner = new Scanner(System.in);
		logger.debug("Option start task");
		logger.debug("introducing name of the task");
		System.out.print("Enter the name of the Task: ");

		String fatherName = "";
		Task task = null;

		boolean correctType = false;
		while (!correctType) {
			try {
				fatherName = scanner.nextLine();
				logger.debug("task name introduced: " + fatherName);
				logger.debug("searching task " + fatherName);
				task = (Task) getActivity(fatherName);
				correctType = true;
			} catch (Exception e) {
				logger.warn(fatherName + " is a project, not a task.");
				logger.warn("introducing a new task to find.");
				System.out.print("You need to choose a task, not a project."
						+ " Please introduce task name: ");
			}
		}

		if (task != null) {
			logger.debug("task " + fatherName + " has been found");
			logger.debug("Starting Task " + fatherName
					+ " by adding new Interval");
			task.start();
		} else {
			logger.debug("The specified task does not exist.");
			System.out.println("Error. The specified Task does not exist.");
		}
	}

	public final void stopTask() {
		Scanner scanner = new Scanner(System.in);
		String fatherName = "";
		Task task = null;

		logger.debug("stoping interval");
		logger.debug("introducing name of the task");
		System.out.print("Enter the name of the Task: ");

		boolean correctType = false;
		while (!correctType) {
			try {
				fatherName = scanner.nextLine();
				logger.debug("task name introduced: " + fatherName);
				logger.debug("searching task " + fatherName);
				task = (Task) getActivity(fatherName);
				correctType = true;
			} catch (Exception e) {
				logger.warn(fatherName + " is a project, not a task.");
				logger.warn("introducing a new task to find.");
				System.out.print("You need to choose a task, not a project. "
						+ "Please introduce task name: ");
			}
		}

		task = (Task) getActivity(fatherName);

		if (task != null) {
			logger.debug("task " + fatherName + "has been found");
			logger.info("Task " + task.getName() + " stopped");
			task.stop();
		} else {
			logger.debug("The specified task does not exist.");
			System.out.println("Error. The specified Task does not exist.");
		}
	}

	public final void generateReport() {
		logger.debug("Generating report");
		
		System.out.print("Choose the type of report to " 
				+ "generate (0 brief, 1 detailed): ");
		Scanner scanner = new Scanner(System.in);
		boolean correctValue = false, correctType = false;
		int reportType = 0;
		while (!correctValue) {
			correctType = false;
			while (!correctType) {
				try {
					reportType = Integer.parseInt(scanner.nextLine());
					logger.debug("chosen option: " + reportType);
					correctType = true;
					if (reportType == 1 || reportType == 0) {
						correctValue = true;
					} else {
						System.out.println("The introduced value "
								+ "is not valid.");
						System.out.print("Choose the type "
								+ "of report to " 
								+ "generate (0 brief, 1 detailed): ");
					}
				} catch (Exception e) {
					logger.debug("chosen option: " + reportType);
					logger.warn("Introduced value is not a number. "
							+ "Introducing new value");
					System.out.print("Introduced value is not a number."
							+ " Please enter a number:");
				}
			}
		}
		
		System.out.print("Choose the format of report to " 
				+ "generate (0 text, 1 web): ");
		correctValue = false;
		int reportFormat = 0;
		while (!correctValue) {
			correctType = false;
			while (!correctType) {
				try {
					reportFormat = Integer.parseInt(scanner.nextLine());
					logger.debug("chosen option: " + reportFormat);
					correctType = true;
					if (reportFormat == 1 || reportFormat == 0) {
						correctValue = true;
					} else {
						System.out.println("The introduced value "
								+ "is not valid.");
						System.out.print("Choose the format "
								+ "of report to " 
								+ "generate (0 text, 1 web): ");
					}
				} catch (Exception e) {
					logger.debug("chosen option: " + reportFormat);
					logger.warn("Introduced value is not a number. "
							+ "Introducing new value");
					System.out.print("Introduced value is not a number."
							+ " Please enter a number:");
				}
			}
		}
		
		Date startDate = null;
		correctType = false;
		while (!correctType) {
			try {
				logger.debug("introducing starting date" 
								+ " for schedule task");
				System.out.print("Start date (yyyy-MM-dd HH:mm): ");
				String dateString = scanner.nextLine();
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm");
				startDate = dateFormat.parse(dateString);
				logger.debug(
						"introduced date " + startDate + " correctly");
				correctType = true;
			} catch (Exception e) {
				logger.debug("Date format incorrect");
				System.out.println("Date format incorrect."
						+ " Introduce date again.");
			}
		}
		
		Date endDate = null;
		correctType = false;
		while (!correctType) {
			try {
				logger.debug("introducing starting date" 
						+ " for schedule task");
				System.out.print("End date (yyyy-MM-dd HH:mm): ");
				String dateString = scanner.nextLine();
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm");
				endDate = dateFormat.parse(dateString);
				logger.debug(
						"introduced date " + endDate + " correctly");
				correctType = true;
			} catch (Exception e) {
				logger.debug("Date format incorrect");
				System.out.println("Date format incorrect."
						+ " Introduce date again.");
			}
		}	
		
		System.out.print("Introduce the report name: ");
		String reportName = scanner.nextLine();
		
		
		Format format;
		
		if (reportFormat == 0) {
			format = new Text();
		} else {
			format = new Html();
		}
		
		Report report;
		
		if (reportType == 0) {
			report = new Brief(this.voidProject, format, 
					startDate, endDate, reportName);
		} else {
			report = new Detailed(this.voidProject, format, 
					startDate, endDate, reportName);
		}
		
		report.generateReport();
	}

	/**
	 * Menu for the actions that the user can select.
	 */
	public final void printSubMenu() {
		final int optionAddRoot = 1, optionAddChildProject = 2, 
				optionAddChildTask = 3, optionStartInterval = 4, 
				optionStopInterval = 5, optionRemoveActivity = 6,
				optionChangeReprintRate = 7, optionChangeIntervalTime = 8,
				optionGenerateReport = 9, exit = 0;

		Scanner scanner = new Scanner(System.in);
		int option = -1;

		while (option != 0) {
			System.out.println("");
			System.out.println("  - CONFIG MENU - ");
			System.out.println("");
			System.out.println("1. Add Root Project");
			System.out.println("2. Add Child Project");
			System.out.println("3. Add Child Task");
			System.out.println("4. Start Task");
			System.out.println("5. Stop Task");
			System.out.println("6. Remove Activity");
			System.out.println("7. Change Reprint Rate");
			System.out.println("8. Change minimum Interval Time");
			System.out.println("9. Generate Report");
			System.out.println("0. Return");

			logger.debug("choosing submenu option");
			System.out.println("");
			System.out.print("Enter an option: ");
			boolean correctType = false;
			while (!correctType) {
				try {
					option = Integer.parseInt(scanner.nextLine());
					logger.debug("chosen option: " + option);
					correctType = true;
				} catch (Exception e) {
					logger.debug("chosen option: " + option);
					logger.warn("Introduced value is not a number. "
							+ "Introducing new value");
					System.out.print("Introduced value is not a number. "
							+ "Please enter a number:");
				}
			}

			switch (option) {
			case optionAddRoot:
				addRootProject();
				break;

			case optionAddChildProject:
				addChildProject();
				break;

			case optionAddChildTask:
				addChildTask();
				break;

			case optionStartInterval:
				startTask();
				break;

			case optionStopInterval:
				stopTask();
				break;

			case optionRemoveActivity:
				logger.debug("removing activity");
				logger.debug("introducing name of element to eliminate");
				System.out.print("Enter name of the element to eliminate: ");
				String name = scanner.nextLine();
				Activity activity = getActivity(name);
				if (activity != null) {
					if (activity.getFather() == null) {
						logger.debug(activity.getName() + " is a root project");
						this.voidProject.removeChild(activity);
						logger.debug("Removed the root project "
								+ activity.getName());
					} else {
						logger.debug(activity.getName()
								+ " is a child activity");
						activity.getFather().removeChild(activity);
					}
				} else {
					logger.debug("activity introduced does not exist");
					System.out.println("This activity does "
							+ "not exist in the system");
				}
				break;

			case optionChangeReprintRate:
				logger.debug("Changing refresh rate");

				correctType = false;
				while (!correctType) {
					try {
						logger.debug("Introducing refresh rate in seconds");
						System.out.print("Enter the new "
								+ "refresh rate in seconds: ");
						Impresor.getInstance().setReprintTime(
								Long.parseLong(scanner.nextLine()));
						logger.info("Introduced new refresh rate");
						correctType = true;
					} catch (Exception e) {
						logger.warn("string has been "
								+ "introduced as refresh rate");
						System.out.println("Refresh rate must be a number.");
					}
				}
				break;

			case optionChangeIntervalTime:
				logger.debug("Changing minimum Interval Time");

				correctType = false;
				while (!correctType) {
					try {
						logger.debug("Introducing minimum "
								+ "interval time in seconds");
						System.out.print("Enter the new minimum "
								+ "Interval time in seconds: ");
						SimpleTask.setMinIntervalTime(Long.parseLong(scanner
								.nextLine()));
						logger.info("Introduced new minimum interval time");
						correctType = true;
					} catch (Exception e) {
						logger.warn("string has been introduced "
								+ "as minimum interval time");
						System.out.println("Minimum interval "
								+ "time must be a number.");
					}
				}
				break;
			
			case optionGenerateReport:
				generateReport();
				break;

			case exit:
				break;

			default:
				System.out.println("Error. Invalid option");
				break;
			}
		}

	}

}
