package kh.classpathsearch;


import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;

import kh.classpathsearch.event.SearchProgressEvent;
import kh.classpathsearch.event.SearchProgressListener;

/**
* Displays a Dialog window for searching the current classpath and zips/jars.
* <p>User enters a search value in the text field and presses search. The results
* are displayed in the list.
*
* <p><b>(C) Copyright Kevin Hooke, 2002. All Rights Reserved.</b>
*
* @author Kevin Hooke, April 2002
*/

public class SearchDialog extends JDialog implements SearchProgressListener
    {
    private JLabel      lblSearch = new JLabel("Enter fully qualified or partial name of class or filename to search for, ");
    private JLabel      lblSearch2 = new JLabel("eg somepackage.package.SomeClass.class, or SomeClassName :");
    private JTextField  txtSearch = new JTextField();
    private JButton     btnSearch = new JButton("Search now");
    private JButton     btnClear = new JButton("Clear results");
    private JScrollPane scrResults;
    private JList   lstResults = new JList();
    private ClasspathSearchApp frmParent;
    private ProgressMonitor progressMonitor = null;
    private SearchJarsThread searchThread = null;
    
    /**
     * Stores results list from search
     */
    private ArrayList listResults;
    
    //private static final Logger LOGGER = Logger.getLogger("kh.classpathsearch");
    
    private ArrayList listFiles; //files to be searched
    private static final String BTN_SEARCH = "search";
    private static final String BTN_CLEAR = "clear";

    /**
     * Builds UI and initializes app.
     * @param _parentFrame to be replaced
     * @param _listFiles ArrayList containing a list of all the directories, jars and zips
     * to be searched
     */
    public SearchDialog(ClasspathSearchApp _parentFrame, ArrayList _listFiles)
        {
        super((JFrame)_parentFrame, "Classpath Search");
        frmParent = _parentFrame;
        listFiles = _listFiles;
        
        btnSearch.setActionCommand(BTN_SEARCH);
        btnClear.setActionCommand(BTN_CLEAR);

        Container c = this.getContentPane();
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        c.setLayout(gb);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 3;
        gb.setConstraints(lblSearch, constraints);
        c.add(lblSearch);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridwidth = 3;
        gb.setConstraints(lblSearch2, constraints);
        c.add(lblSearch2);


        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        gb.setConstraints(txtSearch, constraints);
        txtSearch.setPreferredSize(new Dimension(250,25));
        c.add(txtSearch);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        gb.setConstraints(btnClear, constraints);
        c.add(btnClear);

        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        gb.setConstraints(btnSearch, constraints);
        c.add(btnSearch);


        scrResults = new JScrollPane(lstResults);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weighty = 1;
        constraints.gridwidth = 3;
        constraints.gridheight = 10;
        constraints.insets = new Insets(10,0,0,0);

        constraints.fill = GridBagConstraints.BOTH;
        gb.setConstraints(scrResults, constraints);
        c.add(scrResults);
        ButtonHandler buttonHandler = new ButtonHandler();

        btnSearch.addActionListener(buttonHandler);
        btnClear.addActionListener(buttonHandler);
        this.getRootPane().setDefaultButton(btnSearch);
        this.setSize(600,300);
        //this.setTitle(_title);
        frmParent.centerWindow(this);
        this.show();
        }


    /**
     * Clears the search results and search value input field
     */
    private void clearResults()
        {
        clearListResults();
        txtSearch.setText("");
        }

    /**
     * Clears the search results displayed in the dialog.
     */
    private void clearListResults()
        {
        lstResults.setListData(new Object[1]);
        }


    /**
     * Implements method from SearchProgressListener. Called when event is fired to notify
     * that the search completed with an empty results set. Displays a 'No matches found'
     * dialog.
     */
    public void searchResultsEmpty(SearchProgressEvent _event)
      {
      //LOGGER.debug("searchResultsEmpty event notified");

      //enable search button
      btnSearch.setEnabled(true);
      
      JOptionPane.showMessageDialog(this, "No matches found",
				"Search Results", JOptionPane.INFORMATION_MESSAGE);      

      //clear up progress monitor
      progressMonitor.close();
      progressMonitor = null;
      }

    /**
     * Implements method from SearchProgressListener. Called when event is fired to notify
     * that the search is still in progress. Keeps track of the search progress with
     * a maximum file count of files to be searched and a progress count of how
     * many files have currently been searched. 
     * <p>If the search is going to continue for longer than 200ms then a progress
     * dialog is displayed. Successive SearchProgressEvents that fire update the progress
     * counts.
     * @param _event SearchProgressEvent that was fired
     */    
    public void searchInProgress(SearchProgressEvent _event)
      {
      int iCurrentProgressCount = _event.getProgressCount();
      int iCurrentMaxFiles = _event.getCurrentMax();
      try
        {
        if(_event == null)
          {
          //LOGGER.error("SearchProgressEvent is null");
          }
          
          
        //LOGGER.debug("searchInProgress event notified - progress: " + iCurrentProgressCount
        //  + ", maxfile count: " + iCurrentMaxFiles);
      
        if(progressMonitor == null)
          {
          progressMonitor = new ProgressMonitor(SearchDialog.this, "Searching Files",
            "Please wait... searching jars, zips and directories", iCurrentProgressCount, iCurrentMaxFiles);
          progressMonitor.setMillisToDecideToPopup(50);        

          }
        else
          {
          //update ProgressMonitor with current status
          progressMonitor.setMaximum(iCurrentMaxFiles);
          progressMonitor.setProgress(iCurrentProgressCount);
          }
        }
      catch(Exception e)
        {
        //LOGGER.error("Unknown error during progress event handling: " + e.getMessage());
    	  e.printStackTrace();
        }
        
      }


    /**
     * Implements method from SearchProgressListener. Called when event is fired to notify
     * that the search is complete and that matches were found.
     */    
    public void searchComplete(SearchProgressEvent _event)
      {
      String sResultMsg = null;
      int iMatches = 0;
      //LOGGER.debug("searchComplete event notified");

      //enable search button
      btnSearch.setEnabled(true);

      iMatches = listResults.size();
      
      if(iMatches == 1)
        {
        sResultMsg = iMatches + " match found";
        }
      else
        {
        sResultMsg = iMatches + " matches found";
        }
      JOptionPane.showMessageDialog(this, sResultMsg,
    			"Search Results", JOptionPane.INFORMATION_MESSAGE);    
          
      //clear up progress monitor
      progressMonitor.close();
      progressMonitor = null;

      //update list with results
      lstResults.setListData(listResults.toArray());
      }


    /**
     * Inner class inplementing ActionListen to handle button events.
     */
    class ButtonHandler implements ActionListener
        {
        public void actionPerformed(ActionEvent e)
            {
            String sAction = e.getActionCommand();

            if(sAction.equals(BTN_SEARCH))
                {
                if(txtSearch != null)
                    {
                    clearListResults();

                    //disable the search button until search is complete (button is
                    //enabled in event listener code
                    btnSearch.setEnabled(false);
                    
                    listResults = new ArrayList(500);
                    searchThread = new SearchJarsThread(txtSearch.getText(),
                        listFiles, listResults);
                    searchThread.start();

                    //register outer class as listener for generated progress events
                    searchThread.addSearchProgressListener(SearchDialog.this);
                    }
                }
            else if (sAction.equals(BTN_CLEAR))
                {
                clearResults();
                }
            }
        }

    }