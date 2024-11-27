package jyrs.dev.vivesbank.products.bankAccounts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BankAccountNotFoundByIban extends BankAccountException {
    public BankAccountNotFoundByIban(String iban) {super("Producto con iban" + iban + " no encontrado");
    }
}
