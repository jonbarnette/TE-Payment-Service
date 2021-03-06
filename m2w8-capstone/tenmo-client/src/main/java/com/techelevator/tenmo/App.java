package com.techelevator.tenmo;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.TransferService;
import com.techelevator.tenmo.services.UserService;
import com.techelevator.view.ConsoleService;

public class App
{

	private static final String API_BASE_URL = "http://localhost:8080/";
	private static final String STATUS_APPROVED = "Approved";
	private static final String STATUS_REJECTED = "Rejected";

	private static final String MENU_OPTION_EXIT = "Exit";
	private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN,
			MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	
	private static final String PENDING_REQUESTS_OPTION_APPROVE = "Approve";
	private static final String PENDING_REQUESTS_OPTION_REJECT = "Reject";
	private static final String PENDING_REQUESTS_OPTION_CANCEL = "Don't approve or reject";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS,
			MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS,
			MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String[] PENDING_REQUESTS_MENU_OPTIONS = { PENDING_REQUESTS_OPTION_APPROVE, 
			PENDING_REQUESTS_OPTION_REJECT, PENDING_REQUESTS_OPTION_CANCEL };

	private AuthenticatedUser currentUser;
	private ConsoleService console;
	private AuthenticationService authenticationService;
	private UserService userService;
	private TransferService transferService;

	public static void main(String[] args)
	{
		App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL),
				new UserService(API_BASE_URL + "users"), new TransferService(API_BASE_URL + "transfers"));
		app.run();
	}

	public App(ConsoleService console, AuthenticationService authenticationService,
			UserService userService, TransferService transferService)
	{
		this.console = console;
		this.authenticationService = authenticationService;
		this.userService = userService;
		this.transferService = transferService;
	}

	public void run()
	{
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");

		registerAndLogin();
		mainMenu();
	}

	private void mainMenu()
	{
		while (true)
		{
			String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice))
			{
				viewCurrentBalance();
			}
			else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice))
			{
				viewTransferHistory();
			}
			else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice))
			{
				viewPendingRequests();
			}
			else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice))
			{
				sendBucks();
			}
			else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice))
			{
				requestBucks();
			}
			else if (MAIN_MENU_OPTION_LOGIN.equals(choice))
			{
				login();
			}
			else
			{
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	
//	Method allows us to view the current balance that calls code in UserService Class
	private void viewCurrentBalance()
	{
		console.displayBalance(userService.getBalance());
	}

//	Method shows transfer history based off current user.
//	Creates a list from the getAllTransfers method and uses displayTransferDetails from TransferService Class.
	private void viewTransferHistory()
	{
		List<Transfer> transfers = transferService.getAllTransfers();
		console.displayTransfers(transfers, currentUser.getUser().getUsername());
		int selection = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
		if(selection !=0)
		{
			console.displayTransferDetails(transferService.getById(selection));
		}
	}
	
//	Method shows pending transfer requests for current user.
//	Utilizes displayTransfer method to show transfers, transferStatus to set/get current status, and updateTransfer to update the current transfers.
	private void viewPendingRequests()
	{
		List<Transfer> transfers = transferService.getPendingTransfers();
		Transfer transferToUpdate = null;
		console.displayTransfers(transfers, currentUser.getUser().getUsername());
		int selection = console.getUserInputInteger("Please enter transfer ID to view approve/reject (0 to cancel)");
		if(selection !=0)
		{
			transferToUpdate = transferService.getById(selection);
			String choice = (String) console.getChoiceFromOptions(PENDING_REQUESTS_MENU_OPTIONS);
			if (!PENDING_REQUESTS_OPTION_CANCEL.equals(choice))
			{
				if (PENDING_REQUESTS_OPTION_APPROVE.equals(choice))
				{
					transferToUpdate.setTransferStatus(STATUS_APPROVED);
					transferService.updateTransfer(transferToUpdate);
				}
				else if (PENDING_REQUESTS_OPTION_REJECT.equals(choice))
				{
					transferToUpdate.setTransferStatus(STATUS_REJECTED);
					transferService.updateTransfer(transferToUpdate);
				}
			} 
		}
	}

//	Method allows user to send money to another user. Using the createTransfer method in TransferService Class.
	private void sendBucks()
	{
		int sendTo;
		
		Transfer transfer = new Transfer();
		transfer.setUserFrom(currentUser.getUser().getUsername());
		
		List<User> users = userService.getAllUsers();
		console.displayUsers(users);
		
		sendTo = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)");
		if(sendTo != 0)
		{
			for (User user : users)
			{
				if(user.getId() == sendTo)
				{
					transfer.setUserTo(user.getUsername());
					transfer.setAmount(new BigDecimal(console.getUserInput("Enter amount")));
					transfer.setTransferStatus("Approved");
					transfer.setTransferType("Send");
					transferService.createTransfer(transfer);
					break;
				}
			}
		}
	}

//	Method allows user to request money from another user. Using the createTransfer method in TransferService Class.
	private void requestBucks()
	{
		int requestFrom;
		
		Transfer transfer = new Transfer();
		transfer.setUserTo(currentUser.getUser().getUsername());
		
		List<User> users = userService.getAllUsers();
		console.displayUsers(users);
		
		requestFrom = console.getUserInputInteger("Enter ID of user you are requesting from (0 to cancel)");
		if(requestFrom != 0)
		{
			for (User user : users)
			{
				if(user.getId() == requestFrom)
				{
					transfer.setUserFrom(user.getUsername());
					transfer.setAmount(new BigDecimal(console.getUserInput("Enter amount")));
					transfer.setTransferStatus("Pending");
					transfer.setTransferType("Request");
					transferService.createTransfer(transfer);
					break;
				}
			}
		}
	}

	private void exitProgram()
	{
		System.exit(0);
	}

	private void registerAndLogin()
	{
		while (!isAuthenticated())
		{
			String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice))
			{
				login();
			}
			else if (LOGIN_MENU_OPTION_REGISTER.equals(choice))
			{
				register();
			}
			else
			{
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated()
	{
		transferService.setUser(currentUser);
		userService.setUser(currentUser);
		return currentUser != null;
	}

	private void register()
	{
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
		while (!isRegistered) // will keep looping until user is registered
		{
			UserCredentials credentials = collectUserCredentials();
			try
			{
				authenticationService.register(credentials);
				isRegistered = true;
				System.out.println("Registration successful. You can now login.");
			}
			catch (AuthenticationServiceException e)
			{
				System.out.println("REGISTRATION ERROR: " + e.getMessage());
				System.out.println("Please attempt to register again.");
			}
		}
	}

	private void login()
	{
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) // will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
			try
			{
				currentUser = authenticationService.login(credentials);
			}
			catch (AuthenticationServiceException e)
			{
				System.out.println("LOGIN ERROR: " + e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}

	private UserCredentials collectUserCredentials()
	{
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}