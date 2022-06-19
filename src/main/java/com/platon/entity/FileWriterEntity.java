package main.java.com.platon.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.concurrent.Callable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FileWriterEntity {

    private volatile File file;

//    private volatile FileOutputStream fileOutputStream;
//    private volatile DataOutputStream dataOutputStream;
}
