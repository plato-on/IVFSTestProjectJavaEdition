package main.java.com.platon.service.impl;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import main.java.com.platon.entity.FileReaderEntity;
import main.java.com.platon.entity.FileWriterEntity;
import main.java.com.platon.exception.BufferTooLargeException;
import main.java.com.platon.exception.FileNameIsNullException;
import main.java.com.platon.exception.FileStatusException;
import main.java.com.platon.exception.TooManyFilesException;
import main.java.com.platon.service.IVFS;
import org.apache.commons.lang3.StringUtils;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static main.java.com.platon.constant.CoreConstants.*;

@Slf4j
@NonNull
@EqualsAndHashCode
public class IVFSImpl implements IVFS { //works with integer maxValue 2147483647 and with numbers/english-language-based files

    private volatile File readFile;
    private volatile File writeFile;
    private static volatile byte fileCount;
    public volatile FileReaderEntity fileReaderEntity = new FileReaderEntity();
    public volatile FileWriterEntity fileWriterEntity = new FileWriterEntity();

    @Override
    synchronized public FileReaderEntity openReadOnlyFile(@NonNull String absolutePath) {
        log.info(OPENING_FILE);
        isFileNameEmpty(absolutePath);
        readFile = new File(absolutePath);
        isFileNull(readFile);

        if (fileExistsAlready(readFile) & !isReaderOpenedAlready()) {
            if (readFile.setReadOnly()) {

                return generateReadEntity(readFile);
            }
            throw new FileStatusException();
        }
        System.out.println("nullptr (is opened writeOnly or does not exist)");
        return null;
    }


    @Override
    synchronized public FileWriterEntity openOrCreateWriteOnlyFile(@NonNull String absolutePath) throws IOException {
        log.info(OPENING_FILE);
        isFileNameEmpty(absolutePath);
        File file = new File(absolutePath);
        isFileNull(file);

        if (fileExistsAlready(file)) {

            if (!isWriterOpenedAlready()) {

                if (file.setWritable(true, false)) {

                    writeFile = file;
                    return generateWriteEntity(writeFile);
                }
                throw new FileStatusException();
            }

            System.out.println("nullptr (is opened in readOnly)");
            return null;
        }
        return createFile(absolutePath);
    }


    @Override
    synchronized public long readDataFromExistingFile(@NonNull FileReaderEntity fileReaderEntity, @NonNull char[] buffer, @NonNull long len) throws IOException {
        log.info(READING_FILE);
        isFileNull(fileReaderEntity.getFile());

        int convertedLen = intFromLongExtractor(len);

        if (convertedLen != -1) {
            byte[] bytesArray = new byte[convertedLen];

            try {
                int count = fileReaderEntity.getFileInputStream().read(bytesArray, 0, convertedLen);

                for (int i = 0; i < bytesArray.length; i++) { //writing results to the char[] buffer
                    char character = (char) (bytesArray[i] & 0xFF);
                    buffer[i] = character;
                }
                return count;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        throw new BufferTooLargeException();
    }


    @Override
    synchronized public long writeDataToExistingFile(@NonNull FileWriterEntity fileWriterEntity, @NonNull char[] buffer, @NonNull long len) throws IOException {
        log.info(WRITING_TO_FILE);
        isFileNull(fileWriterEntity.getFile());

        int convertedLen = intFromLongExtractor(len);
        if (convertedLen != -1) {
            byte[] bytesArray = new String(buffer).getBytes(StandardCharsets.UTF_8);

            try (FileOutputStream fileOutputStream = new FileOutputStream(writeFile, true); //java realisation feature is to close output-streams immediately
                 DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)) {
                dataOutputStream.write(bytesArray, 0, convertedLen);
                return dataOutputStream.size();
            }
        }
        throw new BufferTooLargeException();
    }


    @Override
    public void closeFile(@NonNull FileReaderEntity readerEntity) {
        log.info(CLOSE_ENTITY);
        try {
            fileReaderEntity.getFileInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized private FileWriterEntity createFile(@NonNull String absolutePath) throws IOException { //creates new file AND generates FileWriterEntity, returns it
        fileCounter(absolutePath);
        if (fileCount < 10) { //if number of files is valid
            org.apache.commons.io.FileUtils.touch(new File(absolutePath));

            return generateWriteEntity(writeFile);
        }
        throw new TooManyFilesException("System handles only 10 physical files.");
    }


    synchronized private void isFileNameEmpty(@NonNull String filename) {
        if (StringUtils.isEmpty(filename)) {
            log.error(FILENAME_IS_NULL);
        }
    }


    synchronized private void isFileNull(@NonNull File file) {
    }

    synchronized private FileReaderEntity generateReadEntity(@NonNull File readFile) {
        log.info(GENERATING_READER_ENTITY);

        fileReaderEntity.setFile(readFile);

        try {
            fileReaderEntity.setFileInputStream(new FileInputStream(readFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileReaderEntity;
    }

    synchronized private FileWriterEntity generateWriteEntity(@NonNull File writeFile) {
        log.info(GENERATING_WRITER_ENTITY);

        fileWriterEntity.setFile(writeFile);

        return new FileWriterEntity();
    }


    synchronized private boolean isReaderOpenedAlready() {
        log.info(CHECK_IF_FILE_OPENED);

        if (fileWriterEntity.getFile() == null)
            return false;

        if (fileReaderEntity.getFile() == null)
            return false;

        System.out.println(fileWriterEntity.getFile().hashCode()); //uncomment to check hashcode
        System.out.println(writeFile.hashCode());                  //uncomment to check hashcode

        if (fileReaderEntity.getFile().hashCode() == readFile.hashCode()) {
            log.error(FILE_IS_ALREADY_OPENED_IN_CURRENT_MOD);
        }

        return fileReaderEntity.getFile().hashCode() == writeFile.hashCode(); //if the File value of file(Reader/Writer)Entity is the same object with given one
    }


    synchronized private boolean isWriterOpenedAlready() {
        log.info(CHECK_IF_FILE_OPENED);

        if (fileReaderEntity.getFile() == null)
            return false;

        if (fileWriterEntity.getFile() == null)
            return false;

//        System.out.println(fileWriterEntity.getFile().hashCode()); //uncomment to check hashcode
//        System.out.println(writeFile.hashCode());                  //uncomment to check hashcode

        if (fileWriterEntity.getFile().hashCode() == writeFile.hashCode()) {
            log.error(FILE_IS_ALREADY_OPENED_IN_CURRENT_MOD);
        }

        return fileWriterEntity.getFile().hashCode() == readFile.hashCode(); //if the File value of file(Reader/Writer)Entity is the same object with given one
    }


    synchronized private boolean fileExistsAlready(File file) {
        log.info(CHECK_EXISTENCE);

        return file.exists();
    }


    static synchronized public void fileCounter(String absolutePath) {
        log.info(CHECK_AMOUNT_OF_FILES);
        try (Stream<Path> files = Files.list(Paths.get(absolutePath).getParent())) { //count amount of files into root directory
            fileCount = (byte) files.count();
            //System.out.println("Amount of files: " + fileCount); - uncomment to check filecount meaning
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized private Integer intFromLongExtractor(long theLong) { //returns -1 if theLong is too large (or null) to convert OR returns valid integer if long is valid
        log.info(EXTRACTING_LONG);
        return theLong < Integer.MAX_VALUE ? Math.toIntExact(theLong) : -1;
    }

}
