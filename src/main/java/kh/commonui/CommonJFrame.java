package kh.commonui;

import javax.swing.*;
import java.awt.*;

/**
 * Common UI Superclass. Extends JFrame adding common UI functionality.
 *
 * <p><b>(C) Copyright Kevin Hooke, 2002. All Rights Reserved.</b>
 *
 * @author Kevin Hooke, November 2002
 *
 */
public class CommonJFrame extends JFrame
  {
  /**
   * Centers the Components _c on the current screen accoring to the current screen 
   * dimensions.
   * @param _c Component to center
   */
  public void centerWindow(Component _c)
    {
    Dimension screenSize = this.getToolkit().getScreenSize();
    int width = _c.getWidth();
    int height = _c.getHeight();
    double scrHeight = screenSize.getHeight();
    double scrWidth = screenSize.getWidth();

    _c.setLocation((int)(scrWidth/2)-(width/2),(int)(scrHeight/2)-(height/2));
    }

    /**
     * Closes the application
     */
    public void exitApp()
        {
        System.exit(0);
        }


  }
