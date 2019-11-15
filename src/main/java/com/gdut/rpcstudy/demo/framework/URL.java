package com.gdut.rpcstudy.demo.framework;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:56
 */
@Data
@AllArgsConstructor
public class URL {

    private String hostname;
    private Integer port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URL url = (URL) o;
        return Objects.equals(hostname, url.hostname) &&
                Objects.equals(port, url.port);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hostname, port);
    }
}
