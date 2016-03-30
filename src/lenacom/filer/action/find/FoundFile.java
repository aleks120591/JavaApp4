package lenacom.filer.action.find;

import lenacom.filer.config.Colors;
import lenacom.filer.config.Fonts;
import lenacom.filer.config.Resources;
import lenacom.filer.path.FileExtension;
import lenacom.filer.path.PathUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.List;

class FoundFile extends FoundPath {
    private final static String EXTRACTS_START = "<html><head><style>body '{'background-color: {0}; color: {1}; margin: 2px; font-family: {2}; font-size: {3} pt'}' .needle '{'color: {4}; font-weight: bold'}' .charset '{'color: {5}'}'</style><head><body>";
    private final static String EXTRACTS_END = "</body></html>";
    private final static String DIV_START = "<div>";
    private final static String ELLIPSIS = "...";
    private final static String DIV_END = "</div>";
    private final static String NEEDLE_START = "<span class=needle>";
    private final static String NEEDLE_END = "</span>";
    private final static String CHARSET_START = "<span class=charset>";
    private final static String CHARSET_END = "</span>";
    private final static String NEW_LINE = "<br>";

    private final static String FOUND_EXTRACTS_TEMPLATE = "<div><b>" + Resources.getMessage("dlg.find.found.extracts.template") + "</b></div><br/>";

    private List<ExtractDetails> extracts;
    private String extractsHtml;
    private String extension;
    private Color background;
    private long size;

    FoundFile(Path path, Icon icon, long size) {
         this(path, icon, size, null);
    }

    FoundFile(Path path, Icon icon, long size, List<ExtractDetails> extracts) {
        super(path);
        setIcon(icon);
        this.size = size;
        this.extracts = extracts;
    }

    String getExtension() {
        if (extension == null) init();
        return extension;
    }

    @Override
    String getName() {
        if (name == null) init();
        return name;
    }

    private void init() {
        String name = PathUtils.getName(path);
        EnumMap<FileExtension, String> result = FileExtension.getFileExtension(name);
        this.name = result.get(FileExtension.NAME);
        this.extension = result.get(FileExtension.EXTENSION);
        background = Colors.getBackgroundByExtension(extension);
    }

    long getSize() {
        return size;
    }

    Color getBackground() {
        if (background == null) init();
        return background;
    }

    private String getExtractsStart() {
        Font font = Fonts.getFont();
        return MessageFormat.format(EXTRACTS_START,
                Colors.colorToHex(Colors.getBackground()),
                Colors.colorToHex(Colors.getForeground()),
                font.getFamily(),
                font.getSize(),
                Colors.colorToHex(Colors.getSelectedForeground()),
                Colors.colorToHex(Colors.getDisabledForeground())
        );
    }

    String getExtractsHtml() {
        if (extractsHtml == null && extracts != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getExtractsStart())
                .append(MessageFormat.format(FOUND_EXTRACTS_TEMPLATE, extracts.size()));
            Charset onlyCharset = null;
            for (ExtractDetails details: extracts) {
                if (onlyCharset == null) {
                    onlyCharset = details.getCharset();
                } else {
                    if (!details.getCharset().equals(onlyCharset)) {
                        onlyCharset = null;
                        break;
                    }
                }
            }
            for (int i = 0; i < extracts.size(); i++) {
                ExtractDetails details = extracts.get(i);
                String extract = details.getExtract();
                String leftMargin = extract.substring(0, details.getStart());
                String needle = extract.substring(details.getStart(), details.getEnd());
                String rightMargin = extract.substring(details.getEnd());
                sb.append(DIV_START).append(ELLIPSIS)
                    .append(escape(leftMargin))
                    .append(NEEDLE_START)
                    .append(escape(needle))
                    .append(NEEDLE_END)
                    .append(escape(rightMargin))
                    .append(ELLIPSIS);
                if (onlyCharset == null) {
                    sb.append(" ")
                        .append(CHARSET_START)
                        .append(details.getCharset().name())
                        .append(CHARSET_END);
                }
                sb.append(DIV_END);
            }
            if (onlyCharset != null) {
                sb.append(DIV_START)
                    .append(CHARSET_START)
                    .append(onlyCharset.name())
                    .append(CHARSET_END)
                    .append(DIV_END);
            }
            sb.append(EXTRACTS_END);
            extractsHtml = sb.toString().replace("\n", NEW_LINE);
        }
        return extractsHtml;
    }

    private String escape(String text) {
        return text.replace("<", "&lt;");
    }

    public int getCountExtracts() {
        return extracts == null? 0 : extracts.size();
    }

    @Override
    void refresh() {
        background = null;
        extractsHtml = null;
    }
}
