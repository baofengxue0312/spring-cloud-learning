package edu.dlut.springcloud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/22  16:11
 * DESCRIPTION:
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment implements Serializable {

    private Long id;

    private String serial;

}
