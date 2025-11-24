// PanierSolutionId.java
package com.example.demo.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanierSolutionId implements Serializable {
    private Long panier;
    private Integer solution;
}