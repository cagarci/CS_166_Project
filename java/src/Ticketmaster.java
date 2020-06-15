/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

DECLARE @
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddUser(Ticketmaster esql) throws Exception{//1
                String emails, lnames, fnames, pwds;
                emails = lnames = fnames = pwds = null;
                int phones = 0;
                boolean isCorrect = false;
                Scanner scan = new Scanner (System.in);

                //String sqlre = "INSERT INTO Users (email , lname , fname , phone , pwd) VALUES ('"+emails+"', '"+lnames+"' , '"+fnames+"', '"+phones+"', '"+pwds+"')";


                while (!isCorrect){
                        System.out.print("Add User: email, laastname, firstname, password, phonenumber:");

                        emails = scan.nextLine();
                        lnames = scan.nextLine();
                        fnames = scan.nextLine();
                        pwds = scan.nextLine();
                        phones = scan.nextInt();
                        if(emails.length() > 64 || lnames.length() > 64 || fnames.length() > 64 || pwds.length() > 64  ){
                                System.out.print("incorrect parameters!");
                        }else{
                                String sqlre = "INSERT INTO Users (email , lname , fname , phone , pwd) VALUES ('"+emails+"', '"+lnames+"' , '"+fnames+"', '"+phones+"', '"+pwds+"')";
                                esql.executeUpdate(sqlre);
                                isCorrect = true;
                        }
                }
        }
	
	public static void AddBooking(Ticketmaster esql){//2
		
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		
	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		
	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql) throws Exception{//9
                //executeQueryAndReturnResult

                int showInput = 0;
                System.out.print("Enter a show to list all theaters playing chosen show");
                Scanner scan = new Scanner (System.in);

                showInput = scan.nextInt();
                String sqlre = "SELECT tname FROM Theaters T, Plays P WHERE T.tid = P.tid AND P.sid = '"+showInput+"'";

                System.out.print(esql.executeQueryAndReturnResult(sqlre));
                System.out.print("QQ");
        }

        public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql) throws Exception{//10

                String startTime = null;
                String date = null;

                System.out.print("Enter start time and date");
                Scanner scan = new Scanner (System.in);

                startTime = scan.nextLine();
                date = scan.nextLine();

                String sqlre = "SELECT sid FROM Shows WHERE sdate = '"+date+"' AND sttime = '"+startTime+"'";

                System.out.print(esql.executeQueryAndReturnResult(sqlre));

        }

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql) throws Exception{//11
                String sqlre = "SELECT title FROM Movies WHERE title LIKE '%Love%' AND rdate >= '1/1/2010'";
                //System.out.print(esql.executeQueryAndReturnResult(sqlre));
                esql.executeQueryAndPrintResult(sqlre);

        }

        public static void ListUsersWithPendingBooking(Ticketmaster esql) throws Exception{//12
                String sqlre = "SELECT fname, lname, U.email FROM Users U, Bookings B WHERE U.email = B.email AND B.status = 'Pending'";

                esql.executeQueryAndPrintResult(sqlre);
        }

        public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql) throws Exception{//13
                String movieU = null;
                String cinemaU = null;
                String sdateRange = null;
                String edateRange = null;

                System.out.print("Enter a movie, cinema, and date in that respective order");
                Scanner scan = new Scanner (System.in);

                movieU = scan.nextLine();
                cinemaU= scan.nextLine();
                sdateRange = scan.nextLine();
                edateRange = scan.nextLine();

                String sqlre = "SELECT m.title, m.duration AS runtime, S.sdate AS date, S.sttime AS time FROM Movies M, Cinemas C, Shows S WHERE C.cname = '"+cinemaU+"' AND m.title = '"+movieU+"'"
         + " AND S.sdate >= '"+sdateRange+"' AND S.sdate < '"+edateRange+"'";

                esql.executeQueryAndPrintResult(sqlre);


        }


	public static void ListBookingInfoForUser(Ticketmaster esql) throws Exception{//14
                String userU = null;

                System.out.print("Enter email");
                Scanner scan = new Scanner (System.in);

                userU = scan.nextLine();

                String sqlre = "SELECT  M.title AS title , S.sdate AS date, S.sttime AS time, T.tname AS theater, CS.sno AS cinemaseatnumber  FROM  Movies M, Shows S , Theaters T, Plays P, Bookings B, CinemaSeats CS  WHERE B.email = '"+userU+"' AND B.sid = S.sid AND S.mvid = M.mvid AND P.sid = S.sid AND P.tid = T.tid AND P.tid = CS.tid ";

                esql.executeQueryAndPrintResult(sqlre);

        }

	
}
