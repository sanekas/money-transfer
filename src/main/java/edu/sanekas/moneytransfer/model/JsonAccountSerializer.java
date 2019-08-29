package edu.sanekas.moneytransfer.model;

import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.serializer.SerializeException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public enum  JsonAccountSerializer implements AccountSerializer {
    S;

    public ByteBuffer serialize(Account account) throws SerializeException {
        return ByteBuffer.wrap(
                JsonSerializer.DEFAULT_READABLE
                        .serialize(account)
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}
