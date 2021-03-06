package com.techelevator.tenmo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Transfer;

@RestController
@PreAuthorize("isAuthenticated()")

//Mapping starts here
@RequestMapping(path = "/transfers")
public class TransferController
{
	@Autowired
	UserDAO userDao;
	@Autowired
	TransferDAO transferDao;

//	Mapped as a GET Method using base url of /transfers
	@GetMapping()
	public List<Transfer> getCurrentUserTransfers(Principal principal)
	{
		int id = userDao.findIdByUsername(principal.getName());
		List<Transfer> transfers = userDao.getTransfersByUser(id);
		
		return transfers;
	}
	
//	Mapped as a GET Method using /transfers/{id}
	@GetMapping("/{id}")
	public Transfer getById(@PathVariable int id)
	{
		return transferDao.get(id);
	}
	
//	Mapped as a PUT Method to update transfers using /transfers/update/{id}
	@PutMapping("/update/{id}")
	public Transfer updateTransfer(@RequestBody Transfer transfer, Principal principal, @PathVariable int id)
	{
		Transfer updatedTransfer = new Transfer();
		
		if(isValidUser(transfer, principal))
		{
			updatedTransfer = transferDao.update(transfer);
		}
		return updatedTransfer;
	}
	
//	Mapped as a POST Method using base url of /transfers
	@PostMapping()
	public Transfer createTransfer(@RequestBody Transfer transfer, Principal principal)
	{
		Transfer newTransfer = new Transfer();
		
		if(isValidUser(transfer, principal))
		{
			newTransfer = transferDao.create(transfer);	
		}
		
		return newTransfer;
	}
	
	private boolean isValidUser(Transfer transfer, Principal principal)
	{
		if(principal.getName().equalsIgnoreCase(transfer.getUserFrom()) ||
				principal.getName().equalsIgnoreCase(transfer.getUserTo()))
		{
			return true;
		}
		return false;
	}
}