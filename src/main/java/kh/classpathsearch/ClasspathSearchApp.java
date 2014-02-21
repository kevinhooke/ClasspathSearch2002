package kh.classpathsearch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipException;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import kh.commonui.CommonJFrame;

/**
* Application to display all files, jars and zips in the current classpath.
* <p>Allows user to search option allows to search for a specific class within the whole classpath, including
* inside jars and zips.
* <p>User can add additional directories, zips and jars to the list for searching.
* <p>History: 
* <ul>
* <li>v3.0 November 2002: tidied up app structure
* <li>v2.0 April 2002: added feature to add user selected dirs, jars and zips to search
* <li>v1.0 March 2002 - initial version
* </ul>
* <p><b>(C) Copyright Kevin Hooke, 2002. All Rights Reserved.</b>
*
* @author Kevin Hooke, April 2002
*/
public class ClasspathSearchApp extends CommonJFrame
    {
    private ClasspathList classpathList = new ClasspathList();

    //ui elements
    private JLabel lblTitle = new JLabel("Select JAR or dir on the left to list files:");
    private JList lstJarList;
    private JList lstFiles = new JList();
    private JSplitPane splitPane;
    private JScrollPane scrList;
    private JScrollPane scrFiles;
    private JPanel pnlListArea = new JPanel();
    private JMenuBar mbMenu = new JMenuBar();
    private JMenu mnFile = new JMenu("File");
    private JMenu mnSearch = new JMenu("Search");
    private JMenuItem mniExit = new JMenuItem(MENU_EXIT, 'x');
    private JMenuItem mniAdd = new JMenuItem(MENU_ADD);
    private JMenuItem mniSearch = new JMenuItem(MENU_SEARCH, 's');
    private JMenu mnHelp = new JMenu("Help");
    private JMenuItem mniAbout = new JMenuItem(MENU_ABOUT);
    private JFileChooser chooser = null;
    
    /**
     * Stores last dir opened by <code>addFileOrDirToClasspath()</code> so same dir
     * is displayed on subsequent calls to this method
     */
    private File fileLastDir = null;
    
    /**
     * Stores the list of directories, jars and zips to be searched
     */
    private ArrayList listFiles;

    /**
     * First time flag for add files dialog message
     */
    private boolean bFirstTimeAddFiles = true;

    //menu constants
    private static final String MENU_ADD = "Add file or directory to list...";
    private static final String MENU_EXIT = "Exit";
    private static final String MENU_SEARCH = "Search for class or file...";
    private static final String MENU_ABOUT = "About";

    
    //private static final Logger LOGGER = Logger.getLogger("kh.classpathsearch");

    /**
     * Application constructor. Initializes UI and starts app.
     */
    public ClasspathSearchApp()
        {
        listFiles = ClasspathList.getFileNameList();
        DefaultListModel listModel = new DefaultListModel();
        initializeList(listFiles, listModel);

        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());

        c.add(lblTitle, BorderLayout.NORTH);

        pnlListArea.setLayout(new GridLayout(1,2));

        lstJarList = new JList(listModel);
        lstJarList.addMouseListener(new ListMouseHandler());
        scrList = new JScrollPane(lstJarList);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.add(scrList);

        scrFiles = new JScrollPane(lstFiles);
        //pnlListArea.add(scrFiles);
        splitPane.add(scrFiles);

        c.add(splitPane, BorderLayout.CENTER);

        //init menu
        MenuHandler menuHandler = new MenuHandler();
        mnFile.add(mniAdd);
        mniAdd.setActionCommand(MENU_ADD);
        mniAdd.addActionListener(menuHandler);
        mnFile.addSeparator();
        mnFile.add(mniExit);
        mniExit.setActionCommand(MENU_EXIT);
        mniExit.addActionListener(menuHandler);
        mbMenu.add(mnFile);
        mnSearch.add(mniSearch);
        mniSearch.setActionCommand(MENU_SEARCH);
        mniSearch.setAccelerator(KeyStroke.getKeyStroke("control S"));
        mniSearch.addActionListener(menuHandler);
        mbMenu.add(mnSearch);
        mnHelp.add(mniAbout);
        mniAbout.setActionCommand(MENU_ABOUT);
        mniAbout.addActionListener(menuHandler);
        mbMenu.add(mnHelp);
        this.setJMenuBar(mbMenu);
        this.addWindowListener(new WindowHandler());
        this.setTitle("Kevs Classpath Search Utility");
        this.setBounds(100,150, 800, 500);
        this.centerWindow(this);
        this.show();
        showIntroMessage();
        }


    public static void main(String[] args)
        {
        new ClasspathSearchApp();
        }


    /**
     * Displays the search dialog window
     */
    private void showSearchDialog()
        {
        new SearchDialog(this, listFiles);
        }


    /**
     * Initializes the list displays the zip and jar content
     */
    private void initializeList(ArrayList _list, DefaultListModel _model)
        {
        Iterator iter = _list.iterator();
        String sItem = null;

        while(iter.hasNext())
            {
            sItem = (String)iter.next();
            _model.addElement(sItem);
            }
        }


    /**
     * Displays File selection dialog so user can add additional zips, jars and or directories
     * to the search path. Displays a JOptionPane with a help message the first time this menu
     * option is selected. Keeps track of the last dir that is accessed so that on subsequent calls
     * the JFileDIalog is set with the initial directory set to the last access directory.
     */
    private void addFileOrDirToClasspath()
        {
        String sFileName;
        if(bFirstTimeAddFiles)
          {
          JOptionPane.showMessageDialog(this, "This option allows you to add a zip/jar or directory to the\nlist of files in the left list. Once added, you can click\nthat filename to browse the directory, zip or jar content. Also, this zip/jar or directory will also\nbe included in the file search");
          bFirstTimeAddFiles = false;
          }
          
        if(chooser == null)
          {
          chooser = new JFileChooser();
          }

        //restore last directory user opened files from
        if(fileLastDir != null)
          {
          chooser.setCurrentDirectory(fileLastDir);
          }
          
        if(chooser.showDialog(this, "Select directory or zip/jar to add to search list...")
            == JFileChooser.APPROVE_OPTION)
            {
            File fNew = chooser.getSelectedFile();

            if(fNew != null)
              {
              //store file/dir for next subsequent call to this method
              fileLastDir = fNew;            
              }
              
            try
                {
                //test for  directory selelcted
                File fileTest = new File(fNew.getPath());
                if(!fileTest.exists())
                    {
                    sFileName = fNew.getParent();
                    }
                else
                    {
                    sFileName = fNew.getPath();
                    }

                //add to list
                DefaultListModel model = (DefaultListModel)lstJarList.getModel();
                model.addElement(sFileName);

                //add new file/directory to list used for searching
                listFiles.add(sFileName);
                }
            catch(Exception e)
                {
                e.printStackTrace();
                }
            }
        }


    private void showIntroMessage()
        {
        JOptionPane.showMessageDialog(this, "Click on directory or jar/zip file names on left to\nbrowse content, or click 'Search' on menu to\nsearch for specific class or filename",
            "Help", JOptionPane.INFORMATION_MESSAGE);
        }

    /**
     * Retrieves the content of the selected zip/jar
     * @param _file Zip/Jar file name of the file to retrieve contents 
     */
    private void getJarContent(String _file)
      {
      try
        {
        SearchJarsThread searchJars = new SearchJarsThread();
        ArrayList listFiles = searchJars.getContentForSource(_file);
        Object[] oFileList = (Object[])listFiles.toArray();
        lstFiles.setListData(oFileList);
        }
      catch(ZipException ze)
        {
        String[] sEmpty = {""};
        JOptionPane.showMessageDialog(this, "This zip/jar/dir does not exist",
            "Error", JOptionPane.ERROR_MESSAGE);
        lstFiles.setListData(sEmpty);
        }

      }


    class WindowHandler extends WindowAdapter
        {
        public void windowClosing(WindowEvent e)
            {
            exitApp();
            }
        }


    class MenuHandler implements ActionListener
        {
        public void actionPerformed(ActionEvent e)
            {
            String sCommand = e.getActionCommand();

            if(sCommand.equals(MENU_EXIT))
                {
                exitApp();
                }
            else if(sCommand.equals(MENU_ADD))
                {
                addFileOrDirToClasspath();
                }
            else if(sCommand.equals(MENU_SEARCH))
                {
                showSearchDialog();
                }
            else if(sCommand.equals(MENU_ABOUT))
                {
                JOptionPane.showMessageDialog(ClasspathSearchApp.this,
                    "ClasspathSearchApp, v3 November 2002 \n\n(C) Copyright Kevin Hooke, 2002. \n \nThis app displays the content of the current classpath "
                    + "and provides\na search facility to search for individual classes in the classpath\nand within zips\\jars in the current classpath ",
                    "ClasspathSearchApp", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }


    /**
     * Inner class implementing MouseListener to handle MouseEvents when mouse
     * is clicked on a filename in the classpathlist.
     */
    class ListMouseHandler implements MouseListener
        {
        /**
         * Retrieves the content for the zip/jar that has been clicked/selected
         */
        public void mouseClicked(MouseEvent mouseEvent)
            {
            String sFile = (String)lstJarList.getSelectedValue();
            getJarContent(sFile);
            }

        public void mouseEntered(MouseEvent e)
            {
            }

        public void mouseExited(MouseEvent e)
            {
            }

        public void mousePressed(MouseEvent e)
            {
            }

        public void mouseReleased(MouseEvent e)
            {
            }
        }
    }