package kh.classpathsearch;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;

/**
* Provides file search and directory listing capabilities.
*
* <p><b>(C) Copyright Kevin Hooke, 2002. All Rights Reserved.</b>
*
* @author Kevin Hooke, April 2002

*/
public class ClasspathList
    {

    public ClasspathList()
        {
        }

    /**
    * Retrieves list of all directory and file names that are defined in the system Classpath
    * @return ArrayList List of all files and directories
    */
    public static ArrayList getFileNameList()
        {
        ArrayList jarList = new ArrayList(300);
        String sClasspath = (String)System.getProperty("java.class.path");
        System.out.println("Current System Classpath:\n " + sClasspath);

        StringTokenizer tokenizer = new StringTokenizer(sClasspath, ";");
        String sJarFileName;

        while(tokenizer.hasMoreElements())
            {
            sJarFileName = (String)tokenizer.nextElement();
            jarList.add(sJarFileName);
            }
        return jarList;
        }


    /**
    * Reads Zip/Jar file to test if specified file is contained with that Zip/Jar
    */
    public boolean isFileInZip(String _zip, String _file)
        {
        String sZipFileName = _zip;
        String sFileName = _file;
        ZipFile tmpZipFile;

        boolean bResult = false;

        try
          {
          tmpZipFile = new ZipFile(sZipFileName);

          //check for a direct match
          if(tmpZipFile.getEntry(sFileName) != null)
            {
            bResult = true;
            }
          else
            {
            //check for a partial match on a filename
            try
              {
              Enumeration enumFiles = tmpZipFile.entries();
              String sTmpFileName = "";
              ZipEntry tmpZipEntry;
              while(enumFiles.hasMoreElements())
                {
                tmpZipEntry = (ZipEntry)enumFiles.nextElement();
                sTmpFileName = tmpZipEntry.getName();

                if(sTmpFileName.toLowerCase().lastIndexOf(sFileName.toLowerCase()) != -1)
                  {
                  bResult = true;
                  break;
                  }
                }
              }
            catch(Exception e)
              {
              e.printStackTrace();
              }
  				}
        }
      catch(Exception e)
        {
        bResult = false;
        }


      return bResult;
      }

    }