package com.mahmoud.ledger.api.web;

import com.mahmoud.ledger.application.port.in.CreateAccountCommand;
import com.mahmoud.ledger.application.port.in.CreateAccountUseCase;
import com.mahmoud.ledger.application.port.in.PostTransactionCommand;
import com.mahmoud.ledger.application.port.in.PostTransactionUseCase;
import com.mahmoud.ledger.application.port.in.RetrieveAccountUseCase;
import com.mahmoud.ledger.application.port.in.TransferFundsCommand;
import com.mahmoud.ledger.application.port.in.TransferFundsUseCase;
import com.mahmoud.ledger.domain.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LedgerController {

    private final CreateAccountUseCase createAccountUseCase;
    private final PostTransactionUseCase postTransactionUseCase;
    private final RetrieveAccountUseCase retrieveAccountUseCase;
    private final TransferFundsUseCase transferFundsUseCase;
    private final com.mahmoud.ledger.application.port.in.DepositFundsUseCase depositFundsUseCase;

    @PostMapping("/accounts")
    public ResponseEntity<UUID> createAccount(@RequestBody CreateAccountCommand command) {
        return ResponseEntity.ok(createAccountUseCase.createAccount(command));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        Account account = retrieveAccountUseCase.getAccount(id);
        return ResponseEntity.ok(new AccountResponse(
                account.getId(),
                account.getName(),
                account.getBalance().currency().getCurrencyCode(),
                account.getBalance().amount(),
                account.getStatus().name()));
    }

    @PostMapping("/transactions")
    public ResponseEntity<UUID> postTransaction(@RequestBody PostTransactionCommand command) {
        return ResponseEntity.ok(postTransactionUseCase.postTransaction(command));
    }

    @PostMapping("/transfers")
    public ResponseEntity<UUID> transferFunds(@RequestBody TransferFundsCommand command) {
        return ResponseEntity.ok(transferFundsUseCase.transferFunds(command));
    }

    @PostMapping("/deposits")
    public ResponseEntity<UUID> depositFunds(
            @RequestBody com.mahmoud.ledger.application.port.in.DepositFundsCommand command) {
        return ResponseEntity.ok(depositFundsUseCase.depositFunds(command));
    }

    record AccountResponse(UUID id, String name, String currency, java.math.BigDecimal balance, String status) {
    }
}
