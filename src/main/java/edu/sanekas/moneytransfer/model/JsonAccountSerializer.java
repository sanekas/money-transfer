package edu.sanekas.moneytransfer.model;

import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.serializer.SerializeException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public enum  JsonAccountSerializer implements AccountSerializer {
    S;

    public Optional<ByteBuffer> serialize(Account account) {
        try {
            return Optional.of(
                    ByteBuffer.wrap(
                            JsonSerializer.DEFAULT_READABLE
                                    .serialize(account)
                                    .getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (SerializeException e) {
            System.err.println("Unable to serialize account with id: " + account.getId());
            return Optional.empty();
        }
    }

    @Override
    public Optional<ByteBuffer> serialize(List<Account> accounts) {
        try {
            return Optional.of(
                    ByteBuffer.wrap(
                            JsonSerializer.DEFAULT_READABLE
                                    .serialize(accounts)
                                    .getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (SerializeException e) {
            System.err.println("Unable to serialize accounts");
            return Optional.empty();
        }
    }
}
