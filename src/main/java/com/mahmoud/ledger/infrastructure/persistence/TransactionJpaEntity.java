package com.mahmoud.ledger.infrastructure.persistence;

import com.mahmoud.ledger.domain.model.Transaction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class TransactionJpaEntity {
    @Id
    private UUID id;

    private LocalDateTime timestamp;
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "transaction_id")
    private List<PostingJpaEntity> postings = new ArrayList<>();

    public static TransactionJpaEntity fromDomain(Transaction transaction) {
        TransactionJpaEntity entity = new TransactionJpaEntity();
        entity.setId(transaction.getId());
        entity.setTimestamp(transaction.getTimestamp());
        entity.setDescription(transaction.getDescription());

        List<PostingJpaEntity> postingEntities = transaction.getPostings().stream()
                .map(PostingJpaEntity::fromDomain)
                .collect(Collectors.toList());
        entity.setPostings(postingEntities);

        return entity;
    }

    public Transaction toDomain() {
        Transaction tx = new Transaction(id, description, timestamp);
        postings.forEach(p -> tx.addPosting(p.toDomain()));
        return tx;
    }
}
