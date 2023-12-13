package com.alfa.customerbills;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static com.alfa.customerbills.ConnectorUtil.*;

public class Connector {
    private static final String sqlQuery = """
        select PARENT_ID, MOBILENO, sum(totalval) netPaid, max(VISITDATE) latest_data from  (
        select PARENT_ID, MOBILENO, VISITDATE, NETTOTAL * factor as totalval from (
        select  MOBILENO, NETTOTAL, VISITDATE, NVL(PARENTINVOICEID, ID) PARENT_ID,
        (case when INVOICETYPE in(165,166) THEN 1 when INVOICETYPE = 167  THEN -1 end)  factor
        from LDM_ALFA.PATIENTINVOICEHEADER
        where cast (VISITDATE as timestamp)  > ?)) where MOBILENO is not null
        group by  PARENT_ID, MOBILENO order by latest_data""";

    private static final Logger logger = Logger.getLogger(Connector.class.getName());


    public static void main(String[] args) throws Exception {
        if (args.length >= 2) {
            if (args[0].equals("-e")) {
                System.out.println(encode(args[1]));
                return;

            } if (args[0].equals("-d")) {
                System.out.println(decode(args[1]));
                return;
            }
        }

        boolean debugSMS = false;
        if (args.length > 0) {
            if (args[0].equals("-diag")) {
                System.out.println("Java version: " + System.getProperty("java.version"));
                System.out.println("App path: " + getAppPath());
                return;
            }

            List<String> argList = Arrays.stream(args).map(String::toLowerCase).toList();
            if (argList.contains("-debug")) {
                debugSMS = true;
            }
        }

        Properties config = new Properties();
        try (FileInputStream in = new FileInputStream(getPath("customer-bills.properties"))) {
            config.load(in);
        }
        if (debugSMS) logger.info("SMS DEBUG enabled");

        try {
            List<PatientInfo> patientsInfo = new ArrayList<>();
            long maxTime = processSMS(debugSMS, config, patientsInfo);
            if (!debugSMS) sendSMSs(patientsInfo);
            if (maxTime != 0) doWriteMaxTime(maxTime);
        } catch (Throwable e) {
            logger.severe("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static long processSMS(boolean debug, Properties config, List<PatientInfo> patientsInfo) throws Exception {
        long maxTime = doReadMaxTime(Integer.parseInt(config.getProperty("defaultLagTime", "30")));
        long t0 = System.currentTimeMillis();
        try (Connection conn = connect(config); ResultSet rs = executeQuery(conn, maxTime)) {
            logger.info("Connected to DB & Executed Query in " + duration(t0));
            maxTime = transform(rs, patientsInfo, config);
        }
        return maxTime;
    }

    private static long transform(ResultSet rs, List<PatientInfo> patientsInfo, Properties config) throws SQLException {
        int recordsLimit = Integer.parseInt(config.getProperty("batch.size", "100"));
        int processedRecords = 0;
        long maxTime = 0;
        while (rs.next()) {
            patientsInfo.add(new PatientInfo(rs.getString(1), rs.getString(2), rs.getDouble(3)));
            Timestamp ts = rs.getTimestamp(4);
            if (ts != null) maxTime = Math.max(maxTime, ts.getTime());
            if (++processedRecords >= recordsLimit) break;
        }
        return maxTime;
    }

    private static void sendSMSs(List<PatientInfo> patientsInfo) {
    }

    record PatientInfo(String id, String mobile, double amount) {

        PatientInfo(String id, String mobile, double amount) {
            this.id = id;
            this.mobile = mobile == null || mobile.startsWith("0") ? mobile : ("0" + mobile);
            this.amount = amount;
        }

        public String toString() {
            return "id=\"" + id + "\", mobile=\"" + mobile + "\"" + "\", amount=\"" + amount + "\"";
        }
    }

    private static Connection connect(Properties config) throws SQLException, ClassNotFoundException, MalformedURLException, InstantiationException, IllegalAccessException {
        logger.info("Connecting to DB");
        DriverManager.registerDriver(new DriverWrapper());
        config.put("user", config.getProperty("dbUser"));
        config.put("password", decode(config.getProperty("dbPassword")));
        Connection conn = DriverManager.getConnection(config.getProperty("dbURL"), config);
        logger.info("Connected to DB");
        return conn;
    }

    private static String encode(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) bytes[i] ^= 22;
        return new String(Base64.getMimeEncoder().encode(bytes), StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        bytes = Base64.getMimeDecoder().decode(bytes);
        for (int i = 0; i < bytes.length; i++) bytes[i] ^= 22;
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static ResultSet executeQuery(Connection conn, long maxTime) throws SQLException {
        logger.info("Executing SQL query");
        PreparedStatement stmt = conn.prepareStatement(sqlQuery);
        stmt.setTimestamp(1, new Timestamp(maxTime));
        ResultSet rs = stmt.executeQuery();
        logger.info("SQL query executed successfully");
        return rs;
    }

    private static long doReadMaxTime(int lagTime) {
        try (BufferedReader in = new BufferedReader(new FileReader(getMaxTimeFile()))) {
            return Long.parseLong(in.readLine());
        } catch (Exception e) {
            long defaultLagTime = lagTime * 60 * 1000L; // 30 minutes back
            return System.currentTimeMillis() - defaultLagTime;
        }
    }

    private static void doWriteMaxTime(long maxTime) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(getMaxTimeFile()))) {
            out.write("" + maxTime);
        }
    }

    private static File getMaxTimeFile() {
        return new File(getAppPath(), "max.time");
    }



    static class DriverWrapper implements Driver {
        private static final String driverClassName = "oracle.jdbc.driver.OracleDriver";
        private final Driver delegate;

        DriverWrapper() throws MalformedURLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
           // URLClassLoader cl = URLClassLoader.newInstance(new URL[]{getPath(config.getProperty("driverJar")).toURI().toURL()});
//            this.delegate = (Driver) Class.forName(driverClassName).getDeclaredConstructor().newInstance();//.newInstance();
            this.delegate = (Driver) Class.forName(driverClassName).newInstance();
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return delegate.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return delegate.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return delegate.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegate.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return delegate.getParentLogger();
        }
    }
}