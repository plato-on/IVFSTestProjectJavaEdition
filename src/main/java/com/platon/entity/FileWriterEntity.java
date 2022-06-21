package main.java.com.platon.entity;

import lombok.*;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NonNull
public class FileWriterEntity {

    private volatile File file;

//    private volatile FileOutputStream fileOutputStream;
//    private volatile DataOutputStream dataOutputStream;
}
