package com.datashop.util.media;

import com.datashop.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Builds deterministic SVG previews for seeded products.
 *
 * <p>The generated documents contain no scripts or external references, so they
 * can be stored in PostgreSQL and rendered inside an {@code img} element without
 * granting the image any page privileges.</p>
 */
public final class ProductPreviewSvg {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TITLE_MAX_CHARS = 24;
    private static final int TITLE_MAX_LINES = 2;

    private static final String[][] PALETTES = {
            {"#0f172a", "#1d4ed8", "#60a5fa"},
            {"#111827", "#6d28d9", "#c4b5fd"},
            {"#042f2e", "#0f766e", "#5eead4"},
            {"#1e1b4b", "#4338ca", "#a5b4fc"},
            {"#3b0764", "#a21caf", "#f0abfc"},
            {"#431407", "#c2410c", "#fdba74"},
            {"#052e16", "#15803d", "#86efac"},
            {"#082f49", "#0369a1", "#7dd3fc"},
            {"#4c0519", "#be123c", "#fda4af"},
            {"#1c1917", "#57534e", "#d6d3d1"},
            {"#172554", "#1d4ed8", "#93c5fd"},
            {"#3f1d0b", "#b45309", "#fcd34d"}
    };

    private ProductPreviewSvg() {
    }

    public static String render(String title, UUID productId, int variant) {
        String safeTitle = title == null || title.isBlank() ? "DataShop dataset" : title.trim();
        int paletteIndex = Math.floorMod(productId.hashCode(), PALETTES.length);
        String[] palette = PALETTES[paletteIndex];
        int chart = Math.floorMod(variant, Constants.Media.PREVIEWS_PER_PRODUCT);
        Random random = new Random(
                productId.getMostSignificantBits()
                        ^ productId.getLeastSignificantBits()
                        ^ (variant * 0x9E3779B97F4A7C15L));

        List<String> titleLines = wrap(safeTitle, TITLE_MAX_CHARS, TITLE_MAX_LINES);
        StringBuilder svg = new StringBuilder(4096);
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                .append(WIDTH)
                .append("\" height=\"")
                .append(HEIGHT)
                .append("\" viewBox=\"0 0 ")
                .append(WIDTH)
                .append(' ')
                .append(HEIGHT)
                .append("\" role=\"img\" aria-label=\"")
                .append(escape(safeTitle))
                .append(" — preview ")
                .append(variant + 1)
                .append("\">");
        svg.append("<defs>")
                .append("<linearGradient id=\"bg\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">")
                .append("<stop offset=\"0\" stop-color=\"").append(palette[0]).append("\"/>")
                .append("<stop offset=\"1\" stop-color=\"").append(palette[1]).append("\"/>")
                .append("</linearGradient>")
                .append("<linearGradient id=\"area\" x1=\"0\" y1=\"0\" x2=\"0\" y2=\"1\">")
                .append("<stop offset=\"0\" stop-color=\"").append(palette[2])
                .append("\" stop-opacity=\"0.45\"/>")
                .append("<stop offset=\"1\" stop-color=\"").append(palette[2])
                .append("\" stop-opacity=\"0\"/>")
                .append("</linearGradient>")
                .append("<pattern id=\"grid\" width=\"40\" height=\"40\" patternUnits=\"userSpaceOnUse\">")
                .append("<path d=\"M 40 0 L 0 0 0 40\" fill=\"none\" stroke=\"#ffffff\" ")
                .append("stroke-opacity=\"0.07\" stroke-width=\"1\"/>")
                .append("</pattern>")
                .append("</defs>");
        svg.append("<rect width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" fill=\"url(#bg)\"/>")
                .append("<rect width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" fill=\"url(#grid)\"/>");

        svg.append("<g font-family=\"DejaVu Sans, Arial, Helvetica, sans-serif\">");
        svg.append("<text x=\"60\" y=\"72\" fill=\"#ffffff\" fill-opacity=\"0.82\" ")
                .append("font-size=\"26\" font-weight=\"700\" letter-spacing=\"1.5\">DataShop</text>");
        svg.append("<rect x=\"620\" y=\"44\" width=\"120\" height=\"36\" rx=\"18\" ")
                .append("fill=\"#ffffff\" fill-opacity=\"0.14\"/>")
                .append("<text x=\"680\" y=\"68\" fill=\"#ffffff\" fill-opacity=\"0.92\" ")
                .append("font-size=\"15\" text-anchor=\"middle\" letter-spacing=\"1\">DATASET</text>");

        int titleY = 145;
        for (String line : titleLines) {
            svg.append("<text x=\"60\" y=\"").append(titleY)
                    .append("\" fill=\"#ffffff\" font-size=\"42\" font-weight=\"700\">")
                    .append(escape(line))
                    .append("</text>");
            titleY += 52;
        }

        switch (chart) {
            case 0 -> appendLineChart(svg, palette[2], random);
            case 1 -> appendBarChart(svg, palette[2], random);
            default -> appendScatterChart(svg, palette[2], random);
        }

        svg.append("<text x=\"60\" y=\"548\" fill=\"#ffffff\" fill-opacity=\"0.72\" ")
                .append("font-size=\"16\" letter-spacing=\"1.2\">SUPABASE STORAGE PREVIEW ")
                .append(variant + 1)
                .append(" / ")
                .append(Constants.Media.PREVIEWS_PER_PRODUCT)
                .append("</text>");
        svg.append("<text x=\"740\" y=\"548\" fill=\"#ffffff\" fill-opacity=\"0.55\" ")
                .append("font-size=\"15\" text-anchor=\"end\">DataShop catalog</text>");
        svg.append("</g></svg>");
        return svg.toString();
    }

    private static void appendLineChart(StringBuilder svg, String accent, Random random) {
        int left = 60;
        int right = 740;
        int top = 260;
        int bottom = 470;
        int points = 12;
        StringBuilder line = new StringBuilder();
        StringBuilder area = new StringBuilder();
        area.append("M ").append(left).append(' ').append(bottom);
        for (int index = 0; index < points; index++) {
            int x = left + (right - left) * index / (points - 1);
            int y = top + random.nextInt(bottom - top - 20);
            if (index > 0) {
                line.append(' ');
            }
            line.append(x).append(',').append(y);
            area.append(" L ").append(x).append(' ').append(y);
        }
        area.append(" L ").append(right).append(' ').append(bottom).append(" Z");
        svg.append("<path d=\"").append(area).append("\" fill=\"url(#area)\"/>")
                .append("<polyline points=\"").append(line).append("\" fill=\"none\" stroke=\"")
                .append(accent).append("\" stroke-width=\"5\" stroke-linecap=\"round\" ")
                .append("stroke-linejoin=\"round\"/>");
        appendAxis(svg);
    }

    private static void appendBarChart(StringBuilder svg, String accent, Random random) {
        int left = 60;
        int bottom = 470;
        int width = 38;
        int gap = 18;
        for (int index = 0; index < 12; index++) {
            int height = 45 + random.nextInt(160);
            int x = left + index * (width + gap);
            svg.append("<rect x=\"").append(x)
                    .append("\" y=\"").append(bottom - height)
                    .append("\" width=\"").append(width)
                    .append("\" height=\"").append(height)
                    .append("\" rx=\"8\" fill=\"").append(accent)
                    .append("\" fill-opacity=\"").append(0.45 + (index % 4) * 0.12)
                    .append("\"/>");
        }
        appendAxis(svg);
    }

    private static void appendScatterChart(StringBuilder svg, String accent, Random random) {
        int left = 60;
        int right = 740;
        int top = 260;
        int bottom = 470;
        StringBuilder points = new StringBuilder();
        for (int index = 0; index < 28; index++) {
            int x = left + random.nextInt(right - left);
            int y = top + random.nextInt(bottom - top);
            int radius = 5 + random.nextInt(7);
            points.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
                    .append("\" r=\"").append(radius)
                    .append("\" fill=\"").append(accent)
                    .append("\" fill-opacity=\"").append(0.35 + (index % 5) * 0.11)
                    .append("\"/>");
        }
        svg.append(points)
                .append("<path d=\"M 60 430 C 180 370, 290 455, 400 350 ")
                .append("S 620 315, 740 285\" fill=\"none\" stroke=\"#ffffff\" ")
                .append("stroke-opacity=\"0.7\" stroke-width=\"4\" stroke-linecap=\"round\"/>");
        appendAxis(svg);
    }

    private static void appendAxis(StringBuilder svg) {
        svg.append("<path d=\"M 60 500 L 740 500\" stroke=\"#ffffff\" ")
                .append("stroke-opacity=\"0.22\" stroke-width=\"2\"/>");
    }

    private static List<String> wrap(String value, int maxChars, int maxLines) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : value.split("\\s+")) {
            if (!current.isEmpty() && current.length() + 1 + word.length() > maxChars) {
                lines.add(current.toString());
                current.setLength(0);
                if (lines.size() == maxLines) {
                    break;
                }
            }
            if (!current.isEmpty()) {
                current.append(' ');
            }
            current.append(word);
        }
        if (lines.size() < maxLines && !current.isEmpty()) {
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add(value);
        }
        if (lines.size() == maxLines) {
            String remainder = String.join(" ", value.split("\\s+"));
            String consumed = String.join(" ", lines);
            if (remainder.length() > consumed.length()) {
                int last = lines.size() - 1;
                String line = lines.get(last);
                lines.set(last, line.length() > 2
                        ? line.substring(0, line.length() - 1) + "…"
                        : line + "…");
            }
        }
        return lines;
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            switch (ch) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&apos;");
                default -> escaped.append(ch);
            }
        }
        return escaped.toString();
    }
}
