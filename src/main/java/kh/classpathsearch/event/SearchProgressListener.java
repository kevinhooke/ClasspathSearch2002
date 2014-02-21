package kh.classpathsearch.event;
import java.util.EventListener;

public interface SearchProgressListener extends EventListener
  {
  /**
   * Invoked at periodic intervals while a search is still in progress. This 
   * should be implemented by the Event Handler who is interested in this event
   * for monitoring the progress of the search to display a progress bar.
   *
   * <p><b>(C) Copyright Kevin Hooke, 2002. All Rights Reserved.</b>
   *
   * @author Kevin Hooke, November 2002
   *
   */
  public void searchInProgress(SearchProgressEvent event);

  /**
   * Invoked when the search is completed
   */
  public void searchComplete(SearchProgressEvent event);

  /**
   * Invoked when the search does not return any hits
   */
   public void searchResultsEmpty(SearchProgressEvent event);
  }