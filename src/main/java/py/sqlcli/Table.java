package py.sqlcli;

import java.util.Arrays;

import static py.sqlcli.Table.Align.*;

/**
 * @author pengyu
 * @date 2019-07-29
 */
public class Table {

    private String title;
    private final String[] headCells;
    private final String[] tailCells;
    private final String[][] cells;
    private final int rows;
    private final int columns;
    private final String middleBorder = "|";
    private final String leftBorder = "|";
    private final String rightBorder = "|";
    private Align[] alignOfColumns;
    private Color[][] colorOfCells;
    private final String RESET_COLOR = "\u001b[0m";
    private final boolean noColor;

    public Table(int rows, int columns, boolean hasHead, boolean hasTail, boolean noColor) {
        this.noColor = noColor;
        cells = new String[rows][columns];
        colorOfCells = new Color[rows][columns];
        this.rows = rows;
        this.columns = columns;
        headCells = hasHead ? new String[columns] : null;
        tailCells = hasTail ? new String[columns] : null;
        alignOfColumns = new Align[columns];
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[][] getCells() {
        return cells;
    }

    public String[] getHeadCells() {
        return headCells;
    }

    public String[] getTailCells() {
        return tailCells;
    }

    public Align[] getAlignOfColumns() {
        return alignOfColumns;
    }

    @Override
    public String toString() {
        int[] maxLengthOfColumn = new int[columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (cells[i][j] != null && cells[i][j].length() > maxLengthOfColumn[j]) {
                    maxLengthOfColumn[j] = cells[i][j].length();
                }
            }
        }
        for (int i = 0; i < columns; i++) {
            if (headCells != null && headCells[i] != null) {
                maxLengthOfColumn[i] = Math.max(maxLengthOfColumn[i], headCells[i].length());
            }
            if (tailCells != null && tailCells[i] != null) {
                maxLengthOfColumn[i] = Math.max(maxLengthOfColumn[i], tailCells[i].length());
            }
        }

        // 整体长度，不包含两边的边界字符
        int totalLength = Math.max(Arrays.stream(maxLengthOfColumn).sum() + (columns - 1) * middleBorder.length(), title.length());
        final StringBuilder sb = new StringBuilder();
        // append title
        appendTitle(sb, totalLength);
        // append head
        appendHead(sb, maxLengthOfColumn, totalLength);
        // append cells
        appendCells(sb, maxLengthOfColumn, totalLength);
        // append cells
        appendTail(sb, maxLengthOfColumn, totalLength);
        // append tail line
        sb.append(expandCell(null, totalLength, "-", "+", "+"));
        return sb.toString();
    }

    private StringBuilder appendTitle(StringBuilder sb, int totalLength) {
        // append line
        sb.append(expandCell(null, totalLength, "-", "+", "+")).append("\n");
        // append title
        sb.append(expandCell(title, totalLength, " ", "|", "|")).append("\n");
        // append line
        sb.append(expandCell(null, totalLength, "-", "|", "|")).append("\n");
        return sb;
    }

    private StringBuilder appendHead(StringBuilder sb, int[] maxLengthOfColumn, int totalLength) {
        if (headCells == null) {
            return sb;
        }
        int lengthOfThisRow = 0;
        for (int j = 0; j < columns; j++) {
            final String cellString;
//            if (j == 0) {
//                cellString = expandCell(headCells[j], maxLengthOfColumn[j], " ", leftBorder, null);
//            } else if (j != columns - 1) {
//                cellString = expandCell(headCells[j], maxLengthOfColumn[j], " ", leftBorder, null);
//            } else {
//                cellString = expandCell(headCells[j], Math.max(maxLengthOfColumn[j], totalLength - lengthOfThisRow), " ", middleBorder, rightBorder);
//            }
            cellString = expandCell(headCells[j],
                    j == columns - 1 ? Math.max(maxLengthOfColumn[j], totalLength - lengthOfThisRow) : maxLengthOfColumn[j],
                    " ",
                    j == 0 ? leftBorder : middleBorder,
                    j == columns - 1 ? rightBorder : null);
            sb.append(cellString);
            lengthOfThisRow += cellString.length();
        }
        sb.append("\n");
        // append line
        sb.append(expandCell(null, totalLength, "-", "|", "|")).append("\n");
        return sb;
    }

    private StringBuilder appendTail(StringBuilder sb, int[] maxLengthOfColumn, int totalLength) {
        if (tailCells == null) {
            return sb;
        }
        // append line
        sb.append(expandCell(null, totalLength, "-", "|", "|")).append("\n");
        int lengthOfThisRow = 0;
        for (int j = 0; j < columns; j++) {
            final String cellString;
//            if (j == 0) {
//                cellString = expandCell(tailCells[j], maxLengthOfColumn[j], " ", leftBorder, null);
//            } else if (j != columns - 1) {
//                cellString = expandCell(tailCells[j], maxLengthOfColumn[j], " ", leftBorder, null);
//            } else {
//                cellString = expandCell(tailCells[j], Math.max(maxLengthOfColumn[j], totalLength - lengthOfThisRow), " ", middleBorder, rightBorder);
//            }
            cellString = expandCell(tailCells[j],
                    j == columns - 1 ? Math.max(maxLengthOfColumn[j], totalLength - lengthOfThisRow) : maxLengthOfColumn[j],
                    " ",
                    j == 0 ? leftBorder : middleBorder,
                    j == columns - 1 ? rightBorder : null);
            sb.append(cellString);
            lengthOfThisRow += cellString.length();
        }
        sb.append("\n");
        return sb;
    }

    private StringBuilder appendCells(StringBuilder sb, int[] maxLengthOfColumn, int totalLength) {
        for (int i = 0; i < rows; i++) {
            int lengthOfThisRow = 0;
            for (int j = 0; j < columns; j++) {
                final String cellString;
//                if (j == 0) {
//                    cellString = expandCell(cells[i][j], maxLengthOfColumn[j], " ", leftBorder, null, alignOfColumns[j], colorOfCells[i][j]);
//                } else if (j != columns - 1) {
//                    cellString = expandCell(cells[i][j], maxLengthOfColumn[j], " ", leftBorder, null, alignOfColumns[j], colorOfCells[i][j]);
//                } else {
//                    cellString = expandCell(cells[i][j], Math.max(maxLengthOfColumn[j], totalLength - lengthOfThisRow), " ", middleBorder, rightBorder, alignOfColumns[j], colorOfCells[i][j]);
//                }
                cellString = expandCell(cells[i][j],
                        j == columns - 1 ? Math.max(maxLengthOfColumn[j], totalLength - lengthOfThisRow) : maxLengthOfColumn[j],
                        " ",
                        j == 0 ? leftBorder : middleBorder,
                        j == columns - 1 ? rightBorder : null,
                        alignOfColumns[j],
                        colorOfCells[i][j]);
                sb.append(cellString);
                lengthOfThisRow += cellString.length();
            }
            sb.append("\n");
        }
        return sb;
    }

    private String expandCell(String value, int length, String rest, String leftBorder, String rightBorder) {
        return expandCell(value, length, rest, leftBorder, rightBorder, null, Color.NON);
    }

    private String expandCell(String value, int length, String rest, String leftBorder, String rightBorder, Align align, Color color) {
        if (value == null) {
            value = "";
        }
        String coloredValue = null;
        if (noColor || color == null) {
            coloredValue = value;
        } else if (color != null) {
            coloredValue = color.value + value + RESET_COLOR;
        }
        final int diff = length - value.length();
        StringBuilder sb = new StringBuilder();
        if (diff <= 0) {
            if (leftBorder != null) {
                sb.append(leftBorder);
            }
            sb.append(coloredValue);
            if (rightBorder != null) {
                sb.append(rightBorder);
            }
            return sb.toString();
        }
        final int left = diff / 2;
        final int right = diff - left;
        if (leftBorder != null) {
            sb.append(leftBorder);
        }

        if (align == null) {
            appendCharacter(sb, rest, left);
            sb.append(coloredValue);
            appendCharacter(sb, rest, right);
        } else {
            switch (align) {
                case LEFT:
                    sb.append(coloredValue);
                    appendCharacter(sb, rest, left);
                    appendCharacter(sb, rest, right);
                    break;
                case RIGHT:
                    appendCharacter(sb, rest, left);
                    appendCharacter(sb, rest, right);
                    sb.append(coloredValue);
                    break;
                case CENTER:
                default:
                    appendCharacter(sb, rest, left);
                    sb.append(coloredValue);
                    appendCharacter(sb, rest, right);
            }
        }

        if (rightBorder != null) {
            sb.append(rightBorder);
        }
        return sb.toString();
    }

    private void appendCharacter(StringBuilder sb, String c, int length) {
        for (int i = 0; i < length; i++) {
            sb.append(c);
        }
    }

    public enum Align {
        LEFT, CENTER, RIGHT
    }

    public enum Color {
        NON("\u001b[0m"), RED("\u001b[31m"), GREEN("\u001b[32m");

        private String value;

        private Color(String value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
//        final Table table = new Table(5, 6);
//        table.setTitle("The Title===1111111111111111111111111111111111111111111111111112222222222222222222222222222222222");
//        table.cells[0][0] = "abc";
//        table.cells[0][1] = "111";
//        table.cells[0][2] = "222";
//        table.cells[0][3] = "3adsf33";
//        table.cells[0][4] = "444";
//        table.cells[0][5] = "444";
//        table.cells[1][0] = "abdaSDc";
//        table.cells[1][1] = "111";
//        table.cells[1][2] = "222sdfad";
//        table.cells[1][3] = "3";
//        table.cells[1][4] = "";
//        table.cells[1][5] = "asdfasdfasdfasdfadsfasdfasd";
//        table.cells[1][0] = "111";
//        table.cells[1][1] = null;
//        table.cells[1][2] = "222sdfad";
//        table.cells[1][3] = "3";
//        table.cells[1][4] = "";
//        table.cells[1][5] = "3123123";
//        System.out.println(table.toString());

        final Table table = new Table(1, 2, false, false, true);
        table.setTitle("0123456789");
        System.out.println("title: " + table.title.length());
        table.cells[0][0] = "abc";
        table.cells[0][1] = "111";
        System.out.println(table.toString());
        final Table table3 = new Table(1, 1, true, false, true);
        table3.setTitle("0123456789");
        System.out.println("title: " + table.title.length());
        table3.headCells[0] = "count(*)";
        table3.cells[0][0] = "2000";
        System.out.println(table3.toString());

        final Table table2 = new Table(5, 3, true, true, true);
        table2.setTitle("asdfasdfasdfasdf");
        table2.headCells[0] = "adsfasdfadsf";
        table2.headCells[1] = "dawee123";
        table2.headCells[2] = "adszxcv我";
        table2.cells[0][0] = "abc";
        table2.cells[0][1] = "111";
        table2.cells[0][2] = "asdfasdfasdf范德萨";
        table2.cells[1][0] = "abc";
        table2.cells[1][1] = "111";
        table2.cells[1][2] = "asdfasdfasdf";
        table2.cells[2][0] = "abc";
        table2.cells[2][1] = "111";
        table2.cells[2][2] = "asdfasdfasdf";
        table2.cells[3][0] = "abc";
        table2.cells[3][1] = "111";
        table2.cells[3][2] = String.format("%.2f%%", 0.1 * 100);
        table2.tailCells[0] = "11";
        table2.tailCells[1] = "11";
        table2.tailCells[2] = "1";
        table2.alignOfColumns[0] = LEFT;
        table2.alignOfColumns[1] = CENTER;
        table2.alignOfColumns[2] = RIGHT;
        table2.colorOfCells[0][0] = Color.RED;
        table2.colorOfCells[0][1] = Color.GREEN;
        table2.colorOfCells[0][2] = Color.NON;
        System.out.println(table2.toString());


    }
}
