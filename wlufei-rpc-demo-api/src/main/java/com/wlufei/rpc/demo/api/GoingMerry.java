package com.wlufei.rpc.demo.api;


import lombok.*;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class GoingMerry implements Serializable {
    private String captain;
    private List<String> partners;
}
