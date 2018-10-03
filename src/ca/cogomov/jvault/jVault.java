package ca.cogomov.jvault;

/*
 * jVault - A simple free-form text field encrypter. Useful for storing passwords. 
 * The above Sun Microsystems copyright is included as this program relies heavily on the 
 * TextFieldDemo.java demonstration program provided in the Sun Java Swing tutorials.
 *  
 * See the README file that accompanies this program for more information. Bob. 
 */
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import org.jasypt.util.text.*;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import java.lang.reflect.Method;
//import javax.jnlp.BasicService.showDocument;


public class jVault extends JFrame implements DocumentListener, ActionListener {

	private final static String about = "jVault. A simple password protector.";
	private final static String programname = "jVault";
	private final static String version = "Version 1.0.2";
	private final static String author = "Bob Walker bwalker99@gmail.com";

	private JTextField entry;
	private JLabel jLabel1;
	private JScrollPane jScrollPane1;
	private JLabel status;
	private JTextArea textArea;
	private JButton prev, next;

	final static Color HILIT_COLOR = Color.LIGHT_GRAY;
	final static Color ERROR_COLOR = Color.PINK;
	final static String CANCEL_ACTION = "cancel-search";

	final Color entryBg;
	final Highlighter hilit;
	final Highlighter.HighlightPainter painter;

	private JMenuBar menuBar;
	private JMenu menu, helpmenu;
	private JMenuItem menuItem;
	private JCheckBox casesensitive;

	private final JFileChooser fc = new JFileChooser();
	private java.io.File currentfile = null; // The currently open file. Null if none.												// if none.
	private int searchpos = 0; // the current position of the caret in the text.
	private boolean newfile = false;  // Indicates whether a New file is under construction
	private int newfilenumber = 1;    // Used to created new file names: ie untitledN 
	private boolean modified = false;  // Details whether the current file has had modifications. 
	private String password = null;  // the encrypting password for the current file. 
	private static String initialfilename = null;
	
	public jVault() {
		initComponents();
		
		hilit = new DefaultHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
		textArea.setHighlighter(hilit);
		entryBg = entry.getBackground();
		 // If an initial filename is provided at startup, call the openFile method in a separate thread. 
		// The separate thread will allow the main window to render first. Initial file name is in a global variable.
		if (initialfilename != null) { 
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    openFile(initialfilename);
				}
			});		
		}
	}
	
	/**
	 * Initialize GUI components.
	 */
	private void initComponents() {
		entry = new JTextField();
		textArea = new JTextArea();
		status = new JLabel("Welcome to jVault");
		jLabel1 = new JLabel();
		casesensitive = new JCheckBox("Case Sensitive");
		
		// The main panel for the application.
	    JPanel panel1 = new JPanel();
	    panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // provide some edge spacing
	    
	    // Subpanel to put the search box and buttons on. 
	    JPanel insetPanel1 = new JPanel();

	    textArea.getDocument().addDocumentListener(this);
	    
	    entry.setPreferredSize(new Dimension(200,25));
	    entry.setMaximumSize(new Dimension(200,25));	    
		prev = new JButton("Prev");
		prev.setPreferredSize(new Dimension(60,25));
		prev.addActionListener(this);
		next = new JButton("Next");
		next.setPreferredSize(new Dimension(60,25));
		next.addActionListener(this);
		
		// Must set this so that we can control the Window close operation completely ourselves. 
	    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
  
	    this.addWindowListener(new WindowAdapter() {
		      public void windowClosing(WindowEvent e) {				
		    	  closeApplication();		    	  
		      }
		  });
	   
		setTitle("jVault");

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(true);
		jScrollPane1 = new JScrollPane(textArea);
	    jScrollPane1.setPreferredSize(new Dimension(600, 500));	    

		jLabel1.setText("Enter text to search:");
		
		// Use a standard Border Layout
	    panel1.setLayout(new BorderLayout(10,10)); 
	    
	    // Flow the search box and buttons.
	    insetPanel1.setLayout(new BoxLayout(insetPanel1,BoxLayout.LINE_AXIS));

    	insetPanel1.add(jLabel1);
    	insetPanel1.add(Box.createRigidArea(new Dimension(5,0)));
		insetPanel1.add(entry);
    	insetPanel1.add(Box.createRigidArea(new Dimension(5,0)));
    	insetPanel1.add(casesensitive);
    	insetPanel1.add(Box.createRigidArea(new Dimension(5,0)));    	
		insetPanel1.add(prev);
    	insetPanel1.add(Box.createRigidArea(new Dimension(5,0)));
		insetPanel1.add(next);
	    
	    this.getContentPane().add(panel1, null);
	    panel1.add(insetPanel1, BorderLayout.NORTH);
	    panel1.add(jScrollPane1, BorderLayout.CENTER);
	    panel1.add(status, BorderLayout.SOUTH);
					
		setMenus();		
		// For some reason, on linux (ubuntu) was starting where I could not access the top menu
	    setLocation(50,50);
		pack();
	}

	
	
private void setMenus() {
	/**
	 * Menu Bars
	 */
	// Create the menu bar.
	menuBar = new JMenuBar();

	// File open and exit menu
	menu = new JMenu("File");
	menu.setMnemonic(KeyEvent.VK_F);
	menu.getAccessibleContext().setAccessibleDescription(
			"File Control and Exit menu.");
	menuBar.add(menu);

	// a group of JMenuItems
	menuItem = new JMenuItem("New", KeyEvent.VK_N);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
			ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription("New File");
	menuItem.addActionListener(this);
	menu.add(menuItem);

	menuItem = new JMenuItem("Open", KeyEvent.VK_O);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
			ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription("Open File");
	menuItem.addActionListener(this);
	menu.add(menuItem);

	menuItem = new JMenuItem("Save", KeyEvent.VK_S);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
			ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription("Save File");
	menuItem.addActionListener(this);
	menu.add(menuItem);

	menuItem = new JMenuItem("SaveAs", KeyEvent.VK_A);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
			ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription("Save File As");
	menuItem.addActionListener(this);
	menu.add(menuItem);		
	
	menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
			ActionEvent.ALT_MASK));	
	menuItem.getAccessibleContext().setAccessibleDescription("Exit Program");
	menuItem.addActionListener(this);
	menu.add(menuItem);

	// Help Menu
	menu = new JMenu("Help");
	menu.getAccessibleContext().setAccessibleDescription("Help Menu");
	menuBar.add(menu);

	// a group of JMenuItems
	menuItem = new JMenuItem("Help");
	menuItem.getAccessibleContext().setAccessibleDescription("Help");
	menuItem.addActionListener(this);
	menu.add(menuItem);

	menu.addSeparator();

	menuItem = new JMenuItem("About");
	menuItem.getAccessibleContext().setAccessibleDescription("About this Application");
	menuItem.addActionListener(this);
	menu.add(menuItem);

	setJMenuBar(menuBar);
}	
	
	/**
	 * Search the text for the supplied keyword. 
	 * This function is invoked by clicking one of Prev or Next buttons. 
	 * @param forward True search forward. False search backwards. 
	 */
	public void search(boolean forward) {
		
		hilit.removeAllHighlights();
		String s = entry.getText();
		if (s.length() <= 0) {
			message("Nothing to search");
			return;
		}

		String content = textArea.getText();
		if (!casesensitive.isSelected()) {   // Case Sensitive is NOT checked, so case insensitive search 
			content = content.toLowerCase();
			s = s.toLowerCase();			
		}
		
		int index = 0;
		if (forward)
			index = content.indexOf(s, searchpos);
		else
			index = content.lastIndexOf(s, searchpos);

		if (index >= 0) { // match found
			try {
				int end = index + s.length();
				hilit.addHighlight(index, end, painter);
				textArea.setCaretPosition(end);
				entry.setBackground(entryBg);
				// message("'" + s + "' found. Press ESC to end search");
				if (forward)
					searchpos = index + 1;
				else
					searchpos = index - s.length();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else {
			entry.setBackground(ERROR_COLOR);
			if (forward)
				searchpos = 0;
			else
				searchpos = content.length() - 1;
			message("Search wrapped. Press Prev/Next to continue searching...");
		}
	}

	/**
	 * Populate the information label at the bottom of the application. 
	 * @param msg The message to display. 
	 */
	void message(String msg) {
		status.setText(msg);
	}

	/**
	 * Handle all the actions(button clicks) for the application
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == prev || e.getSource() == next) {
			search(e.getSource() == next ? true : false);
			return;
		}
		JMenuItem source = (JMenuItem) (e.getSource());
		// Object source = (Object)(e.getSource());
		String sourcetext = source.getText();

		if (sourcetext.equals("About"))
			JOptionPane.showMessageDialog(this, about + "\n" + version + "\n"
					+ author, "About", JOptionPane.INFORMATION_MESSAGE);

		// Handle open button action.
		else if (sourcetext.equals("Open")) {
			openFile();
		  } 
		else if (sourcetext.equals("Save")) {
			if (newfile) 
				saveFileAs();
			else
			    saveFile();
		   }
	    else if (sourcetext.equals("SaveAs")) {
		 saveFileAs();
	    }
		else if (sourcetext.equals("New")) {
			if (modified) { 
				int retval = JOptionPane.showConfirmDialog(this,"File has been modified. Do you want to save?");
			    switch (retval) { 
			    case JOptionPane.YES_OPTION:
			    	saveFile();   // Assume the save works!
			    	break;
			    case JOptionPane.NO_OPTION:			    	
			    	break;   // No save. Fall through the switch to code below. 
			    case JOptionPane.CANCEL_OPTION:
			    	return;
			    default: 
			    	return;
			    }				                				
			}
			textArea.setText("");
			newfile = true;
			password = null;
            String newfilename = "untitled" + newfilenumber + ".txt";
            newfilenumber++; // For next time.
			currentfile = new File (newfilename); 
			message(newfilename);
			this.setTitle(programname + ":" + newfilename);
			
			// Set the currentfile variable to the new file name. 			
		    currentfile = new File(newfilename);
		    // Set the filechooser to the new (untitledN) file.  
			fc.setSelectedFile(currentfile);			
		} 
		else if (sourcetext.equals("Help")) {
			JOptionPane.showMessageDialog(this,"Help - Coming Soon...", "Help", JOptionPane.INFORMATION_MESSAGE);	
//			openURL("file:///jvault.html");
//			openURL("http://www.cogomov.ca/stuff");
		}
		else if (sourcetext.equals("Exit")) {
			closeApplication();
		}
	}
/**
 * Close the application. 
 * Prompts to save file if it has been modified.
 * TODO: Note that the return to program option does not work if called from the WindowClosing event.  
 *
 */
	private void closeApplication() {
    	
    	 // If the current file was modified. Initial dialogue to save file.
		if (modified) {  
			int retval = JOptionPane.showConfirmDialog(this,"File has been modified. Do you want to save?");
		    switch (retval) { 
		    case JOptionPane.YES_OPTION:
		    	boolean saveonclose = false;
		    	if (newfile)
		    	   saveonclose = saveFileAs();
		    	else 
		    	   saveonclose =  saveFile();
		    	if (!saveonclose) {  // Save did not work or was cancelled.
					int continueclose = JOptionPane.showConfirmDialog(this,"File was not saved. Do you still want to exit?");
			        if (continueclose == JOptionPane.YES_OPTION) // Anwsered YES to continue closing. 		
                       System.exit(0);
			        else 
			           return;   // No or Cancel. Return to program. 
		    	}
		    case JOptionPane.NO_OPTION:
		    	System.exit(0);
		    	break;
		    case JOptionPane.CANCEL_OPTION:
		    	return;
		    default: 
		    	return;
		    }
		  }
		else				// Not modified 
			System.exit(0);
	}

	/**
	 * Open a file and prompt for a file name. 
	 * @return
	 */
	private boolean openFile() { 
		return openFile(null);
	}
	/**
	 * <p>Open a file. Read file as stream of bytes, not as a text file, as
	 * the file will be an encrypted mess of bytes, not a well ordered text file
	 * with line feeds.</p>
	 * <p>Prompt for decryption password. Use the password to decrypt the file and store in global
	 * variable 'password' for when the file is saved<p>
	 *  
	 * @param filename Optional filename. If null, prompt for filename with File dialogue 
	 * @return True of file was successfully opened. False if not.
	 */
	private boolean openFile(String filename) {
		boolean retval = false;				
	
		// Check to see if current file has been changed. If so, save it. 
		if (modified) { 
			int saveoption = JOptionPane.showConfirmDialog(this,"File has been modified. Do you want to save?");
		    switch (saveoption) { 
		    case JOptionPane.YES_OPTION:
		    	if (!saveFile()) { // Save file did not work.
				    JOptionPane.showMessageDialog(this, "Error saving previous file. Can't open a new one.");
				    return false;
		    	   }
		    	currentfile = new File("");  // blank out file so nothing appears. 
		    	fc.setSelectedFile(currentfile);
		    	textArea.setText("");
		    	break;
		    case JOptionPane.NO_OPTION:
		    	currentfile = new File("");  // blank out file so nothing appears. 
		    	fc.setSelectedFile(currentfile);
		    	textArea.setText("");		    	
		    	break;   // No save. Fall through the switch to code below. 
		    case JOptionPane.CANCEL_OPTION:
		    	return false;
		    default: 
		    	return false;
		    }				                				
		}
	    
		// If file name is not supplied, prompt user.
		if (filename == null) {
		  int returnVal = fc.showOpenDialog(this);
		  if (returnVal != JFileChooser.APPROVE_OPTION) 
			 return false;
		  currentfile = fc.getSelectedFile();
		  }
		else {	// Typically supplied on command line.
			try {
			  String fileseparator = System.getProperty("file.separator");			
			  if (filename.indexOf(fileseparator) >= 0)     // File name contains a file separator (\ or /), assume absolute filename.
			    currentfile = new File(filename);			 
			  else  {
			    String currentdir = System.getProperty("user.dir");   // include calling directory when opening file.
			    currentfile = new File(currentdir,filename);
			    }
			}
			catch (Exception e) {
				message("Error opening file:" + filename);
				return false;
			 }
		    }
		
		 FileInputStream from = null;
						
			// Note : this sets the global variable called 'password'
		getPassword("<html>Enter password that corresponds to this file.<br/>" +  		
                    "This password will be used to decrypt the file.<br/>" +
                    "If it does't work, try again!</html>");
			
		if (password == null) {  // Pressed cancel or did not enter a password in getPassword 
		    JOptionPane.showMessageDialog(this, "Can't open a file, if you don't enter a password!");
		    return false;				
		}
	      message("decrypting...");  // TODO doesn't show. Probably needs to be in a different thread. 	   

	      // TODO: Need to clean up the read from a file. 
	      // I think bbuf needs to be ended (put a null at the end of input) before creating a String from it.
		try {
			from = new FileInputStream(currentfile);
			StringBuffer buf = new StringBuffer();
			byte[] bbuf = new byte[4096];
			int bytes_read;
			while ((bytes_read = from.read(bbuf)) != -1) {				
				buf.append(new String(bbuf));
				bbuf = new byte[4096];
			}
						
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(password);
				 
			// TODO the following try - catch does not work. Need to find out how. 
			//  Will through a stack trace but not an exception. 
			String dtext = null;
			try { 
			  dtext = textEncryptor.decrypt(buf.toString());
			}
			catch (EncryptionOperationNotPossibleException je) { 
				message("Error decrypting file: " + currentfile.getName());
				je.printStackTrace();
			}
				// This is the only way to tell if the decryption did not work.
			if (dtext == null) { 
				JOptionPane.showMessageDialog(this,"Error decrypting file: " + currentfile.getName() + 
						" Try again with correct password.", "Error Decrypting File", JOptionPane.ERROR_MESSAGE);
							
				message("Error decrypting file: " + currentfile.getName());
			}
			else {
			  textArea.setText(dtext);
			  textArea.setCaretPosition(0);
			  newfile = false;
			  retval = true;
			}
		  } 
		  catch (IOException e) {
			message("Error opening file: " + currentfile.getName());			  
			e.printStackTrace();
			return false;
		   }
		 finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					;
				}
		}
		modified = false;
		message("Opened file: " + currentfile.getName());
		this.setTitle(programname + ": " + currentfile.getName());

		return retval;
	}

/**
 * Save a file. Typically, if an existing file that has been opened, we already know the password (stored 
 * in the global variable 'password'), so we don't have to ask for it again.
 * Check for password anyways. Could be saving a new file.  
 * @return
 */
	private boolean saveFile() {

		if (currentfile == null) {
			int returnVal = fc.showSaveDialog(this);
             
			if (returnVal != JFileChooser.APPROVE_OPTION) {   // Did not choose Save.
               return false;
			}
			currentfile = fc.getSelectedFile();
			if (currentfile == null) {
				message("Error saving file.");
				return false;
			}
		}
		if (password == null)   // Shouldn't be. We store it for open files.  
		   getPassword("<html>Enter password that corresponds to this file.<br/>This password will be used to encrypt the file.</html>");
		   // Note : this sets the global variable called 'password'

		if (password == null) {  // Pressed cancel or did not enter a password in getPassword 
		    JOptionPane.showMessageDialog(this, "Can't Save a file if you don't enter a password!");
		    return false;				
		}
	    
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(currentfile));

			String text = textArea.getText();

			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(password);
			String myEncryptedText = textEncryptor.encrypt(text);

			output.write(myEncryptedText, 0, myEncryptedText.length());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					;
				}
		}
		message("Saved file: " + currentfile.getName());
		this.setTitle("jVault: " + currentfile.getName());  
		modified = false;
		return true;
	}

	/**
	 * Save a new or existing file with a different name. Always prompt for password. 
	 * @return
	 */
	private boolean saveFileAs() {
	
				
		int returnVal = fc.showSaveDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {  // Did not choose save.
              return false;
		  }
		currentfile = fc.getSelectedFile();
		if (currentfile == null) {
			message("No file selected for saving.");
			return false;
		   }
		
		// Note : this sets the global variable called 'password'
		getPassword("<html>Enter password that corresponds to this file.<br/>This password will be used to encrypt the file.</html>");

		if (password == null) {  // Pressed cancel or did not enter a password in getPassword 
		    JOptionPane.showMessageDialog(this, "Can't Save a file if you don't enter a password!");
		    return false;				
		}
		
		// If this was a newfile (always save as SaveAs), mark it as not new. 
		newfile = false;
		
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(currentfile));

			String text = textArea.getText();
			
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(password);
			String myEncryptedText = textEncryptor.encrypt(text);
			output.write(myEncryptedText, 0, myEncryptedText.length());
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					;
				}
		}

		message("Saved file: " + currentfile.getName());
		this.setTitle("jVault: " + currentfile.getName());
		modified = false;
		return true;
	}
	
	
	// DocumentListener methods
    // Any time the text is altered, mark the modified flag and show the user that the file has changed. 
	public void insertUpdate(DocumentEvent ev) {
		if (!modified) {
		   modified = true;
		   this.setTitle(programname + ": " + currentfile.getName() + "*");
		}		   
	}

	public void removeUpdate(DocumentEvent ev) {
		if (!modified) {
			   modified = true;
			   this.setTitle(programname + ": " + currentfile.getName() + "*");
			}		   
	}

	public void changedUpdate(DocumentEvent ev) {
		if (!modified) {
			   modified = true;
			   this.setTitle(programname + ": " + currentfile.getName() + "*");
			}		   		
	}

	/**
	 * Opens the nested class dialogue frame to get the password from the user. 
	 * There is no return value, but the global variable 'password' is set if successful or null if not
	 * 
	 * @param usertext  The text to display to the user. 
	 */
    private void getPassword(String usertext) { 

        PasswordFrame dlg = new PasswordFrame(this,usertext);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        	Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.show();
    		
        }
    	
	
	// Main method for Swing application
	public static void main(String args[]) {
		if (args.length > 0)
			initialfilename = args[0];
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				// UIManager.put("swing.boldMetal", Boolean.FALSE);
				new jVault().setVisible(true);
			}
		});
	}


	/** 
	 * Nested class to prompt for password. Can't do it as a jDialog as we want a true password field. 
	 *
	 */

	class PasswordFrame extends JDialog implements ActionListener {
	  Frame parent;
	  JPanel panel1 = new JPanel();
	  JPanel insetsPanel1 = new JPanel();
	  JPanel insetsPanel2 = new JPanel();
	  JPanel insetsPanel3 = new JPanel();
	  JButton ok = new JButton();
	  JButton cancel = new JButton();
	  JLabel label0 = new JLabel();
	  String ptext = "Enter Password:";
	  	  
	  JPasswordField Pass = new JPasswordField();

	  public PasswordFrame(Frame parent,String ptext) {
	    super(parent);
	    this.parent = parent;
	    this.ptext =  ptext;
	    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

	    try { jbInit();    }
	    catch(Exception e) {
	      e.printStackTrace();
	    }
	    pack();
	  }

	  private void jbInit() throws Exception  {
	    this.setTitle("Get Password");
	    label0.setText(ptext);
	    label0.setFont(new java.awt.Font("Dialog", 1, 14));

	    Pass.setColumns(12);

	    setResizable(false);
	    panel1.setLayout(new BorderLayout());
	    insetsPanel1.setLayout(new FlowLayout());
	    insetsPanel1.add(label0);

	    insetsPanel2.setLayout(new FlowLayout());	    
	    insetsPanel2.add(Pass);

	    insetsPanel3.setLayout(new FlowLayout());

	    ok.setText("OK");
	    ok.addActionListener(this);
	    this.getRootPane().setDefaultButton(ok);
	    cancel.setText("Cancel");
	    cancel.addActionListener(this);
	    insetsPanel3.add(ok);
	    insetsPanel3.add(cancel);

	    this.getContentPane().add(panel1, null);
	    panel1.add(insetsPanel1, BorderLayout.NORTH);
	    panel1.add(insetsPanel2, BorderLayout.CENTER);
	    panel1.add(insetsPanel3, BorderLayout.SOUTH);
	  }

	  /**
	   * Window close event. Same as cancel button.
	   */
	  protected void processWindowEvent(WindowEvent e) {
	    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
	      cancel();
	    }
	    super.processWindowEvent(e);
	  }

	 private void cancel() {
	    dispose();
	  }

	/**
	 * Process the button actions.
	 */
	  public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == cancel) {
	      password = null;	
	      cancel();
	     }
	    else if (e.getSource() == ok) {
	      char [] temp =  Pass.getPassword();
	      password = new String(temp);
	      cancel();
	      }
	  }

	} // End of nested class getPassword


	/////////////////////////////////////////////////////////
	// Bare Bones Browser Launch //
	// Version 1.5 //
	// December 10, 2005 //
	// Supports: Mac OS X, GNU/Linux, Unix, Windows XP //
	// Example Usage: //
	// String url = "http://www.centerkey.com/"; //
	// BareBonesBrowserLaunch.openURL(url); //
	// Public Domain Software -- Free to Use as You Like //
	/////////////////////////////////////////////////////////

//	public class BareBonesBrowserLaunch {

	

	public static void openURL(String url) {
    String errMsg = "Error attempting to launch web browser";
	String osName = System.getProperty("os.name");
	try {
	  if (osName.startsWith("Mac OS")) {
	   Class fileMgr = Class.forName("com.apple.eio.FileManager");
	   Method openURL = fileMgr.getDeclaredMethod("openURL",
	   new Class[] {String.class});
	   openURL.invoke(null, new Object[] {url});
	   }
	  else if (osName.startsWith("Windows"))
	    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
	   else { //assume Unix or Linux
	    String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
	    String browser = null;
	    for (int count = 0; count < browsers.length && browser == null; count++)
	       if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
	         browser = browsers[count];
	       if (browser == null)
	          throw new Exception("Could not find web browser");
	       else
	          Runtime.getRuntime().exec(new String[] {browser, url});
	     }
	   }
	catch (Exception e) {
	     JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
	    }
	}
  
}