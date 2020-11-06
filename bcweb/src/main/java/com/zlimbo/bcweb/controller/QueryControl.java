package com.zlimbo.bcweb.controller;

import com.zlimbo.bcweb.domain.Sql;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("")
public class QueryControl {

    @GetMapping("/query")
    public String query(Model model) {
        model.addAttribute("sql", new Sql());
        return "query";
    }


    @PostMapping("/query")
    public ModelAndView query(@ModelAttribute Sql sql) {
        System.out.println("----------------------------------query ok");
        List<String> columnNames = new ArrayList<>();
        List<List<String>> columnValues = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(ConnectInfo.DB_URL, ConnectInfo.USER, ConnectInfo.PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();

            System.out.println("query sql: " + sql.getSql());
            ResultSet resultSet = stmt.executeQuery(sql.getSql());

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();

            for (int i = 0; i < columnCount; ++i) {
                String columnName = resultSetMetaData.getColumnName(i + 1);
                columnNames.add(columnName);
                System.out.println("columnName: " + columnName);
            }

            while (resultSet.next()) {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < columnCount; ++i) {
                    list.add(resultSet.getString(i + 1));
                }
                columnValues.add(list);
            }
            resultSet.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    conn.close();
            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }
        ModelAndView modelAndView = new ModelAndView("query");
        modelAndView.addObject("columnNames", columnNames);
        modelAndView.addObject("columnValues", columnValues);
        modelAndView.addObject("existValue", !columnValues.isEmpty());
        return modelAndView;
    }
}
