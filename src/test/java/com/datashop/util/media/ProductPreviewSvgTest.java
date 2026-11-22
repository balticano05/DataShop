package com.datashop.util.media;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPreviewSvgTest {

    private static final UUID PRODUCT_ID = UUID.fromString("2f0c1f1e-65b5-4d8c-a08a-4f7a6c3c9d10");

    @Test
    void rendersValidSvgForEveryVariant() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        for (int variant = 0; variant < 3; variant++) {
            String svg = ProductPreviewSvg.render("Акции Tesla 2019–2024", PRODUCT_ID, variant);
            Document document = factory.newDocumentBuilder().parse(
                    new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));

            assertThat(document.getDocumentElement().getTagName()).isEqualTo("svg");
            assertThat(svg).contains("SUPABASE STORAGE PREVIEW " + (variant + 1) + " / 3");
            assertThat(svg).doesNotContain("<script");
        }
    }

    @Test
    void escapesMarkupFromProductTitle() {
        String svg = ProductPreviewSvg.render("<b>&\"'</b>", PRODUCT_ID, 0);

        assertThat(svg).contains("&lt;b&gt;&amp;&quot;&apos;&lt;/b&gt;");
        assertThat(svg).doesNotContain("<b>");
    }

    @Test
    void isDeterministicForSameProductAndVariant() {
        String first = ProductPreviewSvg.render("Крипто-пары BTC/ETH", PRODUCT_ID, 1);
        String second = ProductPreviewSvg.render("Крипто-пары BTC/ETH", PRODUCT_ID, 1);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void variantsUseDifferentCharts() {
        String line = ProductPreviewSvg.render("Погода по 100 городам", PRODUCT_ID, 0);
        String bars = ProductPreviewSvg.render("Погода по 100 городам", PRODUCT_ID, 1);
        String scatter = ProductPreviewSvg.render("Погода по 100 городам", PRODUCT_ID, 2);

        assertThat(line).isNotEqualTo(bars);
        assertThat(bars).isNotEqualTo(scatter);
        assertThat(line).contains("<polyline");
        assertThat(bars).contains("<rect");
        assertThat(scatter).contains("<circle");
    }
}
