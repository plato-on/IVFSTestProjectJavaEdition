package main.java.com.platon.service;

import main.java.com.platon.entity.FileReaderEntity;
import main.java.com.platon.entity.FileWriterEntity;
import main.java.com.platon.exception.FileIsNullException;
import main.java.com.platon.exception.ReadBufferIsNullException;
import main.java.com.platon.exception.WriteBufferIsEmptyOrNullException;
import main.java.com.platon.exception.NothingToIOException;

import java.io.IOException;

public interface IVFS {

    FileReaderEntity openReadOnlyFile(String absolutePath) throws IOException, FileIsNullException;

    FileWriterEntity openOrCreateWriteOnlyFile(String absolutePath) throws IOException, FileIsNullException;

    long readDataFromExistingFile(FileReaderEntity fileReaderEntity, char[] buffer, long len) throws IOException, ReadBufferIsNullException, NothingToIOException;

    long writeDataToExistingFile(FileWriterEntity fileWriterEntity, char[] buffer, long len) throws IOException, WriteBufferIsEmptyOrNullException, NothingToIOException;

    void closeFile(FileReaderEntity fileReaderEntity) throws Exception;
}
