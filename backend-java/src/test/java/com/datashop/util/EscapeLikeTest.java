package com.datashop.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EscapeLikeTest {

    @Test
    void escapesWildcardsAndBackslash() {
        assertThat(EscapeLike.escape("100%_off\\now")).isEqualTo("100\\%\\_off\\\\now");
    }

    @Test
    void keepsPlainTextUntouched() {
        assertThat(EscapeLike.escape("dataset")).isEqualTo("dataset");
    }

    @Test
    void handlesNull() {
        assertThat(EscapeLike.escape(null)).isNull();
    }
}
