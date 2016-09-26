package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import rental.Quote;
import rental.Reservation;

/**
 * Small scriptable testing system. Reads a scenario from file and executes it.
 * 
 * Supported commands
 * 
 * <command>C 
 * 	add to other command to indicate that <command> is expected to fail
 * <client> A <from> <till>
 *	Availability: check which car types are available from <from> until <till>.
 * <client> B <from> <till> <car type>  
 *	Book: request a quote for a client named <client>, for a car of type <car type> from <from> until <till>.
 * <client> F 
 * 	Finalize: finalize the quote made for client <client>
 * <client> MR
 * 	Manager, Reservations: print all reservations of client <client>
 * <client> M <type:nr>*
 * 	Manager: check total number of reservations per car type, each type <type> has <nr> reservations
 */
public abstract class AbstractScriptedSimpleTest {

	/**
	 * Check which car types are available in the given period.
	 *
	 * @param 	start
	 * 			start time of the period
	 * @param 	end
	 * 			end time of the period
	 * @throws 	Exception
	 * 			if things go wrong, throw exception
	 */
	protected abstract void checkForAvailableCarTypes(Date start, Date end) throws Exception;
	
	/**
	 * Retrieve a quote for a given car type (tentative reservation).
	 * 
	 * @param	clientName 
	 * 			name of the client 
	 * @param 	start 
	 * 			start time for the quote
	 * @param 	end 
	 * 			end time for the quote
	 * @param 	carType 
	 * 			type of car to be reserved
	 * @return	the newly created quote
	 *  
	 * @throws 	Exception
	 * 			if things go wrong, throw exception
	 */
	protected abstract Quote createQuote(String clientName, Date start, Date end, String carType) throws Exception;
	
	/**
	 * Confirm the given quote to receive a final reservation of a car.
	 * 
	 * @param 	quote 
	 * 			the quote to be confirmed
	 * @return	the final reservation of a car
	 * 
	 * @throws 	Exception
	 * 			if things go wrong, throw exception
	 */
	protected abstract Reservation confirmQuote(Quote quote) throws Exception;
	
	/**
	 * Get all reservations made by the given client.
	 *
	 * @param 	clientName
	 * 			name of the client
	 * @return	the list of reservations of the given client
	 * 
	 * @throws 	Exception
	 * 			if things go wrong, throw exception
	 */
	protected abstract List<Reservation> getReservationsBy(String clientName) throws Exception;

	/**
	 * Get the number of reservations for a particular car type.
	 * 
	 * @param 	carType 
	 * 			name of the car type
	 * @return 	number of reservations for the given car type
	 * 
	 * @throws 	Exception
	 * 			if things go wrong, throw exception
	 */
	protected abstract int getNumberOfReservationsForCarType(String carType) throws Exception;
	
	//date format to parse dates from file
	private static final DateFormat datef = new SimpleDateFormat("d/M/y");
	//name of the file containing the test script 
	private String scriptFile;
	//open sessions
	private HashMap<String, Quote> sessions = new HashMap<String, Quote>();
	
	public AbstractScriptedSimpleTest(String scriptFile) {
		this.scriptFile = scriptFile;
	}
	
	public void run()throws Exception {
		//read script
		BufferedReader in = new BufferedReader(new FileReader(scriptFile));
		int lnr = 0;
		//while we have lines
		while (in.ready()) {
			lnr++;
			
			//read line
			String line = in.readLine();
			//tokenize
			StringTokenizer scriptReader = new StringTokenizer(line, " ");
			
			//get command and name
			String name = scriptReader.nextToken();
			String cmd = scriptReader.nextToken();
			
			//dispatch according to command
			if(cmd.contains("M")) {
				if(cmd.contains("R")) {
					System.out.println("Reservations by "+name+":\n");
					int i = 1;
					for(Reservation r : getReservationsBy(name)) {
						System.out.println(i++ +") "+r.toString()+"\n");
					}
				} else {
					check(name,scriptReader);
				}
			} else if(cmd.contains("A")) {
				Date start = datef.parse(scriptReader.nextToken());
				Date end = datef.parse(scriptReader.nextToken());
				checkForAvailableCarTypes(start, end);
			} else if(cmd.contains("B")) {
				Date start = datef.parse(scriptReader.nextToken());
				Date end = datef.parse(scriptReader.nextToken());
				String type = scriptReader.nextToken();
				Exception be = null;
				try {
					sessions.put(name, createQuote(name, start, end, type));
				} catch (Exception e) {
					be = e;
				}
				boolean shouldfail = cmd.contains("C");
				if(be == null && shouldfail)
					System.err.println("command should have failed: " + line +" on line " + lnr );
				if(be != null && !shouldfail){
					System.err.println("command failed: " + line +" on line " + lnr );
					be.printStackTrace();
				}
			} else if(cmd.contains("F")){
				Quote reservation = sessions.get(name);
				if(reservation==null)
					throw new IllegalArgumentException("script broken: no quote" +  line +" on line " + lnr );	
				Exception be = null;
				try {
					Reservation r = confirmQuote(reservation);
					System.out.println("Reservation succeeded: "+r.toString());
				} catch (Exception e) {
					be = e;
				}
				boolean shouldfail = cmd.contains("C");
				if(be == null && shouldfail)
					System.err.println("command should have failed: " + line +" on line " + lnr );
				if(be != null && !shouldfail){
					System.err.println("command failed: " + line +" on line " + lnr );
					be.printStackTrace();
				}
			} else throw new IllegalArgumentException("unknown command"  + line +" on line " + lnr );
		}	
	}
	
	private void check(String name, StringTokenizer scriptReader) throws Exception {
		while(scriptReader.hasMoreTokens()){
			String pars = scriptReader.nextToken();
			String[] pair = pars.split(":");
			int nr = getNumberOfReservationsForCarType(pair[0]);
			if (Integer.parseInt(pair[1]) == nr) {
				System.out.println(name + " has correct totals " + pars + " " + nr);
			} else {
				System.err.println(name + " has wrong totals " + pars +" " + nr );
			}
		}
	}
}