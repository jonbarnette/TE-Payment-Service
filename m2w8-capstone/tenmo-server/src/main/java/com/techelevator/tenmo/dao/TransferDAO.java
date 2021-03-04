package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.model.Transfer;

public interface TransferDAO {

	public List<Transfer> getAllTransfers(int userId);
	public Transfer getTransferById(int transactionId);
	public String sendTransfer(int userFrom, int userTo, BigDecimal amount);
	public String requestTransfer(int userFrom, int userTo, BigDecimal amount);
	public List<Transfer> getPendingRequests(int userId);
	public String updateRequest(Transfer transfer, int statusId);
	

}
