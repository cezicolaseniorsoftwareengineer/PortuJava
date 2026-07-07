package com.biotechpay.lab.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Pairs a short excerpt of a referenceSolution with an explanation of why that part of the code is
 * written the way it is. Distinct from hints (which guide a student toward an answer without giving
 * it away): annotations only ever surface alongside the already-revealed solution, in the "Resposta
 * completa" panel.
 */
@Embeddable
public class SolutionAnnotation {

    @Column(name = "code_excerpt", length = 500)
    private String codeExcerpt;

    @Column(name = "explanation", length = 800)
    private String explanation;

    public SolutionAnnotation() {}

    public SolutionAnnotation(String codeExcerpt, String explanation) {
        this.codeExcerpt = codeExcerpt;
        this.explanation = explanation;
    }

    public String getCodeExcerpt() { return codeExcerpt; }
    public void setCodeExcerpt(String codeExcerpt) { this.codeExcerpt = codeExcerpt; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
