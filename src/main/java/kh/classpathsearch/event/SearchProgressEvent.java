package kh.classpathsearch.event;
import java.util.EventObject;

/**
 * Event to sigify progress with Search. Depending on value of iEventCause, event
 * can signify that event is in progress, completed with results, or completed without
 * results.
 * 
 * <p><b>(C) Copyright Kevin Hooke, 2002. All Rights Reserved.</b>
 *
 * @author Kevin Hooke, November 2002
 *
 */
public class SearchProgressEvent extends EventObject 
  {
  public static final int SEARCH_COMPLETE = 0;
  public static final int SEARCH_IN_PROGRESS = 1;
  public static final int SEARCH_EMPTY = 2;

  /**
   * source of the event
   */
  private Object source; 

  /**
   * Indicates cause of the Event
   */
  private int iEventCause = 1;

  /**
   * Value indicates progress on current task.
   */
  private int iProgressCount = 0;
  
  /**
   * Maximum number of files/directories to be searched
   */
  private int iCurrentMax = 0;

  public SearchProgressEvent(Object _source, int _cause, int _progress, int _currentMax)
    {
    super(_source);
    this.iEventCause = _cause;
    this.source = _source;
    this.iProgressCount = _progress;
    this.iCurrentMax = _currentMax;
    }

  /**
   * Returns the cause code of the event
   */
  public int getEventCause()
    {
    return iEventCause;
    }

  /**
   * Returns the current search progress count, as a number of files that have been
   * search/compared for match so far
   */
  public int getProgressCount()
    {
    return iProgressCount;
    }

  /**
   * Returns the current know maximum number of files to be searched/compared
   */
  public int getCurrentMax()
    {
    return iCurrentMax;      
    }
}