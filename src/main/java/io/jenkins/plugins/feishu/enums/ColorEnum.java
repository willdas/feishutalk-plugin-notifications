package io.jenkins.plugins.feishu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ColorEnum {

    RED("red"),
    BLUE("blue"),
    GREEN("green");

    private String name;
}
