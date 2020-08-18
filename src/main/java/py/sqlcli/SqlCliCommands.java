package py.sqlcli;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

/**
 * @author pengyu
 * @date 2019-07-29
 */
@ShellComponent
public class SqlCliCommands {
    @Value("${jdbc.driver}")
    private String driver;
    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.username}")
    private String username;
    @Value("${jdbc.password}")
    private String password;
    @Value("${outputfile}")
    private String outputfile;

    Connection connection = null;
    Statement stmt = null;

    FileWriter fileWriter = null;
    PrintWriter printWriter = null;

    @PostConstruct
    public void init() {
        try {
            System.out.println("driver: " + driver);
            System.out.println("url: " + url);
            System.out.println("username: " + username);
            System.out.println("password: " + password);
            Class.forName(driver);
            System.out.println("connecting the DB server");
            connection = DriverManager.getConnection(url, username, password);
            stmt = connection.createStatement();

            System.out.println("opening output file");
            File file = new File(outputfile);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file, true);
            printWriter = new PrintWriter(fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to start!");
            System.exit(1);
        }
    }

    @ShellMethod("execute sql")
    public String exec(
            @ShellOption() String sql,
            @ShellOption() boolean json,
            @ShellOption() boolean excel) {

        sql = sql.trim();
        if (StringUtils.startsWithIgnoreCase(sql, "select")) {
            return execQuery(sql, json, excel);
        } else {
            return execUpdate(sql);
        }

    }

    private String execUpdate(String sql) {
        try {
            int rows = stmt.executeUpdate(sql);
            return "rows: " + rows;
        } catch (SQLException e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    private String execQuery(String sql, boolean json, boolean excel) {
        try (ResultSet rs = stmt.executeQuery(sql)) {
            final ResultSetMetaData metaData = rs.getMetaData();
            final int columnCount = metaData.getColumnCount();
            Map<String, String> resultMap = new HashMap<>();
            final String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            final List<String[]> rows = new ArrayList<>();
            while (rs.next()) {
                final String[] row = new String[columnCount];
                for (int i = 0; i < row.length; i++) {
                    row[i] = rs.getString(i + 1);
                }
                rows.add(row);
            }
            final String output;
            if (json) {
                output = createJsonString(columnNames, rows);
            } else if (excel) {
                output = createExcelFile(columnNames, rows);
            } else {
                final Table table = createTable(columnNames, rows);
                output = table.toString();
            }
            appendFile(sql, output);
            return output;
        } catch (SQLException e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    private Table createTable(String[] columnNames, List<String[]> rows) {
        final int rowCount = rows.size();
        final Table table = new Table(rowCount, columnNames.length, true, false, true);
        table.setTitle("Result");
        final String[] headCells = table.getHeadCells();
        for (int i = 0; i < columnNames.length; i++) {
            headCells[i] = columnNames[i];
        }
        final String[][] cells = table.getCells();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            final String[] row = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++) {
                cells[rowIndex][columnIndex] = row[columnIndex];
            }
        }
        return table;
    }

    private String createJsonString(String[] columnNames, List<String[]> rows) {
        final int rowCount = rows.size();
        JSONArray jsonArray = new JSONArray(rowCount);
        for (String[] row : rows) {
            JSONObject field = new JSONObject();
            for (int i = 0; i < row.length; i++) {
                field.put(columnNames[i], row[i]);
            }
            jsonArray.add(field);
        }
        return "";
    }

    private String createExcelFile(String[] columnNames, List<String[]> rows) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        String filename = sdf.format(new Date());
        final String pathname = "/tmp/java-sql-cli/";
        filename = pathname + filename + ".xlsx";
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Result");
        final XSSFRow firstRow = sheet.createRow(0);
        // 表头
        for (int i = 0; i < columnNames.length; i++) {
            final XSSFCell cell = firstRow.createCell(i);
            cell.setCellValue(columnNames[i]);
        }

        for (int i = 0; i < rows.size(); i++) {
            final XSSFRow row = sheet.createRow(i + 1);
            for (int j = 0; j < columnNames.length; j++) {
                final XSSFCell cell = row.createCell(j);
                cell.setCellValue(rows.get(i)[j]);
            }
        }
        File path = new File(pathname);
        if (!path.exists()) {
            final boolean ok = path.mkdirs();
            if (!ok) {
                return "Make Directories Error: " + pathname;
            }
        }
        File file = new File(filename);
        try {
            final boolean ok = file.createNewFile();
            if (!ok) {
                return "Create File Error: " + filename;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Create File Error: " + filename;
        }
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
            return "Written to file: " + filename;
        } catch (IOException e) {
            e.printStackTrace();
            return "Write data error: " + filename;
        }
    }

    private void appendFile(String sql, String result) throws IOException {
        printWriter.println("");
        printWriter.println("===");
        printWriter.println("sql: " + sql);
        printWriter.println(result);
        printWriter.println("===");
        printWriter.flush();
    }

    @PreDestroy
    public void preDestroy() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (printWriter != null) {
            printWriter.close();
        }
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}