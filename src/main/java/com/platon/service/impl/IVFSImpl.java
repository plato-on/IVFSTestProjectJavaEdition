package main.java.com.platon.service.impl;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import main.java.com.platon.entity.FileReaderEntity;
import main.java.com.platon.entity.FileWriterEntity;
import main.java.com.platon.exception.*;
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
public class IVFSImpl implements IVFS { //works with integer maxValue 2147483639 and with numbers/english-language-based files

    private static volatile byte fileCount;
    private final FileReaderEntity fileReaderEntity = new FileReaderEntity();
    private final FileWriterEntity fileWriterEntity = new FileWriterEntity();
    private volatile File readFile;
    private volatile File writeFile;


    @Override
    synchronized public FileReaderEntity openReadOnlyFile(String absolutePath) throws FileNameIsNullException, FileIsNullException {
        log.info(OPENING_FILE);

        if (!isFileNameEmptyOrNull(absolutePath)) {
            readFile = new File(absolutePath);

            if (isDir(readFile)) {
                throw new IllegalArgumentException();
            }

            if (isFileNull(readFile)) {
                throw new FileIsNullException();
            }

            if (fileExistsAlready(readFile) & !isReaderOpenedAlready()) {

                if (readFile.setReadOnly()) {

                    return setupReadEntity(readFile);
                }
                throw new FileStatusException();

            }
            log.error(NULLPTR_READFILE);
            return null; //it's bad practise, but that is in task of c++ realization
        }
        throw new FileNameIsNullException();
    }

    @Override
    synchronized public FileWriterEntity openOrCreateWriteOnlyFile(String absolutePath) throws IOException, FileIsNullException {
        log.info(OPENING_FILE);

        if (!isFileNameEmptyOrNull(absolutePath)) {
            writeFile = new File(absolutePath);
        }

        if (isDir(writeFile)) {
            throw new IllegalArgumentException();
        }

        if (isFileNull(writeFile)) {
            throw new FileIsNullException();
        }

        fileCounter(fileWriterEntity.getFile().getAbsolutePath());

        if (fileCount < 10) {

            if (fileExistsAlready(writeFile)) {

                if (!isWriterOpenedAlready()) {

                    if (writeFile.setWritable(true, false)) {

                        return setupWriteEntity(writeFile);
                    }
                    throw new FileStatusException();
                }
                log.error(NULLPTR_WRITEFILE);
            }
            return createFile(absolutePath);
        }
        throw new TooManyFilesException();
    }

    @Override
    synchronized public long readDataFromExistingFile(FileReaderEntity fileReaderEntity, char[] buffer, long len) throws IOException, ReadBufferIsNullException, NothingToIOException, FileReaderEntityIsNullException {
        log.info(READING_FILE);

        if (isReaderEntityNull(fileReaderEntity)) {
            throw new FileReaderEntityIsNullException();
        }

        if (isBufferNullOrEmpty(buffer)) {
            throw new ReadBufferIsNullException();
        }

        if (isLongEmpty(len)) {
            throw new NothingToIOException();
        }

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

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        throw new BufferTooLargeException();
    }

    @Override
    synchronized public long writeDataToExistingFile(FileWriterEntity fileWriterEntity, char[] buffer, long len) throws IOException, WriteBufferIsEmptyOrNullException, NothingToIOException, FileWriterEntityIsNullException {
        log.info(WRITING_TO_FILE);

        if (isWriterEntityNull(fileWriterEntity)) {
            throw new FileWriterEntityIsNullException();
        }

        if (isBufferNullOrEmpty(buffer)) {
            throw new WriteBufferIsEmptyOrNullException();
        }

        if (isLongEmpty(len)) {
            throw new NothingToIOException();
        }

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
    synchronized public void closeFile(FileReaderEntity readerEntity) {
        log.info(CLOSE_ENTITY);
        try {
            fileReaderEntity.getFileInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    synchronized private FileWriterEntity createFile(String absolutePath) throws IOException { //creates new file AND generates FileWriterEntity, returns it
        fileCounter(absolutePath);
        if (fileCount < 10) { //if number of files is valid
            org.apache.commons.io.FileUtils.touch(new File(absolutePath));

            return setupWriteEntity(writeFile);
        }
        throw new TooManyFilesException();
    }

    synchronized private FileReaderEntity setupReadEntity(File readFile) {
        log.info(GENERATING_READER_ENTITY);

        fileReaderEntity.setFile(readFile);

        try {
            fileReaderEntity.setFileInputStream(new FileInputStream(readFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileReaderEntity;
    }

    synchronized private FileWriterEntity setupWriteEntity(File writeFile) {
        log.info(GENERATING_WRITER_ENTITY);

        fileWriterEntity.setFile(writeFile);

        return fileWriterEntity;
    }

    synchronized private boolean isReaderOpenedAlready() {
        log.info(CHECK_IF_FILE_OPENED);

        if (fileWriterEntity.getFile() == null)
            return false;

        if (fileReaderEntity.getFile() == null)
            return false;

//        System.out.println(fileWriterEntity.getFile().hashCode()); //uncomment to check hashcode
//        System.out.println(writeFile.hashCode());                  //uncomment to check hashcode

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

    synchronized private boolean isFileNameEmptyOrNull(String filename) {
        log.info(FILENAME_IS_NULL);
        if (StringUtils.isEmpty(filename)) {
            log.error(FILENAME_IS_NULL);
            return true;
        }
        return false;
    }

    synchronized private boolean fileExistsAlready(File file) {
        log.info(CHECK_EXISTENCE);
        return file.exists();
    }

    synchronized private boolean isDir(File file) {
        log.info(CHECK_IF_FILE_IS_DIRECTORY);
        return file.isDirectory();
    }

    synchronized private boolean isFileNull(File file) {
        log.info(CHECK_IF_FILE_IS_NULL);
        return file.length() == 0;
    }

    synchronized private boolean isLongEmpty(long theLong) {
        log.info(CHECK_IF_LONG_IS_NULL);
        return theLong == 0;
    }

    synchronized private boolean isWriterEntityNull(FileWriterEntity fileWriterEntity) {
        log.info(CHECK_IF_WRITER_ENTITY_NULL);
        return fileWriterEntity == null;
    }

    synchronized private boolean isReaderEntityNull(FileReaderEntity fileReaderEntity) {
        log.info(CHECK_IF_READER_ENTITY_NULL);
        return fileReaderEntity == null;
    }

    synchronized private boolean isBufferNullOrEmpty(char[] chars) {
        log.info(CHECK_IF_BUFFER_IS_NULL_OR_EMPTY);
        if (chars == null) {
            return true;
        }
        return chars.length == 0;
    }

    synchronized private Integer intFromLongExtractor(Long theLong) { //returns -1 if theLong is too large (or null) to convert OR returns valid integer if long is valid
        log.info(EXTRACTING_LONG);
        return theLong < (Integer.MAX_VALUE - 8) ? Math.toIntExact(theLong) : -1;
    }

    public static synchronized void fileCounter(String absolutePath) {
        log.info(CHECK_AMOUNT_OF_FILES);
        try (Stream<Path> files = Files.list(Paths.get(absolutePath).getParent())) { //count amount of files into root directory
            fileCount = (byte) files.count();
            //System.out.println("Amount of files: " + fileCount); - uncomment to check filecount meaning
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
