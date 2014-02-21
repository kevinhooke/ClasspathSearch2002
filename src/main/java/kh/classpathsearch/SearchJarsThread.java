package kh.classpathsearch;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import kh.classpathsearch.event.SearchProgressEvent;
import kh.classpathsearch.event.SearchProgressListener;


/**
* Performs the search as a seperate Thread, which may take some time to execute.
* It is run as a seperate thread to blocking the UI event handling from clicking
* the Search button.
* <p>This class generates SearchProgressEvents to inform interested parties of the 
* progress of the current search operation. Interested classes can register as a listener
* by calling the addSearchProgressListener() method.
*
* <p><b>(C) Copyright Kevin Hooke, 2002. All Rights Reserved.</b>
*
* @author Kevin Hooke, April 2002
*
*/
class SearchJarsThread extends Thread
	{
	private String sFileName;
  private ArrayList listZipsToSearch;
  private ArrayList listResults;
  private int iProgressCount = 0;
  private int iCurrentMaxFiles = 0;

  private ArrayList registeredListeners = new ArrayList(5);
  
  //private static final Logger LOGGER = Logger.getLogger("kh.classpathsearch");

  /**
   * Default constructor called by ClasspathSearchApp. 
   */
  public SearchJarsThread()
    {
    }
    

  /**
   * Main Constructor, called by SearchDialog.
   * @param _sFileName Search string of a class or files to search for
   * @param _listFiles ArrayList of the files to be searched
   * @param _listResults ArrayList to contain the results from the search
   */
	public SearchJarsThread(String _sFileName, ArrayList _listFiles, ArrayList _listResults)
		{
		this.sFileName = _sFileName;
    this.listZipsToSearch = _listFiles;
    this.iCurrentMaxFiles = _listFiles.size();
    this.listResults = _listResults;
		}


  /**
   * Runs the thread, performs the search.
   */
	public void run()
		{

    
		ArrayList listFileMatches = null;
		Iterator iter = listZipsToSearch.iterator();
		String sZipFileName = "";		
    SearchProgressEvent event = null;
    
    iProgressCount = 1;
    iCurrentMaxFiles += listZipsToSearch.size();
    
		while(iter.hasNext())
			{
      //fire progress event
      iProgressCount++;
      event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS, iProgressCount, iCurrentMaxFiles);      
      fireSearchProgressEvent(event);

			sZipFileName = (String)iter.next();
			listFileMatches = getMatchingFiles(sZipFileName, sFileName);

			if(listFileMatches.size() > 0)
				{
				listResults.addAll(listFileMatches);
				}
			}

    //fire event for empty results
		if(listResults.size() == 0)
			{
      event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_EMPTY, 
        0, iCurrentMaxFiles);
      fireSearchProgressEvent(event);
			}
    else
      {
      event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_COMPLETE, 
        iProgressCount, iCurrentMaxFiles);
      fireSearchProgressEvent(event);      
      }
		}

      
    /**
    * Gets matching file names within a Zip/Jar or directory
    * @param _searchCandidate Filename or Directory name of current object to check
    * @param _file Filename to search for
    * @return ArrayList of matching file names
    */
    public ArrayList getMatchingFiles(String _searchCandidate, String _file)
        {
        String sSearchCandidateName = _searchCandidate;
        String sFileName = _file.toLowerCase();
        ZipFile tmpZipFile;
        ArrayList listMatches = new ArrayList(100);
        SearchProgressEvent event = null;
        
        boolean bResult = false;

        try
          {
          //check if file is a zip - throws ZipException if not
          tmpZipFile = new ZipFile(sSearchCandidateName);

          //check for a direct match
          if(tmpZipFile.getEntry(sFileName) != null)
            {
            bResult = true;
            listMatches.add(sFileName);
            }
          else
            {
            //check for a partial match on a filename
            Enumeration enumFiles = tmpZipFile.entries();
            this.iCurrentMaxFiles += tmpZipFile.size();

            //fire in progress event with updated maximum file count
            event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS, 
              this.iProgressCount, this.iCurrentMaxFiles);
            fireSearchProgressEvent(event);

            String sTmpFileName = "";
            ZipEntry tmpZipEntry;
            while(enumFiles.hasMoreElements())
              {
              tmpZipEntry = (ZipEntry)enumFiles.nextElement();
              sTmpFileName = tmpZipEntry.getName();

              if(sTmpFileName.toLowerCase().lastIndexOf(sFileName) != -1)
                {
                listMatches.add(sSearchCandidateName + ": " + sTmpFileName);
                }
              this.iProgressCount++;

              //fire event
              event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS, 
                this.iProgressCount, this.iCurrentMaxFiles);
              fireSearchProgressEvent(event);
              
              }
            }//end else
          }
        catch(ZipException ze)
          {
          //file was not a zipfile, so treat as either a file or a directory
          ArrayList tmpList;
          tmpList = testFileForMatch(sSearchCandidateName, sFileName);
          if(tmpList.size() > 0)
              {
              listMatches.addAll(tmpList);
              }
          }
        catch(Exception e)
          {
          e.printStackTrace();
          bResult = false;
          }

      return listMatches;
      }


    /**
    * Checks whether candidate name is a file or a directory. If a file, checks for a match.
    * If candidate is directory, call getMatchingFiles() to check directory contents
    * @param _fileCandidate File or directory name to check
    * @param _fileName Name of file searching fors
    * @return ArrayList continaing matching filenames
    */
    private ArrayList testFileForMatch(String _fileCandidate, String _fileName)
        {
        File fTmpFile = null;
        ArrayList listResults = new ArrayList(100);
        SearchProgressEvent event = null;
        
        try
            {
            fTmpFile = new File(_fileCandidate);

            if(fTmpFile.isFile())
                {
                if(_fileCandidate.toLowerCase().indexOf(_fileName.toLowerCase()) != -1)
                    {
                    listResults.add(_fileCandidate);
                    }
                }
            if(fTmpFile.isDirectory())
                {
                ArrayList list = getContentForSource(_fileCandidate);

                //recalc max number of files
                iCurrentMaxFiles += list.size();

                //fire event for progress and new maximum file count
                event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS,
                    iProgressCount, iCurrentMaxFiles);
                fireSearchProgressEvent(event);
                
                //search through each file beneath this directory for a file match
                Iterator iter = list.iterator();
                String tmpFileName = null;

                while(iter.hasNext())
                    {
                    tmpFileName = (String)iter.next();
                    if(tmpFileName.toLowerCase().indexOf(_fileName) != -1)
                        {
                        listResults.add(_fileCandidate + ": " + tmpFileName);
                        }

                    //fire progress event 
                    iProgressCount++;
                    event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS,
                        iProgressCount, iCurrentMaxFiles);
                    fireSearchProgressEvent(event);
                    }
                }
            }
        catch(Exception e)
            {
            e.printStackTrace();
            }

        return listResults;
        }

    /**
    * Gets list of files that are contained by the source zip/archive
    * @param _name String name of either a directory, zip or jar
    * @param ArrayList containing results
    * @throws ZipException if problem encountered reading zip/jar
    */
    public ArrayList getContentForSource(String _name)
    	throws ZipException
        {
        ArrayList listFiles = new ArrayList(1000);
        SearchProgressEvent event = null;
        
        if(_name == null || _name.equals(""))
            {
            return null;
            }

        if(_name.lastIndexOf(".jar") > -1 || _name.lastIndexOf(".zip") > -1
                || _name.lastIndexOf(".JAR") > -1 || _name.lastIndexOf(".ZIP") > -1)
                {
                try
                    {
                    ZipFile tmpZipFile = new ZipFile(_name);

                    Enumeration enumFiles = tmpZipFile.entries();
                    String sFileName = "";
                    ZipEntry zipEntry;
                    int iEntries = tmpZipFile.size();

                    //fire progress event
                    event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS,
                      iProgressCount, iCurrentMaxFiles);
                    fireSearchProgressEvent(event);

                    while(enumFiles.hasMoreElements())
                        {
                        //fire progress event
                        event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS,
                          iProgressCount, iCurrentMaxFiles);
                        fireSearchProgressEvent(event);
                        
                        zipEntry = (ZipEntry)enumFiles.nextElement();
                        sFileName = zipEntry.getName();
                        listFiles.add(sFileName);
                        }

                    }
                catch(Exception e)
                    {
                    e.printStackTrace();
                    if(e instanceof ZipException)
                        {
                        throw (ZipException)e;
                        }
                    }
                }
            else
                {
                //try to get list of files for a directory
                try
                    {
                    File tmpFile = new File(_name);

                    if(tmpFile.isFile())
                        {
                        listFiles.add(_name);

                        //fire progress event
                        iProgressCount++;
                        event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS,
                          iProgressCount, iCurrentMaxFiles);
                        fireSearchProgressEvent(event);
                        }
                    else
                        if(tmpFile.isDirectory())
                            {
                            listFiles = buildRecursiveFileList(tmpFile, listFiles);
                            }

                    }
                catch(Exception e)
                    {
                    e.printStackTrace();
                    }

                }

        return listFiles;
        }

    /**
    * Builds file list of whole directory structure.
    * <p>This method recursively calls itelf to process all subdirectories
    * @param _file Starting directory for file list
    * @param _list Current list of files
    * @param ArrayList containing all files passed in _list plus additional files in directory
    */
    private ArrayList buildRecursiveFileList(File _file, ArrayList _list)
        {
        ArrayList tmpFileList = _list;
        File tmpFile = _file;
        File[] dirContent =  tmpFile.listFiles();
        SearchProgressEvent event = null;

        int iNumFiles = dirContent.length;
        int iNewMax = 0;
        if(iNumFiles > 0)
            {
            this.iCurrentMaxFiles += iNumFiles;

            //fire search progress event
            event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS,
                this.iProgressCount, this.iCurrentMaxFiles);
            fireSearchProgressEvent(event);
            }


        //loop for content
        for(int iCurrentFile = 0; iCurrentFile < dirContent.length; iCurrentFile++)
            {
            if(dirContent[iCurrentFile].isFile())
                {
                ///fire progress event
                iProgressCount++;
                event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS,
                    this.iProgressCount, this.iCurrentMaxFiles);
                fireSearchProgressEvent(event);

                //add to list                
                tmpFileList.add(dirContent[iCurrentFile].getPath());
                }
            else
                {
                if(dirContent[iCurrentFile].isDirectory())
                    {
                    //fire progress event
                    iProgressCount++;
                    event = new SearchProgressEvent(this, SearchProgressEvent.SEARCH_IN_PROGRESS,
                        this.iProgressCount, this.iCurrentMaxFiles);
                    fireSearchProgressEvent(event);

                    tmpFileList = buildRecursiveFileList(dirContent[iCurrentFile], tmpFileList);
                    }
                }
            }
        return tmpFileList;
        }

    /**
     * Registers an interested party with this class to be notified of SearchProgressEvents
     * @param _listener Object to be notified when a SearchProgressEvent fires
     */
    public void addSearchProgressListener(Object _listener)
      {
      if(_listener != null)
        {
        registeredListeners.add(_listener);
        }
      }
      
    /**
     * Removes an Object from the SearchProgressEvent registered listeners list.
     * @param _listener Listener to be removed
     */
    public void removeSearchProgressListener(Object _listener)
      {
      registeredListeners.remove(_listener);
      }


    /**
     * Fires an event on each of the registered listeners
     * @param _event New event fired
     */
    private void fireSearchProgressEvent(SearchProgressEvent _event)
      {
      Iterator iter = registeredListeners.iterator();
      Object listener = null;
      while(iter.hasNext())
        {
        listener = iter.next();
        if(listener instanceof SearchProgressListener)
          {
          switch(_event.getEventCause())
            {
            case SearchProgressEvent.SEARCH_IN_PROGRESS:
              {
              ((SearchProgressListener)listener).searchInProgress(_event);
              break;
              }

            case SearchProgressEvent.SEARCH_EMPTY:
              {
              ((SearchProgressListener)listener).searchResultsEmpty(_event);
              break;
              }

            case SearchProgressEvent.SEARCH_COMPLETE:
              {
              ((SearchProgressListener)listener).searchComplete(_event);
              break;
              }
            }            
          }//end if SearchProgressListener
        }//end while loop
      }

    
	}
