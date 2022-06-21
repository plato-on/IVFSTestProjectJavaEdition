package main.java.com.platon.entity;

import lombok.*;

import java.io.File;
import java.io.FileInputStream;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NonNull
public class FileReaderEntity {

    private volatile File file;
    private volatile FileInputStream fileInputStream;

}
