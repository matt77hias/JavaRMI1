package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Set;

import rental.CarType;
import rental.ICarRentalCompany;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;

public class Client extends AbstractScriptedSimpleTest {
	
	/********
	 * MAIN *
	 ********/
	
	public static void main(String[] args) throws Exception {
		
		String carRentalCompanyName = "Hertz";
		ICarRentalCompany carRentalCompany = clientSetup("localhost", carRentalCompanyName);
		
		Client client = new Client("simpleTrips", carRentalCompany);
		client.run();
	}
	
	public static ICarRentalCompany clientSetup(String host, String companyName) {
		
		System.setSecurityManager(null);
		
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			ICarRentalCompany carRentalCompany = (ICarRentalCompany) registry.lookup(companyName);
			return carRentalCompany;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/***************
	 * CONSTRUCTOR *
	 ***************/
	
	private ICarRentalCompany carRentalCompany;
	
	public Client(String scriptFile, ICarRentalCompany carRentalCompany) {
		super(scriptFile);
		
		this.carRentalCompany = carRentalCompany;
	}
	
	public ICarRentalCompany getCarRentalCompany() {
		return this.carRentalCompany;
	}
	
	/**
	 * Check which car types are available in the given period
	 * and print this list of car types.
	 *
	 * @param 	start
	 * 			start time of the period
	 * @param 	end
	 * 			end time of the period
	 * @throws 	Exception
	 * 			if things go wrong, throw exception
	 */
	@Override
	protected void checkForAvailableCarTypes(Date start, Date end)
			throws Exception {
		
		Set<CarType> availableCarTypes = getCarRentalCompany().getAvailableCarTypes(start, end);
		for (CarType carType : availableCarTypes) {
			System.out.println(carType);
		}
	}

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
	@Override
	protected Quote createQuote(String clientName, Date start, Date end,
			String carType) throws Exception {
		
		ReservationConstraints constraints = new ReservationConstraints(start, end, carType);
		return getCarRentalCompany().createQuote(constraints, clientName);
	}

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
	@Override
	protected Reservation confirmQuote(Quote quote) throws Exception {
		
		return getCarRentalCompany().confirmQuote(quote);
	}
	
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
	@Override
	protected List<Reservation> getReservationsBy(String clientName) throws Exception {
		return getCarRentalCompany().getReservationsBy(clientName);
	}

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
	@Override
	protected int getNumberOfReservationsForCarType(String carType) throws Exception {
		return getCarRentalCompany().getNumberOfReservationsForCarType(carType);
	}
}