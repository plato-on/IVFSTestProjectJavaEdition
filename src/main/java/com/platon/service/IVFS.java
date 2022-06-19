package main.java.com.platon.service;

import main.java.com.platon.entity.FileReaderEntity;
import main.java.com.platon.entity.FileWriterEntity;

import java.io.IOException;

public interface IVFS {

    FileReaderEntity openReadOnlyFile(String absolutePath) throws IOException;

    FileWriterEntity openOrCreateWriteOnlyFile(String absolutePath) throws IOException;

    long readDataFromExistingFile(FileReaderEntity fileReaderEntity, char[] buffer, long len) throws IOException;

    long writeDataToExistingFile(FileWriterEntity fileWriterEntity, char[] buffer, long len) throws IOException;

    void closeFile(FileReaderEntity fileReaderEntity) throws Exception;
}
