package jyrs.dev.vivesbank.movements.services;

import jyrs.dev.vivesbank.movements.models.Movement;
import jyrs.dev.vivesbank.products.bankAccounts.models.BankAccount;

import java.util.List;

public interface MovementsService {

    void createMovement(String senderClientId, String recipientClientId,
                        BankAccount origin, BankAccount destination, String typeMovement,
                        Double amount);

    void reverseMovement(String movementId);

    List<Movement> getMovementsByClientId(String clientId);
}
