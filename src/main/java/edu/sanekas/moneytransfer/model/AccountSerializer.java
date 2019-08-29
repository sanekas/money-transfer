package edu.sanekas.moneytransfer.model;

import org.apache.juneau.serializer.SerializeException;

import java.nio.ByteBuffer;

/**
 * Interface for different account serialization types
 */
public interface AccountSerializer {
    ByteBuffer serialize(Account account) throws SerializeException;
}
