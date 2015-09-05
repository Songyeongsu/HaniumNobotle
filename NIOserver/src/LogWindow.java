import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class LogWindow extends JFrame {
	private int width;
	private int height;
	private JTextArea textArea = null;
	private JScrollPane pane = null;

	public LogWindow(String title, int width, int height) {
		super(title);
		setSize(width, height);
		textArea = new JTextArea();
		pane = new JScrollPane(textArea);
		getContentPane().add(pane);
		setVisible(true);
	}

	public void showInfo(String data) {
		textArea.append(data);
		this.getContentPane().validate();
	}
}

class WindowHandler extends Handler {
	private LogWindow window = null;
	private static WindowHandler handler = null;

	private WindowHandler() {
		configure();
		if (window == null)
			window = new LogWindow("Logging window", 700, 700);
	}

	public static synchronized WindowHandler getInstance() {
		if (handler == null) {
			handler = new WindowHandler();
		}
		return handler;
	}

	private void configure() {
		LogManager manager = LogManager.getLogManager();
		String className = this.getClass().getName();
		String level = manager.getProperty(className + ".level");
		String filter = manager.getProperty(className + ".filter");
		String formatter = manager.getProperty(className + ".formatter");
		setLevel(level != null ? Level.parse(level) : Level.INFO);
		setFilter(makeFilter(filter));
		setFormatter(makeFormatter(formatter));
	}

	private Filter makeFilter(String filterName) {
		Class c = null;
		Filter f = null;
		try {
			c = Class.forName(filterName);
			f = (Filter) c.newInstance();
		} catch (Exception e) {
			System.out.println("There was a problem to load the filter class: "
					+ filterName);
		}
		return f;
	}

	private Formatter makeFormatter(String formatterName) {
		Class c = null;
		Formatter f = null;

		try {
			c = Class.forName(formatterName);
			f = (Formatter) c.newInstance();
		} catch (Exception e) {
			f = new SimpleFormatter();
		}
		return f;
	}

	public synchronized void publish(LogRecord record) {
		String message = null;
		// check if the record is loggable
		if (!isLoggable(record))
			return;
		try {
			message = getFormatter().format(record);
		} catch (Exception e) {
			reportError(null, e, ErrorManager.FORMAT_FAILURE);
		}
		try {
			window.showInfo(message);
		} catch (Exception ex) {
			reportError(null, ex, ErrorManager.WRITE_FAILURE);
		}
	}
	public void close() {}
	public void flush() {}
}

