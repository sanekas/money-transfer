package edu.sanekas.moneytransfer.model;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

/**
 * Interface for different account serialization types
 */
public interface AccountSerializer {
    Optional<ByteBuffer> serialize(Account account);
    Optional<ByteBuffer> serialize(List<Account> accounts);
}
