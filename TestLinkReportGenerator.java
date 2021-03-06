/*
 *  Routine to parse logs generated by TestLink
 *  Performs these tasks
 *     1.  Read lines from a TestLink report that is in a .txt format
 *     2.  Output results in a tab delimited format that can be opened in Excel
 *  
 *   Programmed by Mark Goodell, 22 August 2014
 */

package testlinkreportgenerator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Formatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


public class TestLinkReportGenerator {

    private static final String INPUTPATH = "c:\\temp\\TestLinkReport.txt";
    private static final String OUTPUTPATH = "c:\\temp\\TestLinkReportOutput.txt";
    private Formatter output;  // output text to file
    
    private String Title;
    private String Author;
    private String PTR;
    private String PassedWithIssues;
    private String LastResult;
    private String executionNotes;
    
    
    
    public static void main(String[] args) {
        
        String newLine = " ";
        
        String msgFiles = " Input file location\n" + INPUTPATH + "\nOutput file location\n" + OUTPUTPATH;

        JOptionPane.showMessageDialog( null, msgFiles );
        
        TestLinkReportGenerator testLink = new TestLinkReportGenerator();  // do this to avoid static reference error
        
        testLink.openOutput();  // open output file
        testLink.readReportFile();  // parse data from the TestLink Repport file
        
        
    }   // end main
    
    public void openOutput()
    {

        // Open output file
        try
       {
          // System.out.println( "Output Path" + OUTPUTPATH );
           output = new Formatter( OUTPUTPATH );  // open the file
           
       }  // end try
       catch ( SecurityException securityException )
       {
           System.err.println(
             "You do not have write access to this file." );
           System.exit( 1 );  // terminate the program
       } 
       catch (FileNotFoundException fileNotFoundException) {
                  
           System.err.println(
             "File Does Not Exist - terminating program." );
           System.exit( 1 );  // terminate the program
           
       }  // end catch
        
    }  // end method openOutput
    
    public void readReportFile()
    {
       int i = 0;
       int x = 0; 
       int tmpID;
       int wordCheck;  // index location for key word searches
       String tcName = "";   // sames name of test case
       String sPTR = "";  // temporarily holds result from PTR Number field
       String lResult = ""; // holds Last rest result (pass/fail)
       String notes = "";  // holds execution notes
       String keyWord = "";  // holds keywords
       String executionDuration = "";  // holds execution duration
       String tester = "";  // name of tester
       boolean tcDone = false;  // used to determine when a test case has been parsed
       boolean kwDone = false;  // used to determine when test notes have been parsed
       boolean newTCFound = false; // flag when a new test case if found prior to finishing the previous one
       boolean endFileFound = false;  // flag when end of file is found
       boolean passWIssue = false;  
       
       String newLine = "";
       
        // Open PTR Interest List - read list then close file
        try {               
                BufferedReader bufferedReader = new BufferedReader( new FileReader( INPUTPATH ) );
                
                // print first line to output file
                 output.format("%s\t %s\t %s\t %s\t %s\t %s\n","Test Case Name", "Category", "Status", "Tester", "Comments", "PTRs" );
                
                while ( !endFileFound )
                {
                    if ( !newTCFound ) 
                        newLine = bufferedReader.readLine();
                       
                      newTCFound = false;
                      // find start of new test case
                     if (newLine.indexOf("Test Case") >0 && newLine.indexOf("Test Case") <2 )
                     {    // found new test case - print test case title
                         i++;  // count number of test cases found
                         System.out.println( newLine );
                         tcName =  newLine.substring(17);  // title starts at index of 17
                         
                         while (!tcDone)   // continue looping through lines until end of test case is found
                         {
                             newLine = bufferedReader.readLine();  // Read next line
                             if (newLine == null)
                             {
                                 endFileFound = true;
                                 tcDone = true;
                             } else
                             {
                             if (newLine.indexOf("Test Case") >0 && newLine.indexOf("Test Case") <2 )
                                                       {
                                                           //System.out.println( "Skipped a Text Case in Test Case parsing ->  " + newLine);
                                                           tcDone = true;
                                                           newTCFound = true;
                                                       }
                             if (newLine != null)  {  // just in case end of file is found
                              //  System.out.println( newLine );
                                String[] tokens = newLine.split("\\s");  // split string into tokens

                                switch ( tokens[0] )
                                {
                                    case "PTR":  // Get all the PTR numbers
                                        if (tokens[1].equals("Numbers:"))
                                        {
                                            newLine = bufferedReader.readLine();  // PTR numbers are listed in the next line
                                            if (newLine == null)
                                                {
                                                    endFileFound = true;
                                                }
                                            sPTR += newLine;

                                        } else
                                            sPTR = bufferedReader.readLine();
                                        break;

                                    case "Last":  // Last Result (pass, fail)
                                        newLine = bufferedReader.readLine();
                                        if (newLine == null)
                                            {
                                                endFileFound = true;
                                            }
                                        if (!passWIssue )
                                            lResult = newLine;   
                                        break;

                                    case "Check":  // Found Pass with Issues
                                        newLine = bufferedReader.readLine();
                                        if (newLine == null)
                                            {
                                                endFileFound = true;
                                            }
                                        wordCheck = newLine.indexOf("Passed");
                                        if (wordCheck >= 0 ) {
                                          passWIssue = true;
                                          lResult = newLine;
                                        }  // end if
                                        break;

                                    case "Execution":  // There are three lines that start with the word Execution
                                        switch ( tokens [1] )
                                        {
                                            case "type":  //Execution type is always manual
                                                break;
                                            case "duration": 
                                                executionDuration = bufferedReader.readLine();
                                                break;  
                                            case "notes":
                                                 while (!kwDone)  
                                                   {  
                                                       newLine = bufferedReader.readLine();
                                                       if (newLine == null)
                                                        {
                                                            endFileFound = true;
                                                            kwDone = true;
                                                            tcDone = true;
                                                        }
                                                       if (newLine.indexOf("v8-") >0 )
                                                       {
                                                           System.out.println( "Skipped a Text Case in Kew Word Search ->  " + newLine);
                                                       }
                                                       if (newLine.startsWith("Keywords"))
                                                       {
                                                           // save Keywords
                                                            newLine = bufferedReader.readLine();
                                                            if (newLine == null)
                                                            {
                                                                endFileFound = true;
                                                                kwDone = true;
                                                            }
                                                            keyWord = newLine;
                                                            kwDone = true;
                                                           // Don't need to search through anymore of the Test Case - mark as done
                                                           tcDone = true;
                                                       }
                                                       notes += newLine;  // newline has execution notes
                                                   } // end while

                                                 break;  

                                        }  // end switch for token[1]

                                       break; 

                                    case "Keywords:":  // Found keywords
                                        newLine = bufferedReader.readLine();
                                        if (newLine == null)
                                            {
                                                endFileFound = true;
                                                tcDone = true;
                                            }
                                        keyWord = newLine;
                                        // Don't need to search through anymore of the Test Case - mark as done
                                        tcDone = false;     
                                        break; 
                                        
                                    case "Tester":
                                        newLine = bufferedReader.readLine();
                                        if (newLine == null)
                                            {
                                                endFileFound = true;
                                                tcDone = true;
                                            }
                                        tester = newLine;
                                        break;

                                }  // end switch
                             
                             }  // end if - end of file check
                             else
                                 endFileFound = true;
                          } // end if-else for end of file check
                         }  // end While - tcDone
                         
                         // print results to output file
                         output.format("%s\t %s\t %s\t %s\t %s\t %s\n",tcName, keyWord, lResult, tester, notes, sPTR );
                             
                         // reset variables
                         kwDone = false;
                         notes = "";
                         tcDone = false;
                         passWIssue = false;
                         sPTR = "";
                        
                     }  // end if - new test case found

                }  // end while - still lines to read
                
                System.out.println("Number of Test Cases found = " + i );
                bufferedReader.close();
                output.close();
                
                String msgFiles = "Parsing Complete \n Number of Test Cases Found = " + i;
                JOptionPane.showMessageDialog( null, msgFiles );
                
         }  catch (FileNotFoundException e) {
                       e.printStackTrace();
         } catch (IOException e) {
                       e.printStackTrace();
         }  // end  catch
              
    }  // end method readPTRInterestData
    
    
}
