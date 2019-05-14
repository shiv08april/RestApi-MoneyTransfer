package com.shiv.rest.mt.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;

@Value
@Builder
@JsonDeserialize(builder = SortCodeAccountNumber.SortCodeAccountNumberBuilder.class)
public class SortCodeAccountNumber {
    @JsonPOJOBuilder(withPrefix = "")
    public static final class SortCodeAccountNumberBuilder {}

    // invariants aren't enforced in order to allow hibernate validator to do it's thing

    @NotEmpty
    String sortCode;

    @NotEmpty
    String accountNumber;

}